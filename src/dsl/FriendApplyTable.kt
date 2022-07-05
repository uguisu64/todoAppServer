/*
*** File Name    :FriendTable.kt
*** Designer     :平出　達大
*** Date         :2022.06.29
*** Purpose      :フレンドTable作成
 */

/*commit History
*** 2022.06.01 : fristcommit
*** 2022.06.21 : FriendApplyTable修正
*** 2022.06.21 : 表題コメント追加
 */

package dsl
import org.jetbrains.exposed.sql.Table

object FriendApplyTable : Table() {
    val Myid = integer("Myid")
    val Friendid = integer("Friendid")
}
