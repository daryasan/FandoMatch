package org.example

import org.example.config.JwtProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
@ComponentScan(basePackages = ["org.example", "com.fandomatch.media", "com.fandomatch.notifications"])
class UsersApplication

fun main(args: Array<String>) {
    SpringApplication.run(UsersApplication::class.java, *args)
}