package org.example

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients(basePackages = ["com.fandomatch.users.api"])
class CoreApplication

fun main(args: Array<String>) {
    SpringApplication.run(CoreApplication::class.java, *args)
}