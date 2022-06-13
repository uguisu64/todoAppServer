import dataclass.UserData
import dsl.User
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserManage {
    fun authUser(userData: UserData): Boolean {
        var auth = false
        transaction {
            val record =  User.select { User.id eq userData.id }.single()
            if(record[User.name] == userData.name && record[User.pass] == userData.pass){
                auth = true
            }
        }

        return auth
    }

    fun userData(userID : Int): UserData {
        lateinit var record: ResultRow
        transaction {
            record = User.select { User.id eq userID }.single()
        }
        return UserData(record[User.id],record[User.name],record[User.pass])
    }

    fun createUser(name : String, pass : String): Int {
        var userId = 0
        transaction {
            val record = User.insert {
                it[User.name] = name
                it[User.pass] = pass
            }
            userId = record[User.id]
        }
        return userId
    }
}