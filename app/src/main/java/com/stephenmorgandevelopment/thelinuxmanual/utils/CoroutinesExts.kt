package com.stephenmorgandevelopment.thelinuxmanual.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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
        collect()
    }

/**
 * Coroutine scope tied to the passed in ManPageTabLifecycle.
 * Runs in Dispatchers.Default and contains a supervisor job to prevent
 * children exceptions from cancelling scope.
 */
fun coroutineScopeFor(lifecycle: Lifecycle): CoroutineScope {
    return lifecycle.coroutineScope + Dispatchers.Default + SupervisorJob()
}
