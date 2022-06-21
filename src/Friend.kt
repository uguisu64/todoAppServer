import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import dsl.User
import dsl.FriendTable

class Friend () {
    fun friendsearch(userId: Int): String{
        var friendname = ""
        transaction {
             friendname = User.select { User.id eq userId }.single()[User.name]
        }
        return friendname
    }

    fun friendlist(userId: Int): String {
        var frinedname = "";
        transaction {
            frinedname = FriendTable.select { FriendTable.UserId eq userId }.single()[FriendTable.Friendname] //numは変える
        }
        return frinedname;
    }

    fun addfrined(userId: Int, friendname : String){

        transaction {
            FriendTable.insert{
                it[UserId] = userId
                it[Friendname] = friendname
            }
        }
    }

    fun deletefriend(userId: Int, friendname : String){
        transaction {
            FriendTable.deleteWhere { (FriendTable.UserId eq userId) and (FriendTable.Friendname eq friendname)}
        }
    }
}
