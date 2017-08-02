package me.mgaldieri.pismowarehouse.dao

import me.mgaldieri.pismowarehouse.DBHelper
import me.mgaldieri.pismowarehouse.models.DBResult
import org.sql2o.Connection

object TransactionDAO {
    fun addTransaction(productId: Int, vendorId: Int, paymentToken: String) : DBResult {
        val result = DBResult(null, false)

        val query = "INSERT INTO Transaction (product_id, vendor_id, payment_token) VALUES (:productId, :vendorId, :paymentToken)"
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: return result
        conn.createQuery(query)
                .addParameter("productId", productId)
                .addParameter("vendorId", vendorId)
                .addParameter("paymentToken" ,paymentToken)
                .executeUpdate()

        result.successful = true

        return result
    }
}