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
*** 2022.07.03 : フレンド申請関連修正
*** 2022.07.03 : フレンド申請画面追加
*** 2022.07.03 : 仕様変更修正
*** 2022.07.04 : FriendApply修正
*** 2022.07.05 : ほぼ完成
 */

import com.typesafe.config.ConfigException.Null
import dataclass.FriendApplyData
import dataclass.FriendData
import dsl.FriendApplyTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import dsl.User
import dsl.FriendTable

class Friend () {
    /*
    関数名: friendsearch
    引数　: userId : Int
    返り値: Int
    動作　: 受け取ったユーザIDからUsertableでフレンドを探す。
    作成者: 平出　達大
    */
    fun friendsearch(userId: Int): Int{ //フレンド検索
        var friendid = 0
        transaction {
             friendid = User.select { User.id eq userId }.single()[User.id]
        }
        return friendid
    }
    /*
    関数名: friendList
    引数　: userid : Int
    返り値: MutableList<FriendData>
    動作　: 受け取ったユーザIDのフレンドをリストにして返す。
    作成者: 平出　達大
    */
    fun friendlist(userId : Int): MutableList<FriendData> { //フレンド表示

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

    /*
    関数名: addfriend
    引数　: userId : Int, friendid : Int
    返り値: なし
    動作　: UserIdとfriendidを受け取り、FriendTableに格納する
    作成者: 平出　達大
    */

    fun addfrined(userId: Int, friendid : Int){ //フレンド追加

        transaction {
            FriendTable.insert{
                it[UserId] = userId
                it[Friendid] = friendid
            }
        }
    }

    /*
    関数名: deletefriend
    引数　: userId : Int, friendid : Int
    返り値: なし
    動作　: UserIdとfriendidを受け取り、FriendTableから消去する
    作成者: 平出　達大
    */

    fun deletefriend(userId: Int, friendid : Int){ //フレンド消去
        transaction {
            FriendTable.deleteWhere { (FriendTable.UserId eq userId) and (FriendTable.Friendid eq friendid)}
        }
    }

    /*
    関数名: friendapplymenu
    引数　: MyId : Int
    返り値: MutableList<FriendApplyData>
    動作　: MyIdを受け取り、フレンド申請されたユーザのレコードをリストにして返す
    作成者: 平出　達大
    */

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

    /*
    関数名: friendApply
    引数　: myid : Int, friendId : Int
    返り値: comf : Boolean
    動作　: myIdとfriendIdを受け取り、FriendApplyTableに格納する。自分自身と既に申請した人は申請できないようにする。
    作成者: 平出　達大
    */

    fun FriendApply(myid : Int, friendId: Int) : Boolean{ //フレンド申請
        var comf = false
        transaction {
            User.select{ User.id eq myid }.single()
            User.select{ User.id eq friendId }.single()

            if((myid != friendId)) {
                comf = true
                FriendApplyTable.insert {
                    it[Myid] = friendId
                    it[Friendid] = myid
                }
            }
        }
        return comf
    }


    /*
    関数名: apply
    引数　: myid : Int, friendid : Int
    返り値: なし
    動作　: myIdとfriendIdを受け取り、FriendApplyTableを検索して一致したらFriendTableに格納する
    作成者: 平出　達大
    */

    //相手のidを入れる。 FriendApplytable
    //OK だったらFriendtableに入れる
    fun apply(myid : Int, friendid : Int){  //申請許可
        transaction {
            val record = FriendApplyTable.select{ (FriendApplyTable.Myid eq myid) and (FriendApplyTable.Friendid eq friendid)}.single()
            if(record[FriendApplyTable.Myid] == myid && record[FriendApplyTable.Friendid] == friendid){
                FriendTable.insert { //FriendTableに書き込む
                    it[UserId] = myid
                    it[Friendid] = friendid
                }
                FriendTable.insert {
                    it[Friendid] = myid
                    it[UserId] = friendid
                }
            }
            FriendApplyTable.deleteWhere { (FriendApplyTable.Myid eq myid) and (FriendApplyTable.Friendid eq friendid)}
        }
    }// FriendApplyTableから申請を行ったレコードを消去する

    /*
    関数名: disapproval
    引数　: myid : Int, friendId : Int
    返り値: なし
    動作　: myIdとfriendIdを受け取り、FriendApplyTableからレコードを消去する
    作成者: 平出　達大
    */

    fun disapproval(myid : Int, friendid : Int){  // 申請拒否
        transaction {
            FriendApplyTable.deleteWhere { (FriendApplyTable.Myid eq myid) and (FriendApplyTable.Friendid eq friendid)}
        }  //FriendApplyTableから不許可した情報を消去
    }
}
