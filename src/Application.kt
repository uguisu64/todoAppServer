/*
*** File Name           : Application.kt
*** Designer            : 加藤　颯真
*** Date                : 2022.06.21
*** Purpose             : main関数内で、サーバーに必要なクラスのインスタンス化とサーバーの設定と実行を行う
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
*** 2022.06.21 : テーブル仕様変更
*** 2022.06.28 : サーバー応答変更
*** 2022.07.02 : フレンドリクエスト処理追加
*** 2022.07.02 : フレンドリクエスト処理修正
*/

import dataclass.TaskData
import dataclass.UserData
import dsl.FriendTable
import dsl.TaskTable
import dsl.User
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.Executable
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

    //テーブルの作成(テーブルがない場合)
    transaction {
        SchemaUtils.create(User)
        SchemaUtils.create(TaskTable)
        SchemaUtils.create(FriendTable)
    }

    //各クラスのインスタンス化
    val taskManager  = Task()
    val userManage   = UserManage()
    val friendManage = Friend()

    val success = "success"
    val failed  = "failed"

    //サーバーの設定
    val server = embeddedServer(Netty, port = args[0].toInt()) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            post("/user") {
                try {
                    val request = call.receive<UserData>()

                    userManage.createUser(request.name,request.pass)
                    call.respondText(success)
                }
                catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest,failed)
                }
            }
            route("/user/{userId}") {
                //タスク関連の処理
                get("task") {
                    try {
                        val name = call.parameters["name"]
                        val pass = call.parameters["pass"]
                        val id   = call.parameters["userId"]
                        if(id != null && name != null && pass != null){
                            val userData = UserData(id.toInt(),name,pass)
                            if(userManage.authUser(userData) && userData.id == id.toInt()){
                                call.respond(taskManager.allTask(id.toInt()))
                            }
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("task") {
                    try {
                        val request = call.receive<TaskData>()

                        val name = call.parameters["name"]
                        val pass = call.parameters["pass"]
                        val id   = call.parameters["userId"]
                        if(id != null && name != null && pass != null) {
                            val userData = UserData(id.toInt(),name,pass)
                            if(userManage.authUser(userData) && userData.id == id.toInt()){
                                taskManager.addTask(request)
                                call.respondText(success)
                            }
                            else{
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("task/{taskId}") {
                    try {
                        val request = call.receive<TaskData>()

                        val name = call.parameters["name"]
                        val pass = call.parameters["pass"]
                        val id   = call.parameters["userId"]
                        if(id != null && name != null && pass != null) {
                            val userData = UserData(id.toInt(),name,pass)
                            if(userManage.authUser(userData) && userData.id == id.toInt()){
                                taskManager.editTask(request)
                                call.respond(request)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                delete("task/{taskId}") {
                    try {
                        val name   = call.parameters["name"]
                        val pass   = call.parameters["pass"]
                        val userId = call.parameters["userId"]
                        val taskId = call.parameters["taskId"]
                        if((userId != null) && (name != null) && (pass != null) && (taskId != null)) {
                            val userData = UserData(userId.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                taskManager.deleteTask(userId.toInt(),taskId.toInt())
                                call.respondText(success)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }

                //フレンド関連の処理
                get("friend") {
                    try {
                        val friends = mutableListOf<UserData>()

                        val name = call.parameters["name"]
                        val pass = call.parameters["pass"]
                        val id   = call.parameters["userId"]
                        if(id != null && name != null && pass != null) {
                            val userData = UserData(id.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                val friendList = friendManage.friendlist(id.toInt())
                                friendList.forEach {
                                    friends.add(userManage.userData(it.Friendid))
                                }
                                call.respond(friends)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                get("friend/search") {
                    try {
                        val name     = call.parameters["name"]
                        val pass     = call.parameters["pass"]
                        val userid   = call.parameters["userId"]
                        val friendId = call.parameters["friendId"]
                        if((userid != null) && (name != null) && (pass != null) && (friendId != null)) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                val friendData = userManage.userData(friendId.toInt())
                                call.respond(mapOf("id" to friendData.id, "name" to friendData.name))
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest,failed)
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("friend/search") {
                    try {
                        val name     = call.parameters["name"]
                        val pass     = call.parameters["pass"]
                        val userid   = call.parameters["userId"]
                        val friendId = call.parameters["friendId"]
                        if((userid != null) && (name != null) && (pass != null) && (friendId != null)) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                friendManage.addfrined(userData.id, friendId.toInt())
                                call.respondText(success)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest,failed)
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                get("friend/accept") {
                    try {
                        val name   = call.parameters["name"]
                        val pass   = call.parameters["pass"]
                        val userid = call.parameters["userId"]
                        if(userid != null && name != null && pass != null) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                call.respond(friendManage.friendapplymenu(userData.id))
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest,failed)
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("friend/accept/{friendId}") {
                    try {
                        val name     = call.parameters["name"]
                        val pass     = call.parameters["pass"]
                        val userid   = call.parameters["userId"]
                        val friendId = call.parameters["friendId"]
                        if((userid != null) && (name != null) && (pass != null) && (friendId != null)) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                friendManage.apply(userData.id,friendId.toInt())
                                call.respondText(success)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest,failed)
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                get("/friend/task") {
                    try {
                        val name   = call.parameters["name"]
                        val pass   = call.parameters["pass"]
                        val userid = call.parameters["userId"]
                        if(userid != null && name != null && pass != null) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                val friendTask = mutableListOf<TaskData>()
                                val friendList = mutableListOf<UserData>()
                                friendManage.friendlist(userData.id).forEach {
                                    friendList.add(userManage.userData(it.Friendid))
                                }
                                friendList.forEach {
                                    taskManager.allTask(it.id).forEach { friendtask ->
                                        friendTask.add(friendtask)
                                    }
                                }
                                call.respond(friendTask)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                        else {
                            call.respond(HttpStatusCode.BadRequest,failed)
                        }
                    }
                    catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
            }
        }
    }
    //サーバーの実行
    server.start(wait = true)
}