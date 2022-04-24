package org.example.globalsourcing.service

/**
 * 自定义 Service 层异常类。
 */
class ServiceException(override val message: String) : Exception(message)