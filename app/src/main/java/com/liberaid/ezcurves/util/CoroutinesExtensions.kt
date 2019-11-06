package com.liberaid.ezcurves.util

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.launchUI(block: suspend CoroutineScope.() -> Unit) = this.launch(Dispatchers.Main, block = block)
suspend fun <T> CoroutineScope.withUI(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block = block)
fun CoroutineScope.launchCatching(block: suspend CoroutineScope.() -> Unit, onError: (ctx: CoroutineContext, throwable: Throwable) -> Unit) = launch(CoroutineExceptionHandler(onError), block = block)
fun CoroutineScope.launchUICatching(block: suspend CoroutineScope.() -> Unit, onError: (ctx: CoroutineContext, throwable: Throwable) -> Unit) = launch(
    CoroutineExceptionHandler(onError) + Dispatchers.Main, block = block)
