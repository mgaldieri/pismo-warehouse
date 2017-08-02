package me.mgaldieri.pismowarehouse.dao

import me.mgaldieri.pismowarehouse.DBHelper
import me.mgaldieri.pismowarehouse.models.DBResult
import me.mgaldieri.pismowarehouse.models.Product
import org.sql2o.Connection
import org.sql2o.Query
import org.sql2o.Sql2o

object ProductDAO {
    var db: Sql2o = DBHelper.getInstance()

    fun getProducts() : DBResult {
        val result = DBResult(null, false)

        val query = "SELECT id, name, description, price_cents, qty FROM Product"
        val conn: Connection = db.open() ?: return result
        val products: List<Product> = conn.createQuery(query)
                .addColumnMapping("price_cents", "priceCents")
                .executeAndFetch(Product::class.java)

        result.data = products
        result.successful = true

        return result
    }

    fun getFilteredProducts(ids: String) : DBResult {
        val result = DBResult(null, false)
        val idsList = ids.split(",")

        // Manually build IN clause, since sql2o does't support lists in prepared statements
        val queryList = mutableListOf<String>()
        for (idx in 0 until idsList.size) {
            val idTag = ":id_$idx"
            queryList.add(idTag)
        }
        val queryListString = queryList.joinToString()

        val query = "SELECT id, name, description, price_cents, qty FROM Product WHERE id IN ($queryListString)"
        val conn: Connection = db.open() ?: return result

        val preparedQuery = conn.createQuery(query)
        for (idx in 0 until idsList.size) {
            val tag = queryList.get(idx).trimStart(':')
            val id = idsList.get(idx)
            preparedQuery.addParameter(tag, id)
        }
        val products: List<Product> = preparedQuery
                .addColumnMapping("price_cents", "priceCents")
                .executeAndFetch(Product::class.java)

        result.data = products
        result.successful = true

        return result
    }

    fun getProductById(id: Int) : DBResult {
        val result = DBResult(null, false)

        val query = "SELECT id, name, description, price_cents, qty FROM Product WHERE id = :productId"
        val conn: Connection = db.open() ?: return result
        val product: Product? = conn.createQuery(query)
                .addParameter("productId", id)
                .addColumnMapping("price_cents", "priceCents")
                .executeAndFetchFirst(Product::class.java)

        result.data = product
        result.successful = true

        return result
    }

    fun searchProductsByNamePart(namePart: String) : DBResult {
        val result = DBResult(null, false)
        val query = "SELECT id, name, description, price_cents, qty FROM Product WHERE name LIKE :namePart"
        val conn: Connection = db.open() ?: return result
        val products: List<Product> = conn.createQuery(query)
                .addParameter("namePart", "%$namePart%".toLowerCase())
                .addColumnMapping("price_cents", "priceCents")
                .executeAndFetch(Product::class.java)

        result.data = products
        result.successful = true

        return result
    }
}