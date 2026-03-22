# Data contract and model integration

## Scope split

- Android app: capture device-observable behavior, normalize it into `behavior_seq`, persist locally, and prepare runtime detection input.
- AutoDL / training side: offline training, label iteration, score generation, model export.
- Runtime `score`: optional/null in app exports; it is **not** used as an online inference feature.

## Canonical app-side behavior schema

Each normalized behavior event is exported with fields compatible with the training-side `behavior_seq` contract:

- `action`
- `timestamp`
- `app`
- `app_type`
- `website`
- `website_type`
- `information`
- `online`
- `observable`

Optional runtime metadata:

- `source`
- `package_name`

## Training-friendly record shape

Runtime export can be serialized as:

```json
{
  "behavior_seq": [...],
  "score": null,
  "source": "device_capture",
  "case_type": null,
  "meta": {
    "window_start": "2026-03-19 08:00:00",
    "window_end": "2026-03-19 08:05:00",
    "event_count": 6,
    "window_millis": 300000
  }
}
```

`score` originates from offline labeling / `evaluation.score` flattening and stays outside runtime detection features.

## Observable vs semantic actions

### Real system-observable events currently implemented or prepared

- foreground app open / switch / close via `UsageStatsManager` + `UsageEvents`
- package install / uninstall / update via package broadcasts
- camera active events via `AppOpsManager.startWatchingActive` (best effort)

### Training-sample semantic actions not directly observable

Examples such as `文本聊天`, `购买商品`, or chat content summaries may exist in training data, but on-device runtime capture does **not** directly read them from third-party apps. Those semantic actions require offline mapping/inference or manual annotation.

## Detector boundary

### Real chain

- `DetectionInputBuilder`
- `FraudDetector`
- `RuleBasedFraudDetector`
- `LocalLlmFraudDetector`
- `LocalInferenceEngine` / `LlamaCppInferenceEngine`
- fallback from local LLM to rule based detection

### Demo / stub chain

- `FakeEventSource`
- `StubFraudTypeClassifier`
- `StubFraudVerifier`
- `FraudDetectionEngine` (demo-only compatibility path)

## Local model deployment contract

Target deployment route:

1. train / fine-tune on AutoDL
2. merge LoRA back into base model
3. export merged HF model
4. convert to GGUF
5. Android runtime loads GGUF through `llama.cpp`

Current repo status:

- the app exposes `LocalInferenceEngine` and `LlamaCppInferenceEngine`
- the JNI / llama.cpp runtime is **not** fully wired yet
- fallback to `RuleBasedFraudDetector` is the default safe path when no GGUF model is available
- the app is now scaffolded around `Qwen/Qwen2-1.5B-Instruct` as the target embedded model id

## Platform limitations

- UsageStats requires manual user authorization and may vary across ROMs.
- Camera AppOps callbacks can vary by device/ROM and are best-effort only for ordinary apps.
- Background persistence is now driven through a foreground service when collection is enabled; reboot restore is still not implemented.


## Why foreground app events may not appear

If opening/switching/closing Gmail, YouTube, Chrome or other apps does not produce records, the most common reasons are:

- `UsageStats` / Usage Access has not been granted yet, so foreground app open/switch/close events cannot be queried.
- the collection foreground service is not running because the in-app collection switch is off.
- notification permission is disabled on newer Android versions, so the foreground service may not stay visibly/consistently active.

## Integrating and testing the real Qwen model

Recommended path for `Qwen/Qwen2-1.5B-Instruct`:

1. Fine-tune or prepare the merged Hugging Face model off-device.
2. Convert the merged model to GGUF for `llama.cpp`.
3. Push the `.gguf` file onto the test device (for example into app-specific external storage).
4. Set `LOCAL_LLM_MODEL_PATH` to that absolute device path for the build variant you use.
5. Replace the TODO in `LlamaCppInferenceEngine` with the actual JNI-backed `llama.cpp` loading / inference call.
6. Enable collection, generate a real `behavior_seq`, then confirm the detection result source switches from `RULE_BASED` to `LOCAL_LLM`.
