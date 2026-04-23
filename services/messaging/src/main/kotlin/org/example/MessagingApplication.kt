package org.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["org.example", "com.fandomatch.media", "com.fandomatch.notifications"])
class MessagingApplication

fun main(args: Array<String>) {
    runApplication<MessagingApplication>(*args)
}
