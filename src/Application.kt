import dataclass.TaskData
import dataclass.UserData
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.cio.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kotlin.reflect.jvm.internal.impl.load.java.JavaClassFinder.Request

fun main() {
    //sqliteのDataBaseファイルのパス
    val dbPath = "jdbc:sqlite:C:\\src\\sqlite3\\todoAppDB.db"

    //sqliteへの接続
    Database.connect(dbPath,"org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    val taskManager = Task()
    val userManage  = UserManage()

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
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
            }
        }
    }
    server.start(wait = true)
}