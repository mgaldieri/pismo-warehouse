package me.mgaldieri.pismowarehouse.models

data class ErrorMessage(val type: String, val errorCode: String, val httpCode: Int, val message: String)