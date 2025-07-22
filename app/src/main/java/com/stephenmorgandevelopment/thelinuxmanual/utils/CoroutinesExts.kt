package com.stephenmorgandevelopment.thelinuxmanual.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.coroutineScope
import com.stephenmorgandevelopment.thelinuxmanual.ui.lifecycle.ManPageTabLifecycle
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
        collect() // tail-call
    }

/**
 * Coroutine scope tied to the passed in ManPageTabLifecycle.
 * Runs in Dispatchers.Default and contains a supervisor job to prevent
 * children exceptions from cancelling scope.
 */
fun coroutineScopeFor(lifecycle: ManPageTabLifecycle): CoroutineScope {
    return lifecycle.coroutineScope + Dispatchers.Default + SupervisorJob()
}

/**
 *  Get the corresponding event for an <b><i>Incremental</i></b> change in lifecycle state.
 *
 *  @param newState: Lifecycle.State - the state lifecycle is being moved to
 *  @return Lifecycle.Event that should be emitted to observers or null if already in DESTROYED state
 *                              or calling state == newState.
 *
 *  @throws IllegalStateException - when newState is not one step up or down from calling state.
 */
fun Lifecycle.State.getEventForMoveTo(newState: Lifecycle.State): Lifecycle.Event? =
    this.let { currentState ->
        if (currentState == newState) return null

        val directionIsUpward = currentState.lessThan(newState)

        return if (directionIsUpward) {
            if (newState.ordinal == currentState.ordinal.plus(1)) {
                currentState.toUpwardEventOrNull
            } else throw IllegalStateException("Changes in state must happen incrementally.")
        } else {
            if (newState.ordinal == currentState.ordinal.minus(1)) {
                currentState.toDownwardEventOrNull
            } else throw IllegalStateException("Changes in state must happen incrementally.")
        }
    }

/**
 * Compare calling state to given state and return true if given state is in a more active
 * lifecycle state, but not destroyed.
 *
 * DESTROYED is treated as a special and final state for our purposes.
 */
fun Lifecycle.State.lessThan(state: Lifecycle.State): Boolean {
    return this.ordinal < state.ordinal
}

fun Lifecycle.State.greaterThan(state: Lifecycle.State): Boolean {
    return this.ordinal > state.ordinal
}

/**
 * @returns Lifecycle.Event? - the corresponding lifecycle event to move from the caller's state into a higher state.
 *
 * Will return null if caller state is DESTROYED(lifecycle effectively ended) or RESUMED(no where to go).
 */
val Lifecycle.State.toUpwardEventOrNull
    get(): Lifecycle.Event? = when (this) {
        DESTROYED -> null
        INITIALIZED -> ON_CREATE
        CREATED -> ON_START
        STARTED -> ON_RESUME
        RESUMED -> null
    }

val Lifecycle.State.toDownwardEventOrNull
    get(): Lifecycle.Event? = when (this) {
        DESTROYED -> null
        INITIALIZED -> null
        CREATED -> ON_DESTROY
        STARTED -> ON_STOP
        RESUMED -> ON_PAUSE
    }
