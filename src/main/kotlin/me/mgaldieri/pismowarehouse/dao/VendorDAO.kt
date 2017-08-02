package me.mgaldieri.pismowarehouse.dao

import me.mgaldieri.pismowarehouse.DBHelper
import me.mgaldieri.pismowarehouse.models.DBResult
import me.mgaldieri.pismowarehouse.models.Vendor
import org.sql2o.Connection
import spark.Request

object VendorDAO {
    fun getVendorByKey(key: String) : DBResult {
        val result = DBResult(null, false)

        val query = "SELECT id, name, token FROM Vendor"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return result
        val vendor: Vendor = conn.createQuery(query)
                .executeAndFetchFirst(Vendor::class.java)

        result.data = vendor
        result.successful = true

        return result
    }

    fun getCurrentVendor(request: Request) : Vendor? {
        val apiKey = request.headers("Authorization") ?: return null

        val (data, successful) = getVendorByKey(apiKey)
        if (!successful || data == null) return null

        return data as Vendor
    }
}