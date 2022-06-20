/*
*** File Name           : Application.kt
*** Designer            : 加藤　颯真
*** Date                : 2022.06.20
*** Purpose             : サーバーに必要なクラスのインスタンス化。サーバーの設定を行い実行する。
*/
/*  Commit History
*** 2022.05.31 : first commit
*** 2022.06.06 : DB接続コード追加
*** 2022.06.06 : userテーブル用object追加
*** 2022.06.07 : 6/7
*** 2022.06.12 : json返却追加
*** 2022.06.13 : タスク関連追加
*** 2022.06.13 : ユーザー追加
*** 2022.06.14 : タスク削除
*** 2022.06.14 : ビルド可能に修正
*** 2022.06.15 : パラメータエラー文追加
*** 2022.06.15 : ユーザ追加処理変更
*** 2022.06.20 : 表題コメント追加
*/

import dataclass.TaskData
import dataclass.UserData
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

fun main(args: Array<String>) {
    //コマンドライン引数がない場合プログラムを終了させる
    if(args.size != 2) {
        println("実行するには")
        println("java -jar 実行ファイル名 [ポート番号] [sqliteのファイルパス]のように実行してください")
        return
    }

    //sqliteのDataBaseファイルのパス
    val dbPath = "jdbc:sqlite:${args[1]}"

    //sqliteへの接続
    Database.connect(dbPath,"org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    //各クラスのインスタンス化
    val taskManager = Task()
    val userManage  = UserManage()

    //サーバーの設定
    val server = embeddedServer(Netty, port = args[0].toInt()) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            post("/user") {
                val request = call.receive<UserData>()

                val userId = userManage.createUser(request.name,request.pass)
                call.respond(UserData(userId,request.name,request.pass))
            }
            route("/user/{userId}") {
                get("/task") {
                    val name = call.parameters["name"]
                    val pass = call.parameters["pass"]
                    val id   = call.parameters["userId"]
                    if(id != null && name != null && pass != null){
                        val userData = UserData(id.toInt(),name,pass)
                        if(userManage.authUser(userData)){
                            call.respond(taskManager.allTask(id.toInt()))
                        }
                    }
                }
                post("/task") {
                    val request = call.receive<TaskData>()

                    val name = call.parameters["name"]
                    val pass = call.parameters["pass"]
                    val id   = call.parameters["userId"]
                    if(id != null && name != null && pass != null) {
                        val userData = UserData(id.toInt(),name,pass)
                        if(userManage.authUser(userData)){
                            val taskId = taskManager.addTask(id.toInt(),request)
                            call.respond(request.copy(taskId = taskId))
                        }
                    }
                }
                post("task/{taskId}") {
                    val request = call.receive<TaskData>()

                    val name = call.parameters["name"]
                    val pass = call.parameters["pass"]
                    val id   = call.parameters["userId"]
                    if(id != null && name != null && pass != null) {
                        val userData = UserData(id.toInt(),name,pass)
                        if(userManage.authUser(userData)){
                            taskManager.editTask(id.toInt(),request)
                            call.respond(request)
                        }
                    }
                }
                delete("task/{taskId}") {
                    val name   = call.parameters["name"]
                    val pass   = call.parameters["pass"]
                    val userId = call.parameters["userId"]
                    val taskId = call.parameters["taskId"]
                    if((userId != null) && (name != null) && (pass != null) && (taskId != null)) {
                        val userData = UserData(userId.toInt(),name,pass)
                        if(userManage.authUser(userData)){
                            taskManager.deleteTask(userId.toInt(),taskId.toInt())
                        }
                    }
                }
            }
        }
    }
    //サーバーの実行
    server.start(wait = true)
}