import dataclass.TaskData
import dsl.UserTaskTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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

    fun allTask(userId : Int) : MutableList<TaskData> {
        val taskTable = UserTaskTable(userId)
        val tasks = mutableListOf<TaskData>()

        transaction {
            taskTable.selectAll().forEach() {
                val task = TaskData(it[taskTable.taskId], it[taskTable.name], it[taskTable.deadLine], it[taskTable.priority], it[taskTable.share], it[taskTable.tag])
                tasks.add(task)
            }
        }

        return tasks
    }
}