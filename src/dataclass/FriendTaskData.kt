package dataclass

@kotlinx.serialization.Serializable
data class FriendTaskData(
    val taskId : Int,
    val userId : Int,
    val userName : String,
    val name : String,
    val deadLine : String,
    val priority : Int,
    val share : Boolean,
    val tag : String,
)
