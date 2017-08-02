package me.mgaldieri.pismowarehouse.models

import me.mgaldieri.pismowarehouse.DBHelper
import org.sql2o.Connection

data class Product(var id : Long?, var name : String, var description : String, var priceCents : Int, var qty: Int) {
    fun save() : Boolean {
        if (id == null) {
            val query = "INSERT INTO Product (name, description, price_cents, qty) VALUES (:name, :description, :priceCents, :qty)"
            val db = DBHelper.getInstance()
            val conn: Connection = db.open() ?: return false
            this.id = conn.createQuery(query, true)
                    .addParameter("name", name)
                    .addParameter("description", description)
                    .addParameter("priceCents", priceCents)
                    .addParameter("qty", qty)
                    .executeUpdate()
                    .key as Long
        } else {
            val query = "UPDATE Product SET name = :name, description = :description, price_cents = :priceCents, qty = :qty WHERE id = :id"
            val db = DBHelper.getInstance()
            val conn: Connection = db.open() ?: return false
            conn.createQuery(query)
                    .addParameter("name", name)
                    .addParameter("description", description)
                    .addParameter("priceCents", priceCents)
                    .addParameter("qty", qty)
                    .addParameter("id", id ?: -1)
                    .executeUpdate()
        }
        return true
    }
}