import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class Friend () {

    companion object FriendList {
        fun friendsearch(Usersid: Int): String{
            Database.connect("url","driver","user","password")
            transaction {
                val friendid = Userinfo.select { Userinfo.id eq Usersid }.single()[Userinfo.name]
                println(friendid)
            }
            return "s"
        }

        fun friendlist(Usersid: Int,Username: String) {
            Database.connect("url","driver","user","password")
            transaction {
                val friendid = Userinfo.select { Userinfo.id eq Usersid }.single()[Userinfo.id]
                
            }
        }

    }
}
