/*
*** File Name    :FriendTable.kt
*** Designer     :平出　達大
*** Date         :2022.06.21
*** Purpose      :フレンドTable作成
 */

/*
*** 2022.06.01 : fristcommit
*** 2022.06.21 : FriendTable修正
*** 2022.06.21 : 表題コメント追加
 */

package dsl
import org.jetbrains.exposed.sql.Table

object FriendTable : Table() {
    val UserId = integer("userId")
    val Friendname = text("friendname")
    override val primaryKey: PrimaryKey = PrimaryKey(UserId)
}
