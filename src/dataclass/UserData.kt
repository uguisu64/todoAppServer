package dataclass

@kotlinx.serialization.Serializable
data class UserData(
    val id : Int,
    val name : String,
    val pass : String,
)
