import io.ktor.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import dsl.User
import dsl.Friend

class Friend () {
    fun friendsearch(Usersid: Int){
        return transaction {
            val friendname = User.select { User.id eq Usersid }.single()[User.name]
        }
    }

    fun friendlist(num: Int) {
        return transaction {
            val friendid = User.select { dsl.Friend.num eq num }.single()[dsl.Friend.name]
        }
    }
}
