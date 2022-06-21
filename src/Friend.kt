/*
*** File Name    :Friend.kt
*** Designer     :平出　達大
*** Date         :2022.06.21
*** Purpose      :フレンド関係の処理
 */

/*
*** 2022.06.01 : fristcommit
*** 2022.06.21 : friendlist修正
*** 2022.06.21 : addfriend追加
*** 2022.06.21 : deletefriend追加
*** 2022.06.21 : 表題コメント追加
 */

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import dsl.User
import dsl.FriendTable

class Friend () {
    fun friendsearch(userId: Int): String{ //フレンド検索
        var friendname = ""
        transaction {
             friendname = User.select { User.id eq userId }.single()[User.name]
        }
        return friendname
    }

    fun friendlist(userId: Int): String { //フレンド表示
        var frinedname = "";
        transaction {
            frinedname = FriendTable.select { FriendTable.UserId eq userId }.single()[FriendTable.Friendname]
        }
        return frinedname;
    }

    fun addfrined(userId: Int, friendname : String){ //フレンド追加

        transaction {
            FriendTable.insert{
                it[UserId] = userId
                it[Friendname] = friendname
            }
        }
    }

    fun deletefriend(userId: Int, friendname : String){ //フレンド消去
        transaction {
            FriendTable.deleteWhere { (FriendTable.UserId eq userId) and (FriendTable.Friendname eq friendname)}
        }
    }
}
