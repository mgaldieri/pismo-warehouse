package me.mgaldieri.pismowarehouse.controllers

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.mgaldieri.pismowarehouse.*
import me.mgaldieri.pismowarehouse.dao.ProductDAO
import me.mgaldieri.pismowarehouse.dao.UserDAO
import me.mgaldieri.pismowarehouse.models.Product
import me.mgaldieri.pismowarehouse.models.User
import spark.Request
import spark.Response
import spark.Spark

object AdminController {
    fun loginAdmin(request: Request, response: Response) : String? {
        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val email = jsonBody.get("email")
            val password = jsonBody.get("password")

            if (email.isNull || password.isNull) {
                Spark.halt(403, abort(response, "Invalid credentials", 403))
            }

            val (data, successful) = UserDAO.getUserByEmail(email.asText())
            if (!successful) {
                Spark.halt(500, abort(response, "Error retrieving user", 500))
            }
            if (data == null) {
                Spark.halt(404, abort(response, "User not found", 404))
            }

            val user = data as User
            if (!user.checkPassword(password.asText())) {
                Spark.halt(402, abort(response, "Invalid password", 402))
            }

            val jwt = loginUser(user) ?: Spark.halt(500, abort(response, "Error logging user in", 500))

            return success(response, mapOf(
                    "jwt" to jwt
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }

    fun logoutAdmin(request: Request, response: Response) : String? {
        val user = getCurrentUser(request)
        if (user == null) Spark.halt(401, abort(response, "Unauthorized", 401))
        if (!logoutUser(user!!)) Spark.halt(401, abort(response, "Error logging user out", 401))

        return success(response, null)
    }

    fun getProducts(request: Request, response: Response) : String? {
        checkAuthorization(request, response)
        val (data, successful) = ProductDAO.getProducts()
        if (!successful) {
            Spark.halt(500, abort(response, "Error retrieving products", 500))
        }

        return success(response, mapOf(
                "products" to data
        ))
    }

    fun getProduct(request: Request, response: Response) : String? {
        checkAuthorization(request, response)
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

    fun putProduct(request: Request, response: Response) : String? {
        checkAuthorization(request, response)
        val mapper = jacksonObjectMapper()
        val jsonBody = mapper.readTree(request.bodyAsBytes())
        if (jsonBody == null) {
            Spark.halt(400, abort(response, "Missing request body", 400))
        }

        val name = jsonBody.get("name")
        val description = jsonBody.get("description")
        val priceCents = jsonBody.get("priceCents")
        val qty = jsonBody.get("qty")

        if (name.isNull || description.isNull || priceCents.isNull || qty.isNull) {
            Spark.halt(400, abort(response, "Missing product information", 400))
        }

        val product = Product(null, name.asText(), description.asText(), priceCents.asInt(), qty.asInt())
        if (product.save()) {
            return success(response, mapOf(
                    "product" to product
            ))
        } else {
            Spark.halt(500, abort(response, "Error saving product", 500))
            return null
        }
    }

    fun addProduct(request: Request, response: Response) : String? {
        checkAuthorization(request, response)
        val productId = request.params("productId")
        if (productId == null) {
            Spark.halt(400, abort(response, "Missing product id", 400))
        }

        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val qty = jsonBody.get("qty")

            if (qty.isNull) {
                Spark.halt(400, abort(response, "Missing quantity information", 400))
            }

            val (data, successful) = ProductDAO.getProductById(productId.toInt())
            if (!successful) {
                Spark.halt(500, abort(response, "Error retriving product", 500))
            }
            if (data == null) {
                Spark.halt(404, abort(response, "Product does not exist", 404))
            }

            val product: Product = data as Product
            product.qty += qty.asInt()

            if (!product.save()) {
                Spark.halt(500, abort(response, "Error saving product", 500))
            }

            return success(response, mapOf(
                    "product" to product
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }

    fun updateProduct(request: Request, response: Response) : String? {
        checkAuthorization(request, response)
        val productId = request.params("productId")
        if (productId == null) {
            Spark.halt(400, abort(response, "Missing product id", 400))
        }

        try {
            val mapper = jacksonObjectMapper()
            val jsonBody = mapper.readTree(request.bodyAsBytes())
            if (jsonBody == null) {
                Spark.halt(400, abort(response, "Missing request body", 400))
            }

            val name = jsonBody.get("name")
            val description = jsonBody.get("description")
            val priceCents = jsonBody.get("priceCents")
            val qty = jsonBody.get("qty")

            val (data, successful) = ProductDAO.getProductById(productId.toInt())
            if (!successful) {
                Spark.halt(500, abort(response, "Error retriving product", 500))
            }
            if (data == null) {
                Spark.halt(404, abort(response, "Product does not exist", 404))
            }

            val product: Product = data as Product
            if (name != null && !name.isNull) product.name = name.asText()
            if (description != null && !description.isNull) product.description = description.asText()
            if (priceCents != null && !priceCents.isNull) product.priceCents = priceCents.asInt()
            if (qty != null && !qty.isNull) product.qty = qty.asInt()

            if (!product.save()) {
                Spark.halt(500, abort(response, "Error saving product", 500))
            }

            return success(response, mapOf(
                    "product" to product
            ))
        } catch (e: JsonMappingException) {
            Spark.halt(400, abort(response, "Missing request body", 400))
            return ""
        }
    }
}