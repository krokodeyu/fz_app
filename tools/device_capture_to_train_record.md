# device_capture to train record

1. Device runtime captures normalized `BehaviorEvent` records locally.
2. Window aggregation + dedupe produce a stable `behavior_seq`.
3. Exported records keep `score = null` until offline annotation.
4. Offline tooling can join `behavior_seq` with label metadata and flatten `evaluation.score` into `score`.
5. Final merged LLM artifacts are deployed back to Android as GGUF files, never trained in this repo.
