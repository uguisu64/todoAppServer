package dsl

import org.jetbrains.exposed.sql.Table

object User : Table() {
    val id   = integer("id").autoIncrement()
    val name = text("name")
    val pass = text("pass")
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
