package com.github.yuk1ty.contextParametersExample

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun callCoroutine() {
    coroutineScope {
        val firstTask = async { highLoadFunction() }
        val secondTask = async { highLoadFunction() }
        // Continues...
    }
}

fun highLoadFunction() {}