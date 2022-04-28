package org.example.globalsourcing.util

data class ResponseData<T>(
    val status: Int,
    val message: String,
    val data: T?
) {
    companion object {
        /**
         * 构造请求成功时的响应数据。
         */
        fun <T> success(data: T?): ResponseData<T> = ResponseData(200, "成功", data)

        /**
         * 构造请求失败或异常时的响应数据。
         */
        fun error(status: Int, message: String): ResponseData<Any> = ResponseData(status, message, null)
    }
}