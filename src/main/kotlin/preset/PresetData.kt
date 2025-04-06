package preset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresetData(
    @SerialName("title")
    val name: String,
    @SerialName("level")
    val level: Level,
    @SerialName("category")
    val category: Category,
    @SerialName("words")
    val words: List<PresetWord>
)

@Serializable
data class PresetWord(
    @SerialName("word_en")
    val wordEn: String,
    @SerialName("word_ru")
    val wordRu: String
)