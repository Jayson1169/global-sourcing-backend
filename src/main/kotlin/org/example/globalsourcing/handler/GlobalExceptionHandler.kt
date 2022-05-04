package org.example.globalsourcing.handler

import org.example.globalsourcing.service.ServiceException
import org.example.globalsourcing.util.ResponseData
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ConstraintViolationException

/**
 * 全局异常处理类。
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * JSON参数校验异常处理。
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseData<Nothing> {
        val message = e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
        return ResponseData.error(400, "请求参数异常: {${message}}")
    }

    /**
     * URL参数校验异常处理。
     */
    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseData<Nothing> {
        val message = e.constraintViolations.joinToString { "${it.propertyPath}: ${it.message}" }
        return ResponseData.error(400, "请求参数异常: {${message}}")
    }

    /**
     * 请求参数异常处理。
     */
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseData<Nothing> {
        return ResponseData.error(400, e.message ?: "")
    }

    /**
     * 服务层异常处理。
     */
    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(e: ServiceException): ResponseData<Nothing> {
        return ResponseData.error(300, e.message ?: "")
    }
}