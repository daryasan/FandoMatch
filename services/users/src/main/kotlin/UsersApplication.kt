package org.example

import org.example.config.JwtProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
class UsersApplication

fun main(args: Array<String>) {
    SpringApplication.run(UsersApplication::class.java, *args)
}