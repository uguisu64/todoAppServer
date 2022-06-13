import dataclass.TaskData
import dsl.UserTaskTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class Task() {
    fun addTask(userId : Int, data : TaskData): Int {
        val taskTable = UserTaskTable(userId)
        var taskId = 0

        transaction {
            SchemaUtils.create(taskTable)

            taskId = taskTable.insert {
                it[name]     = data.name
                it[deadLine] = data.deadLine
                it[priority] = data.priority
                it[share]    = data.share
                it[tag]      = data.tag
            } get taskTable.taskId
        }

        return taskId
    }

    fun editTask(userId : Int, data : TaskData) {
        val taskTable = UserTaskTable(userId)

        transaction {
            taskTable.update ({taskTable.taskId eq data.taskId}) {
                it[name]     = data.name
                it[deadLine] = data.deadLine
                it[priority] = data.priority
                it[share]    = data.share
                it[tag]      = data.tag
            }
        }
    }

    fun deleteTask(userId: Int, taskId : Int) {
        val taskTable = UserTaskTable(userId)

        transaction {
            taskTable.deleteWhere { taskTable.taskId eq taskId }
        }
    }

    fun allTask(userId : Int) : MutableList<TaskData> {
        val taskTable = UserTaskTable(userId)
        val tasks = mutableListOf<TaskData>()

        transaction {
            taskTable.selectAll().forEach() {
                val task = TaskData(taskId   = it[taskTable.taskId],
                                    name     = it[taskTable.name],
                                    deadLine = it[taskTable.deadLine],
                                    priority = it[taskTable.priority],
                                    share    = it[taskTable.share],
                                    tag      = it[taskTable.tag])
                tasks.add(task)
            }
        }

        return tasks
    }
}