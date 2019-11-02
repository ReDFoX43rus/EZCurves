package com.liberaid.ezcurves.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun CoroutineScope.launchUI(block: suspend CoroutineScope.() -> Unit) = this.launch(Dispatchers.Main, block = block)
suspend fun <T> CoroutineScope.withUI(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block = block)