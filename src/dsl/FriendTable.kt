package dsl

import org.jetbrains.exposed.sql.Table

object FriendTable : Table() {
    val UserId = integer("userId")
    val Friendname = text("friendname")
    override val primaryKey: PrimaryKey = PrimaryKey(UserId)
}
