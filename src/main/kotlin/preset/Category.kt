package preset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    @SerialName("NOUN")
    NOUN,
    VERB,
    @SerialName("ADJECTIVE")
    ADJECTIVE,
    @SerialName("ADVERB")
    ADVERB,
    @SerialName("PRONOUN")
    OTHER
}