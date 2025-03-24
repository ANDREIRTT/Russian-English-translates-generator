package preset

enum class PresetParams(val paramName: String) {
    JSON("json");

    companion object {
        fun fromString(paramName: String): PresetParams {
            return entries.first { it.paramName == paramName }
        }
    }
}