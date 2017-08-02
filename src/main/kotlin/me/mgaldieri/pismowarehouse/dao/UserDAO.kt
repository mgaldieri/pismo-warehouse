package me.mgaldieri.pismowarehouse.dao

import me.mgaldieri.pismowarehouse.DBHelper
import me.mgaldieri.pismowarehouse.models.DBResult
import me.mgaldieri.pismowarehouse.models.User
import org.sql2o.Connection

object UserDAO {
    fun getUserByEmail(email: String) : DBResult {
        val result = DBResult(null, false)

        val query = "SELECT id, name, email, password FROM User WHERE email = :email"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return result
        val user: User? = conn.createQuery(query)
                .addParameter("email", email)
                .executeAndFetchFirst(User::class.java)

        result.data = user
        result.successful = true

        return result
    }
}