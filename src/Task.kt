/*
*** File Name           : Task.kt
*** Designer            : 加藤　颯真
*** Date                : 2022.06.21
*** Purpose             : タスクの追加、削除、編集等のタスクテーブルに関わる処理を行うクラス
*/
/*  Commit History
*** 2022.06.07 6/7
*** 2022.06.12 json返却追加
*** 2022.06.13 タスク関連追加
*** 2022.06.14 タスク削除
*** 2022.06.21 テーブル仕様変更
*** 2022.07.03 : 仕様変更修正
*** 2022.07.05 : 微修正
 */

import dataclass.FriendTaskData
import dataclass.TaskData
import dsl.TaskTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class Task() {
    /*
    関数名: addTask
    引数　: data : TaskData
    返り値: Int
    動作　: 受け取ったタスクデータをタスクテーブルに書き込む。割り当てられたtaskIdを返す
    作成者: 加藤　颯真
     */
    fun addTask(data: TaskData): Int {
        var taskId = 0

        transaction {
            taskId = TaskTable.insert {
                it[name]     = data.name
                it[userId]   = data.userId
                it[deadLine] = data.deadLine
                it[priority] = data.priority
                it[share]    = data.share
                it[tag]      = data.tag
            } get TaskTable.taskId
        }

        return taskId
    }

    /*
    関数名: editTask
    引数　: data : TaskData
    返り値: Unit
    動作　: dataの内容でタスクテーブルの同じtaskIdのレコードを更新する
    作成者: 加藤　颯真
     */
    fun editTask(data: TaskData) {
        transaction {
            TaskTable.update ({(TaskTable.taskId eq data.taskId) and (TaskTable.userId eq data.userId)}) {
                it[name]     = data.name
                it[deadLine] = data.deadLine
                it[priority] = data.priority
                it[share]    = data.share
                it[tag]      = data.tag
            }
        }
    }

    /*
    関数名: deleteTask
    引数　: userId : Int, taskId : Int
    返り値: Unit
    動作　: userIdとtaskIdを受け取り、該当するレコードをタスクテーブルから削除する
    作成者: 加藤　颯真
     */
    fun deleteTask(userId: Int, taskId : Int) {
        transaction {
            TaskTable.deleteWhere { (TaskTable.taskId eq taskId) and (TaskTable.userId eq userId) }
        }
    }

    /*
    関数名: allTask
    引数　: userId : Int
    返り値: MutableList<TaskData>
    動作　: userIdを受け取り、そのIDのユーザーの全てのレコードをリストにして返す
    作成者: 加藤　颯真
     */
    fun allTask(userId: Int) : MutableList<TaskData> {
        val tasks = mutableListOf<TaskData>()

        transaction {
            TaskTable.select { TaskTable.userId eq userId }.forEach {
                val task = TaskData(
                    taskId   = it[TaskTable.taskId],
                    userId   = it[TaskTable.userId],
                    name     = it[TaskTable.name],
                    deadLine = it[TaskTable.deadLine],
                    priority = it[TaskTable.priority],
                    share    = it[TaskTable.share],
                    tag      = it[TaskTable.tag]
                )
                tasks.add(task)
            }
        }

        return tasks
    }

    /*
    関数名: friendTask
    引数　: friendId: Int, friendName: String
    返り値: MutableList<FriendTaskData>
    動作　: friendIdを受け取り、そのIDのユーザーの全てのレコードをリストにして返す
    作成者: 加藤　颯真
     */
    fun friendTask(friendId: Int, friendName: String) : MutableList<FriendTaskData> {
        val tasks = mutableListOf<FriendTaskData>()

        transaction {
            TaskTable.select { TaskTable.userId eq friendId }.forEach {
                val task = FriendTaskData(
                    taskId   = it[TaskTable.taskId],
                    userId   = it[TaskTable.userId],
                    userName = friendName,
                    name     = it[TaskTable.name],
                    deadLine = it[TaskTable.deadLine],
                    priority = it[TaskTable.priority],
                    share    = it[TaskTable.share],
                    tag      = it[TaskTable.tag]
                )
                tasks.add(task)
            }
        }

        return tasks
    }
}