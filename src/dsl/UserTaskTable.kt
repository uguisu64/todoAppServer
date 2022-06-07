package dsl

import org.jetbrains.exposed.sql.Table

class UserTaskTable(id : Int) : Table("UserTask$id") {
    val taskId = integer("id").autoIncrement()
    val name = text("name")
    val deadLine = text("deadline")
    val priority = integer("priority")
    val share = bool("share")
    val tag = text("tag")

    override val primaryKey = PrimaryKey(taskId)
}