package me.mgaldieri.pismowarehouse.models

import me.mgaldieri.pismowarehouse.DBHelper
import org.sql2o.Connection

data class Session(val sessionId: String, val userId: Int) {
    fun save() : Boolean {
        val query = "INSERT INTO Session (session_id, user_id) VALUES (:sessionId, :userId)"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return false
        conn.createQuery(query)
                .addParameter("sessionId", this.sessionId)
                .addParameter("userId", this.userId)
                .executeUpdate()

        return true
    }
}