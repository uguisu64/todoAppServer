package dsl
import org.jetbrains.exposed.sql.Table

object FriendApplyTable : Table() {
    val Myid = integer("Myid")
    val Friendid = integer("Friendid")
}
