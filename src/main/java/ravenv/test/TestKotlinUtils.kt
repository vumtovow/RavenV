package ravenv.test

import kotlinx.coroutines.*

object TestKotlinUtils {
    /**
     * Test function: converts Java String to uppercase using Kotlin
     */
    fun kotlinToUpperCase(input: String): String {
        return input.uppercase()
    }

    /**
     * Test function: uses Kotlin extension functions
     */
    fun repeatedString(text: String, times: Int): String {
        return (1..times).joinToString(" ") { text }
    }

    /**
     * Test coroutine: demonstrates async operation
     * Call from Java: TestKotlinUtils.INSTANCE.testCoroutineBlocking()
     */
    fun testCoroutineBlocking() {
        runBlocking {
            val result = async {
                delay(100)
                "Coroutine completed!"
            }
            println(result.await())
        }
    }

    /**
     * Test nullable types and smart casts
     */
    fun processNullable(value: String?): Int {
        return value?.length ?: 0
    }

    /**
     * Test data class
     */
    data class ModInfo(val name: String, val version: String)

    fun createModInfo(name: String, version: String): ModInfo {
        return ModInfo(name, version)
    }
}