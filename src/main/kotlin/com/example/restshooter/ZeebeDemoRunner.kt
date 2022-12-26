package com.example.restshooter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class ZeebeDemoRunner(
    private val zeebeDemoClient: ZeebeDemoClient
) : CommandLineRunner {

    private val mainProcessesRange = 1..100
    private val shortSubprocessesRange = 1..2
    private val longSubprocessesRange = 1..2
    private val delayMs = 10L
    private val count = AtomicInteger(0)

    /**
     * Starts with Spring Boot
     * Sends many requests by REST API
     *
     * 'createProcesses()' starts BPMN processes with subprocesses
     * according to 'mainProcessesRange', 'shortSubprocessesRange' and 'longSubprocessesRange'
     *
     * 'completeProcesses()' sends messages as BPMN events which will complete processes
     *
     * 'delayMs' sets delay between requests, can be disabled if it sets equal or less than 0
     */
    override fun run(vararg args: String?) {
        val start = System.currentTimeMillis()
        try {
            runBlocking {
                createProcesses()
                completeProcesses()
            }
        } finally {
            val stop = System.currentTimeMillis()
            val mainProcesses = mainProcessesRange.count()
            val shortSubprocesses = shortSubprocessesRange.count() * mainProcesses
            val longSubprocesses = longSubprocessesRange.count() * mainProcesses
            val simpleProcesses = mainProcesses + shortSubprocesses + longSubprocesses

            println("expected main processes count: $mainProcesses")
            println("expected simple processes count: $simpleProcesses")
            println("delay between requests: $delayMs ms")
            println("execution time: ${(stop - start)} ms")
            println("requests count: ${count.get()}")
        }
    }

    private suspend fun createProcesses() = coroutineScope {
        println("==== start creating processes ====")
        for (i in mainProcessesRange) {
            val key = "M$i"
            executeRequest {
                zeebeDemoClient.createMainProcess(key)
            }
            delayIfNeeded()

            for (j in shortSubprocessesRange) {
                val subprocessKey = "M$i-SPS$j"
                executeRequest {
                    zeebeDemoClient.sendMsgCreateSubprocess(key, subprocessKey)
                }
                delayIfNeeded()
            }

            for (k in longSubprocessesRange) {
                val subprocessKey = "M$i-SPL$k"
                executeRequest {
                    zeebeDemoClient.sendMsgCreateSubprocess(key, subprocessKey, "LONG")
                }
                delayIfNeeded()
            }
        }
        println("==== processes created ====")
    }

    private suspend fun completeProcesses() = coroutineScope {
        println("==== start completing processes ====")
        for (i in mainProcessesRange) {
            val key = "M$i"
            executeRequest {
                zeebeDemoClient.sendMsg("MsgCompleteMainProcess", key, key)
            }
            delayIfNeeded()

            for (k in longSubprocessesRange) {
                val subprocessKey = "M$i-SPL$k"
                executeRequest {
                    zeebeDemoClient.sendMsg("MsgSimpleProcessEvent", subprocessKey, subprocessKey)
                }
                delayIfNeeded()
            }
        }
        println("==== processes completed ====")
    }

    private fun CoroutineScope.executeRequest(request: suspend () -> Unit) {
        launch {
            try {
                request()
                count.incrementAndGet()
            } catch (e: Exception) {
                error("FAILED REQUEST: ${e.message}")
            }
        }
    }

    private suspend fun delayIfNeeded() {
        if (delayMs <= 0) return
        delay(delayMs)
    }
}
