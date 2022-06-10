package dataclass

import java.time.LocalDate

data class TaskData(
    val taskId : Int,
    val name : String,
    val deadLine : String,
    val priority : Int,
    val share : Boolean,
    val tag : String,
)
