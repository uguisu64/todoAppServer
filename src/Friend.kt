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
*** 2022.06.28 : フレンド申請関連追加
 */

import dataclass.FriendApplyData
import dataclass.FriendData
import dsl.FriendApplyTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import dsl.User
import dsl.FriendTable

class Friend () {
    fun friendsearch(userId: Int): Int{ //フレンド検索
        var friendid = 0
        transaction {
             friendid = User.select { User.id eq userId }.single()[User.id]
        }
        return friendid
    }

    fun friendlist(userId: Int): MutableList<FriendData> { //フレンド表示

        val frinedid = mutableListOf<FriendData>()
        transaction {
            FriendTable.select { FriendTable.UserId eq userId }.forEach {
                val friendId = FriendData(
                    Friendid = it[FriendTable.Friendid]
                )
            frinedid.add(friendId)
            }
        }
        return frinedid;
    }

    fun addfrined(userId: Int, friendid : Int){ //フレンド追加

        transaction {
            FriendTable.insert{
                it[UserId] = userId
                it[Friendid] = friendid
            }
        }
    }

    fun deletefriend(userId: Int, friendid : Int){ //フレンド消去
        transaction {
            FriendTable.deleteWhere { (FriendTable.UserId eq userId) and (FriendTable.Friendid eq friendid)}
        }
    }

    fun friendapplymenu(Myid : Int) : MutableList<FriendApplyData>{ //フレンド申請画面
        var applys = mutableListOf<FriendApplyData>()
        transaction {
            FriendApplyTable.select { FriendApplyTable.Myid eq Myid }.forEach{
                val apply = FriendApplyData(
                    Myid = it[FriendApplyTable.Myid],
                    Friendid = it[FriendApplyTable.Friendid]
                )
                applys.add(apply)
            }

        }

        return applys;
    }


    //相手のidを入れる。 table
    //OK だったらtableに入れる
    fun apply(myid : Int, friendid : Int){  //申請許可
        transaction {
            val friendId = FriendTable.select{ (FriendApplyTable.Myid eq myid) }.single()[FriendApplyTable.Friendid]
            if(friendid == friendId){  //FriendApplyTableにあったら実行
                FriendTable.insert { //FriendTableに書き込む
                    it[UserId] = myid
                    it[Friendid] = friendId
                }
            }

            FriendApplyTable.insert {  //フレンド申請の許可、不許可に使う
                it[Myid] = myid
                it[Friendid] = friendId
            }
            FriendApplyTable.deleteWhere { (FriendApplyTable.Myid eq myid) and (FriendApplyTable.Friendid eq friendid)}
        }// FriendApplyTableからFriendTableに書き込んだ情報を消去

    }

    fun disapproval(myid : Int, friendid : Int){  // 申請不許可
        transaction {
            FriendApplyTable.deleteWhere { (FriendApplyTable.Myid eq myid) and (FriendApplyTable.Friendid eq friendid)}
        }  //FriendApplyTableから不許可した情報を消去
    }
}
