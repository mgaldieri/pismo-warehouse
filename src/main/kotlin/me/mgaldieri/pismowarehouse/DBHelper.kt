package me.mgaldieri.pismowarehouse

import org.sql2o.Sql2o

object DBHelper {
    private val db: Sql2o = Sql2o("jdbc:h2:mem:warehouse", null, null)

    fun getInstance() : Sql2o {
        return db
    }
}