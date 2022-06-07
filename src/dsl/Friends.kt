package dsl

import org.jetbrains.exposed.sql.Table

class Friends(id:Int): Table("FriendTable$id") {
    val friendId = integer("friendId")
}