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

    val taskManager = Task()
    val userManage  = UserManage()

    val server = embeddedServer(Netty, port = args[0].toInt()) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            post("/user") {
                val name = call.parameters["name"]
                val pass = call.parameters["pass"]
                if(name != null && pass != null) {
                    val userId = userManage.createUser(name,pass)
                    call.respond(UserData(userId,name,pass))
                }
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
    server.start(wait = true)
}