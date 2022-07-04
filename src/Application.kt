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
*** 2022.07.03 : 仕様変更修正
*** 2022.07.04 : フレンド申請処理修正
*** 2022.07.05 : ほぼ完成
*/

import dataclass.FriendTaskData
import dataclass.TaskData
import dataclass.UserData
import dsl.FriendApplyTable
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
        SchemaUtils.create(FriendApplyTable)
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
                println("POST /user")
                try {
                    val request = call.receive<UserData>()

                    val userId = userManage.createUser(request.name,request.pass)
                    call.respond(request.copy(id = userId))
                }
                catch (e: Exception) {
                    println(e)
                    call.respond(HttpStatusCode.BadRequest,failed)
                }
            }
            post("/auth") {
                println("POST /auth")
                try {
                    val request = call.receive<UserData>()

                    if(userManage.authUser(request)) {
                        call.respondText(success)
                    }
                    else {
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                catch (e: Exception) {
                    println(e)
                    call.respond(HttpStatusCode.BadRequest,failed)
                }
            }
            route("/user/{userId}") {
                //タスク関連の処理
                get("task") {
                    println("GET /user/{userId}/task")
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("task") {
                    println("POST /user/{userId}/task")
                    try {
                        val request = call.receive<TaskData>()

                        val name = call.parameters["name"]
                        val pass = call.parameters["pass"]
                        val id   = call.parameters["userId"]
                        if(id != null && name != null && pass != null) {
                            val userData = UserData(id.toInt(),name,pass)
                            if(userManage.authUser(userData) && userData.id == request.userId){
                                taskManager.addTask(request)
                                call.respondText(success)
                            }
                            else{
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                    }
                    catch (e: Exception) {
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("task/{taskId}") {
                    println("POST /user/{userId}/task/{taskId}")
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                delete("task/{taskId}") {
                    println("DELETE /user/{userId}/task/{taskId}")
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }

                //フレンド関連の処理
                get("friend") {
                    println("GET /user/{userId}/friend")
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
                                    val data = userManage.userData(it.Friendid)
                                    friends.add(data.copy(pass = ""))
                                }
                                call.respond(friends)
                            }
                            else {
                                call.respond(HttpStatusCode.BadRequest,failed)
                            }
                        }
                    }
                    catch (e: Exception) {
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                get("friend/search") {
                    println("GET /user/{userId}/friend/search")
                    try {
                        val name     = call.parameters["name"]
                        val pass     = call.parameters["pass"]
                        val userid   = call.parameters["userId"]
                        val friendId = call.parameters["friendId"]
                        if((userid != null) && (name != null) && (pass != null) && (friendId != null)) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                val friendData = userManage.userData(friendId.toInt())
                                call.respond(friendData.copy(pass = ""))
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("friend/search") {
                    println("POST /user/{userId}/friend/search")
                    try {
                        val name     = call.parameters["name"]
                        val pass     = call.parameters["pass"]
                        val userid   = call.parameters["userId"]
                        val friendId = call.parameters["friendId"]
                        if((userid != null) && (name != null) && (pass != null) && (friendId != null)) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                if(friendManage.FriendApply(userData.id,friendId.toInt())){
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
                        else {
                            call.respond(HttpStatusCode.BadRequest,failed)
                        }
                    }
                    catch (e: Exception) {
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                get("friend/accept") {
                    println("GET /user/{userId}/friend/accept")
                    try {
                        val name   = call.parameters["name"]
                        val pass   = call.parameters["pass"]
                        val userid = call.parameters["userId"]
                        println(name)
                        if(userid != null && name != null && pass != null) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                val friendApplyData = friendManage.friendapplymenu(userData.id)
                                val userDataList = mutableListOf<UserData>()
                                friendApplyData.forEach { userDataList.add(userManage.userData(it.Friendid).copy(pass = "")) }
                                call.respond(userDataList)
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                post("friend/accept") {
                    println("POST /user/{userId}/friend/accept")
                    try {
                        val name     = call.parameters["name"]
                        val pass     = call.parameters["pass"]
                        val userid   = call.parameters["userId"]
                        val friendId = call.parameters["friendId"]
                        val reject   = call.parameters["reject"]
                        if((userid != null) && (name != null) && (pass != null) && (friendId != null)) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                when (reject) {
                                    null -> {
                                        friendManage.apply(userData.id,friendId.toInt())
                                        call.respondText(success)
                                    }
                                    "true" -> {
                                        friendManage.disapproval(userData.id,friendId.toInt())
                                        call.respondText(success)
                                    }
                                    else -> {
                                        call.respondText(failed)
                                    }
                                }
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
                get("/friend/task") {
                    println("GET /user/{userId}/friend/task")
                    try {
                        val name   = call.parameters["name"]
                        val pass   = call.parameters["pass"]
                        val userid = call.parameters["userId"]
                        if(userid != null && name != null && pass != null) {
                            val userData = UserData(userid.toInt(),name,pass)
                            if(userManage.authUser(userData)){
                                val friendTask = mutableListOf<FriendTaskData>()
                                val friendList = mutableListOf<UserData>()
                                friendManage.friendlist(userData.id).forEach {
                                    friendList.add(userManage.userData(it.Friendid))
                                }
                                friendList.forEach {
                                    taskManager.allTask(it.id).forEach { task ->
                                        friendTask.add(FriendTaskData(
                                            taskId = task.taskId,
                                            userId = task.userId,
                                            userName = it.name,
                                            name = task.name,
                                            deadLine = task.deadLine,
                                            priority = task.priority,
                                            share = task.share,
                                            tag = task.tag
                                        ))
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
                        println(e)
                        call.respond(HttpStatusCode.BadRequest,failed)
                    }
                }
            }
        }
    }
    //サーバーの実行
    server.start(wait = true)
}