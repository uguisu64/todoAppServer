import io.ktor.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import dsl.User

class Friend () {
    fun friendsearch(Usersid: Int){
        transaction {
            val friendid = User.select { User.id eq Usersid }.single()[User.name]
        }
    }

    fun friendlist(Usersid: Int,Username: String) {
        transaction {
            val friendid = User.select { User.id eq Usersid }.single()[User.id]
        }
    }
}