package com.example.frauddetector.data.db

import androidx.room.TypeConverter

class RoomConverters {

    @TypeConverter
    fun mapToString(value: Map<String, String>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.entries.joinToString(separator = "\u001F") { (key, entryValue) ->
            "${escape(key)}\u001E${escape(entryValue)}"
        }
    }

    @TypeConverter
    fun stringToMap(value: String?): Map<String, String> {
        if (value.isNullOrBlank()) return emptyMap()
        return value.split("\u001F")
            .mapNotNull { pair ->
                val parts = pair.split("\u001E", limit = 2)
                if (parts.size != 2) return@mapNotNull null
                unescape(parts[0]) to unescape(parts[1])
            }
            .toMap()
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\u001E", "\\u001E")
        .replace("\u001F", "\\u001F")

    private fun unescape(value: String): String = value
        .replace("\\u001F", "\u001F")
        .replace("\\u001E", "\u001E")
        .replace("\\\\", "\\")
}
