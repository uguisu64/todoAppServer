import dataclass.TaskData
import dsl.UserTaskTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class Task() {
    fun AddTask(userId : Int, data : TaskData) {
        val taskTable = UserTaskTable(userId)

        transaction {
            SchemaUtils.create(taskTable)

            taskTable.insert {
                it[name] = data.name
                it[deadLine] = data.deadLine
                it[priority] = data.priority
                it[share] = data.share
                it[tag] = data.tag
            }
        }
    }

    fun EditTask(userId : Int, data : TaskData) {
        val taskTable = UserTaskTable(userId)

        transaction {
            taskTable.update ({taskTable.taskId eq data.taskId}) {
                it[name] = data.name
                it[deadLine] = data.deadLine
                it[priority] = data.priority
                it[share] = data.share
                it[tag] = data.tag
            }
        }
    }
}