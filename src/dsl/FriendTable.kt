package dsl

import org.jetbrains.exposed.sql.Table

object Friend : Table() {
    val num = integer("num")
    val name = text("name")
    override val primaryKey: PrimaryKey = PrimaryKey(num)
}
