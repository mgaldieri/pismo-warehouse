package me.mgaldieri.pismowarehouse

import me.mgaldieri.pismowarehouse.controllers.AdminController
import me.mgaldieri.pismowarehouse.controllers.VendorController
import me.mgaldieri.pismowarehouse.dao.VendorDAO
import spark.Spark.*

fun main(args: Array<String>) {
    val port = 8000

    port(port)
    initDB()
    println("Servidor Warehouse iniciado em 127.0.0.1:$port")

    // Ping endpoint
    get("/ping") { req, res ->
        "Alive"
    }

    // ADMIN ENDPOINTS
    path("/admin") {
        /**
         * Login endpoint for administrative tasks <POST>
         *
         * BODY:
         * {@code
         *  {
         *      "email": <string>,
         *      "password": <string>
         *  }
         * }
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <nullable> {
         *          "jwt": <string>
         *      },
         *      "error": <nullable> {
         *          @see ErrorData
         *  }
         * }
         */
        post("/login") { req, res -> AdminController.loginAdmin(req, res) }

        /**
         * Logout admin user <POST>
         *
         * BODY:
         * N/A
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <null>
         *      "error": <nullable> {
         *          @see ErrorData
         *      }
         *  }
         * }
         */
        post("/logout") { req, res -> AdminController.logoutAdmin(req, res) }

        /**
         * List all products in database <GET>
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <nullable> {
         *          "products": [
         *              {
         *                  "name", <string>,
         *                  "description": <string>,
         *                  "priceCents": <int>,
         *                  "qty": <int>
         *              },...
         *          ]
         *      },
         *      "error": <nullable> {
         *          @see ErrorData
         *      }
         *  }
         * }
         */
        get("/products") { req, res -> AdminController.getProducts(req, res) }

        /**
         * Get product info <GET>
         *
         * QUERY PARAMS:
         * productId: <int>
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <nullable> {
         *          "product": {
         *              "id": <int>,
         *              "name": <string>,
         *              "description": <string>,
         *              "priceCents": <int>,
         *              "qty": <int>
         *          }
         *      },
         *      "error": <nullable> {
         *          @see ErrorData
         *      }
         *  }
         * }
         */
        get("/product/:productId") { req, res -> AdminController.getProduct(req, res) }

        /**
         * Insert a new product in the database <PUT>
         *
         * BODY:
         * {@code
         *  {
         *      "name": string,
         *      "description": <string>,
         *      "priceCents": <int>,
         *      "qty": <int>
         *  }
         * }
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <nullable> {
         *          "productId": <int>
         *      },
         *      "error": <nullable> {
         *          @see ErrorData
         *      }
         *  }
         * }
         */
        put("/product") { req, res -> AdminController.putProduct(req, res) }

        /**
         * Increase product qty in stock <PUT>
         *
         * QUERY PARAMS:
         * productId: <int>
         *
         * BODY:
         * {@code
         *  {
         *      "qty": <int>
         *  }
         * }
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <null>,
         *      "error": <nullable> {
         *          @see ErrorData
         *      }
         *  }
         * }
         */
        put("/product/:productId") { req, res -> AdminController.addProduct(req, res) }

        /**
         * Updates product info <POST>
         *
         * QUERY PARAMS:
         * productId: <int>
         *
         * BODY:
         * {@code
         *  {
         *      "name": <string, nullable>,
         *      "description": <string, nullable>,
         *      "priceCents": <int, nullable>,
         *      "qty": <int, nullable>
         *  }
         * }
         *
         * RESPONSE:
         * {@code
         *  {
         *      "data": <null>,
         *      "error": <nullable> {
         *          @see ErrorData
         *      }
         *  }
         * }
         */
        post("/product/:productId") { req, res -> AdminController.updateProduct(req, res) }
    }


    // VENDOR ENDPOINTS

    path("/vendor") {
        before("/*") { req, res ->
            val apiKey = req.headers("Authorization")
            if (apiKey == null) {
                halt(401, abort(res, "Unauthorized", 401))
            }
            val (data, successful) = VendorDAO.getVendorByKey(apiKey)
            if (!successful) {
                halt(500, abort(res, "Error retrieving vendor", 500))
            }
            if (data == null) {
                halt(403, abort(res, "You're not allowed to access this endpoint", 403))
            }
        }
        get("/products") { req, res -> VendorController.getProducts(req, res) }

        get("/product/:productId") { req, res -> VendorController.getProduct(req, res) }

        get("/product/:productId/qty") { req, res -> VendorController.getProductQty(req, res) }

        get("/products/search") { req, res -> VendorController.searchProduct(req, res) }

        post("/products/sell") { req, res -> VendorController.sellProduct(req, res) }
    }
}
