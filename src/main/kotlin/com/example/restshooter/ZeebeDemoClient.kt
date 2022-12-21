package com.example.restshooter

import com.fasterxml.jackson.databind.ObjectMapper
import ru.leroymerlin.wecare.restclient.integration.RestClient

class ZeebeDemoClient(
    private val zeebeDemoClient: RestClient,
    private val objectMapper: ObjectMapper
) {

    suspend fun createMainProcess(key: String, branch: String = "SHORT") {
        val vars = mapOf(
            "key" to key,
            "startBranch" to branch,
            "timeout" to "PT1H"
        )
        val request = CreateInstanceRequest(
            bpmnProcessId = "main-process",
            vars = vars
        )

        val resp = zeebeDemoClient.post("/processes", request, String::class.java)
        if (resp.isNullOrBlank()) {
            println("POST /processes: Got empty response for request: ${objectMapper.writeValueAsString(request)}")
        }
    }

    suspend fun sendMsgCreateSubprocess(key: String, subprocessKey: String, branch: String = "SHORT") {
        sendMsg(
            msgName = "MsgCreateNewSimpleProcess",
            correlationKey = key,
            vars = mapOf(
                "subprocessKey" to subprocessKey,
                "branch" to branch
            )
        )
    }

    suspend fun sendMsg(
        msgName: String,
        correlationKey: String,
        msgId: String? = null,
        vars: Map<String, Any?> = mapOf()
    ) {
        val request = SendMessageRequest(
            msgName = msgName,
            correlationKey = correlationKey,
            messageId = msgId,
            vars = vars
        )

        val resp = zeebeDemoClient.post("/messages", request, String::class.java)
        if (resp.isNullOrBlank()) {
            println("POST /messages: Got empty response for request: ${objectMapper.writeValueAsString(request)}")
        }
    }

    private data class CreateInstanceRequest(
        val bpmnProcessId: String,
        val vars: Map<String, Any?>? = null
    )

    private data class SendMessageRequest(
        val msgName: String,
        val correlationKey: String,
        val messageId: String? = null,
        val vars: Map<String, Any?>? = null
    )
}
