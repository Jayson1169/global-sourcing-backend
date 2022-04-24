package org.example.globalsourcing.aspect

import org.apache.commons.lang3.StringUtils
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.*
import org.example.globalsourcing.util.LOG_MAX_SIZE
import org.example.globalsourcing.util.ResponseData
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Configuration
class LogAspect {
    private val logger = LoggerFactory.getLogger(LogAspect::class.java)

    @Pointcut("execution(* org.example.globalsourcing.controller.*.*(..))")
    fun execute() = Unit

    @Before("execute()")
    fun doBefore(joinPoint: JoinPoint) {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val url = request.requestURL.toString()
        val ip = request.remoteAddr
        val method = joinPoint.signature.name
        val args = joinPoint.args.toList()
        val log = RequestLog(url, ip, method, args)
        logger.info("Request: {}", log)
    }

    @AfterReturning(returning = "result", pointcut = "execute()")
    fun doAfterReturning(result: ResponseData<*>) {
        logger.info("Return: {}", StringUtils.abbreviate(result.toString(), LOG_MAX_SIZE))
    }

    @AfterThrowing(throwing = "e", pointcut = "execute()")
    fun doAfterThrowing(e: Exception) {
        logger.error("Exception: {}", e.toString())
    }

    private data class RequestLog(val url: String, val ip: String, val method: String, val args: List<Any>)
}