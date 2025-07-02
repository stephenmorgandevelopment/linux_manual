package com.stephenmorgandevelopment.thelinuxmanual.utils

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun CoroutineScope.launchCompletable(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit,
): CompletableJob {
    return Job(this.launch(context = context, block = block))
}

fun <T> Flow<T>.launchInCompletable(scope: CoroutineScope): CompletableJob =
    scope.launchCompletable {
        collect() // tail-call
    }