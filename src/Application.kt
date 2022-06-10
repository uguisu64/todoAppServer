import dataclass.TaskData
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

fun main() {
    //sqliteのDataBaseファイルのパス
    val dbPath = "jdbc:sqlite:C:\\src\\sqlite3\\todoAppDB.db"

    //sqliteへの接続
    Database.connect(dbPath,"org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    val server = embeddedServer(Netty, port = 8080) {
        routing {
            route("/user/{id}") {

            }
        }
    }
    server.start(wait = true)
}