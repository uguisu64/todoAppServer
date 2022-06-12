import dataclass.TaskData
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun main() {
    //sqliteのDataBaseファイルのパス
    val dbPath = "jdbc:sqlite:C:\\src\\sqlite3\\todoAppDB.db"

    //sqliteへの接続
    Database.connect(dbPath,"org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    val taskManager = Task()

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            route("/user/{id}") {
                get("/task") {
                    val id = call.parameters["id"]
                    if(id != null){
                        call.respond(taskManager.allTask(id.toInt()))
                    }
                }
            }
        }
    }
    server.start(wait = true)
}