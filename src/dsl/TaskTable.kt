package dsl

import org.jetbrains.exposed.sql.Table

object TaskTable : Table("TaskTable") {
    val taskId   = integer("taskId").autoIncrement()
    val userId   = integer("userId")
    val name     = text("name")
    val deadLine = text("deadline")
    val priority = integer("priority")
    val share    = bool("share")
    val tag      = text("tag")

    override val primaryKey = PrimaryKey(taskId)
}