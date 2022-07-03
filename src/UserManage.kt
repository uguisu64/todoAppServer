/*
*** File Name           : UserManage.kt
*** Designer            : 加藤　颯真
*** Date                : 2022.06.21
*** Purpose             : ユーザー認証や登録等の、ユーザーテーブルに関わる処理を行うクラス
*/
/*  Commit History
*** 2022.06.13 タスク関連追加
*** 2022.06.13 ユーザー追加
*** 2022.06.21 テーブル仕様変更
*** 2022.07.03 : 仕様変更修正
 */

import dataclass.UserData
import dsl.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserManage {
    /*
    関数名: authUser
    引数　: userData : UserData
    返り値: Boolean
    動作　: DBのUserテーブルにアクセスしてUserDataのユーザーのデータが正しいかを返す。正しければtrueを返す。
    作成者: 加藤　颯真
     */
    fun authUser(userData: UserData) : Boolean {
        var auth = false
        transaction {
            val record =  User.select { User.id eq userData.id }.single()
            if(record[User.name] == userData.name && record[User.pass] == userData.pass){
                auth = true
            }
        }

        return auth
    }

    /*
    関数名: userData
    引数　: userId: Int
    返り値: UserData
    動作　: DBのUserテーブルにアクセスし、userIdのUserDataを返す
    作成者: 加藤　颯真
     */
    fun userData(userId: Int) : UserData {
        lateinit var record: ResultRow
        transaction {
            record = User.select { User.id eq userId }.single()
        }
        return UserData(record[User.id],record[User.name],record[User.pass])
    }

    /*
    関数名: createUser
    引数　: name : String, pass : String
    返り値: Int
    動作　: name,passを受け取り、DBのUserテーブルにアクセスし、新しいユーザーを登録する
    作成者: 加藤　颯真
     */
    fun createUser(name: String, pass: String) : Int {
        var userId = 0
        transaction {
            userId = User.insert {
                it[User.name] = name
                it[User.pass] = pass
            } get User.id
        }
        return userId
    }
}