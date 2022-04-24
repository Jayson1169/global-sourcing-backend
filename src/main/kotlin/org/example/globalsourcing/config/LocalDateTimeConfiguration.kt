package org.example.globalsourcing.config

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * LocalDateTime 系列 API 的序列化配置类。
 */
@Configuration
class LocalDateTimeConfiguration {
    @Bean
    fun customizeLocalDateTimeFormat(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer {
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            it.serializerByType(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
            it.deserializerByType(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
            it.serializerByType(LocalDate::class.java, LocalDateSerializer(dateFormatter))
            it.deserializerByType(LocalDate::class.java, LocalDateDeserializer(dateFormatter))
            it.serializerByType(LocalTime::class.java, LocalTimeSerializer(timeFormatter))
            it.deserializerByType(LocalTime::class.java, LocalTimeDeserializer(timeFormatter))
        }
    }
}