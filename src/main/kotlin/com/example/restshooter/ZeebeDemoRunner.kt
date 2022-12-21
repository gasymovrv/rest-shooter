package com.example.restshooter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
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
    private val count = AtomicInteger(0)

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

            for (j in shortSubprocessesRange) {
                val subprocessKey = "M$i-SP$j"
                executeRequest {
                    zeebeDemoClient.sendMsgCreateSubprocess(key, subprocessKey)
                }
            }

            for (k in longSubprocessesRange) {
                val subprocessKey = "M$i-SP$k"
                executeRequest {
                    zeebeDemoClient.sendMsgCreateSubprocess(key, subprocessKey, "LONG")
                }
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

            for (k in longSubprocessesRange) {
                val subprocessKey = "M$i-SP$k"
                executeRequest {
                    zeebeDemoClient.sendMsg("MsgSimpleProcessEvent", subprocessKey, subprocessKey)
                }
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
}
