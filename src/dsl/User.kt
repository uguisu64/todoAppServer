package dsl

import org.jetbrains.exposed.sql.Table

object User : Table() {
    val id = integer("id")
    val name = text("name")
    val pass = text("pass")
    val num = integer("num")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
