package me.mgaldieri.pismowarehouse

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import me.mgaldieri.pismowarehouse.models.ErrorMessage
import me.mgaldieri.pismowarehouse.models.ResponseData
import org.sql2o.Connection
import org.sql2o.Sql2o
import spark.Response

fun abort(resp: Response, message: String, httpCode: Int) : String {
    val error = ErrorMessage(
            "Server Error",
            "0000",
            httpCode,
            message
    )
    val responseData = ResponseData(null, error)

    val mapper = jacksonObjectMapper()
    val jsonString = mapper.writeValueAsString(responseData)

    resp.status(httpCode)
    resp.type("application/json;charset=utf-8")

    return jsonString
}

fun success(resp: Response, data: Any?) : String {
    val responseData = ResponseData(data, null)

    val mapper = jacksonObjectMapper()
    val jsonString = mapper.writeValueAsString(responseData)

    resp.status(200)
    resp.type("application/json;charset=utf-8")

    return jsonString
}

fun initDB() {
    val initSQL = object {}.javaClass.getResource("initdb.sql").readText(Charsets.UTF_8)
    val db = DBHelper.getInstance()
    val conn: Connection = db.open() ?: throw RuntimeException("Could not open connection to DB")
    conn.createQuery(initSQL).executeUpdate()
}
