package com.example.restshooter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.leroymerlin.wecare.restclient.integration.InternalRestClientsFactory

@Configuration
class RestClientsConfiguration(
    val restClientsFactory: InternalRestClientsFactory
) {

    @Bean
    fun zeebeDemoClient(objectMapper: ObjectMapper): ZeebeDemoClient {
        val restClient = restClientsFactory["zeebe-demo"]
        return ZeebeDemoClient(restClient, objectMapper)
    }
}
