package dataclass

@kotlinx.serialization.Serializable
data class TaskData(
    val taskId : Int,
    val userId : Int,
    val name : String,
    val deadLine : String,
    val priority : Int,
    val share : Boolean,
    val tag : String,
)
