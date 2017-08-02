package me.mgaldieri.pismowarehouse.controllers

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.mgaldieri.pismowarehouse.abort
import me.mgaldieri.pismowarehouse.dao.ProductDAO
import me.mgaldieri.pismowarehouse.dao.TransactionDAO
import me.mgaldieri.pismowarehouse.dao.VendorDAO
import me.mgaldieri.pismowarehouse.models.Product
import me.mgaldieri.pismowarehouse.models.Transaction
import me.mgaldieri.pismowarehouse.success
import spark.Request
import spark.Response
import spark.Spark

object VendorController {
    fun getProducts(request: Request, response: Response) : String? {
        val data: List<*>
        val successful: Boolean

        val idFilter = request.queryParams("filter")
        if (idFilter != null) {
            val (d, s) = ProductDAO.getFilteredProducts(idFilter)
            data = d as List<*>
            successful = s
        } else {
            val (d, s) = ProductDAO.getProducts()
            data = d as List<*>
            successful = s
        }
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving products", 500))
        }

        return success(response, mapOf(
                "products" to data
        ))
    }

    fun getProduct(request: Request, response: Response) : String? {
        val productId = request.params("productId")
        if (productId == null) {
            Spark.halt(400, abort(response, "Missing product id in request", 400))
        }

        val (data, successful) = ProductDAO.getProductById(productId.toInt())
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving product", 500))
        }
        if (data == null) {
            Spark.halt(404, abort(response, "Product does not exist", 404))
        }

        return success(response, mapOf(
                "product" to data
        ))
    }

    fun getProductQty(request: Request, response: Response) : String? {
        val productId = request.params("productId")
        if (productId == null) {
            Spark.halt(400, abort(response, "Missing product id in request", 400))
        }

        val (data, successful) = ProductDAO.getProductById(productId.toInt())
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving product", 500))
        }
        if (data == null) {
            Spark.halt(404, abort(response, "Product does not exist", 404))
        }

        val product = data as Product
        return success(response, mapOf(
                "qty" to product.qty
        ))
    }

    fun searchProduct(request: Request, response: Response) : String? {
        val term = request.queryParams("term")
        if (term == null) {
            Spark.halt(400, abort(response, "Missing search term in request", 400))
        }

        val (data, successful) = ProductDAO.searchProductsByNamePart(term)
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving product", 500))
        }

        return success(response, mapOf(
                "products" to data
        ))
    }

    fun sellProduct(request: Request, response: Response) : String? {
        val vendor = VendorDAO.getCurrentVendor(request)
        if (vendor == null) Spark.halt(404, abort(response, "Vendor does not exists", 404))

        val mapper = jacksonObjectMapper()
        val jsonBody: JsonNode
        try {
            jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }

        val paymentToken = jsonBody.get("paymentToken")
        val products = jsonBody.get("products")
        if (paymentToken.isNull or products.isNull) {
            Spark.halt(400, abort(response, "Missing payment info in request", 400))
        }

        for (p in products.asIterable()) {
            if (p == null) Spark.halt(400, abort(response, "Missing product info in request", 400))
            val productId = p.get("productId")
            val qty = p.get("qty")

            if (productId == null || qty == null) {
                Spark.halt(400, abort(response, "Missing product info in request", 400))
            }

            val (data, successful) = ProductDAO.getProductById(productId.asInt())
            if (!successful) {
                Spark.halt(500, abort(response, "Error retrieving product", 500))
            }
            if (data == null) {
                Spark.halt(404, abort(response, "Product does not exist", 404))
            }

            val product = data as Product
            if (product.qty < qty.asInt()) {
                Spark.halt(406, abort(response, "Not enough product in stock", 406))
            }

            product.qty -= qty.asInt()

            if (!product.save()) {
                Spark.halt(500, abort(response, "Error saving product", 500))
            }

            TransactionDAO.addTransaction(productId.asInt(), vendor!!.id, paymentToken.asText())
        }

        return success(response, null)
    }
}