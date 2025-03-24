package preset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Level {
    @SerialName("A1")
    A1,

    @SerialName("A2")
    A2,

    @SerialName("B1")
    B1,

    @SerialName("B2")
    B2,

    @SerialName("C1")
    C1,

    @SerialName("C2")
    C2;

    companion object {
        fun fromString(level: String): Level {
            return entries.first { it.name == level }
        }
    }
}