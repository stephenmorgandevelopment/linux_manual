package com.stephenmorgandevelopment.thelinuxmanual.ui.lifecycle

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.stephenmorgandevelopment.thelinuxmanual.utils.add
import com.stephenmorgandevelopment.thelinuxmanual.utils.getEventForMoveTo
import com.stephenmorgandevelopment.thelinuxmanual.utils.greaterThan
import com.stephenmorgandevelopment.thelinuxmanual.utils.lessThan
import com.stephenmorgandevelopment.thelinuxmanual.utils.remove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManPageTab(
    val id: Long,
    lifecycleOwner: LifecycleOwner,
) : LifecycleOwner {
    private var stateToRestore: State = INITIALIZED

    override val lifecycle: ManPageTabLifecycle = ManPageTabLifecycle(id, this) {
        stateToRestore = it
    }

    init {
        lifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    if (lifecycle.currentState.lessThan(CREATED)
                        && (stateToRestore.greaterThan(INITIALIZED))
                    ) {
                        lifecycle.moveTo(CREATED, false)
                    }
                }

                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    if (lifecycle.currentState.lessThan(STARTED)
                        && (stateToRestore.greaterThan(CREATED))
                    ) {
                        lifecycle.moveTo(STARTED, false)
                    }
                }

                override fun onResume(owner: LifecycleOwner) {
                    super.onResume(owner)
                    if (lifecycle.currentState.lessThan(RESUMED)
                        && (stateToRestore.greaterThan(STARTED))
                    ) {
                        lifecycle.moveTo(RESUMED, false)
                    }
                }

                override fun onPause(owner: LifecycleOwner) {
                    super.onPause(owner)
                    if (lifecycle.currentState.greaterThan(STARTED)) {
                        lifecycle.moveTo(STARTED, false)
                    }
                }

                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    if (lifecycle.currentState.greaterThan(CREATED)) {
                        lifecycle.moveTo(CREATED, false)
                    }
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    if (owner is Activity && owner.isFinishing) {
                        lifecycle.moveTo(DESTROYED, false)
                    } else {
                        lifecycle.moveTo(INITIALIZED, false)
                    }
                }
            }
        )
    }
}

class ManPageTabLifecycle(
    val id: Long,
    val owner: ManPageTab,
    private val setRestoreState: (Lifecycle.State) -> Unit,
) : Lifecycle() {
    private val _state = MutableStateFlow(INITIALIZED)
    private val _observers = MutableStateFlow<List<ManPageTabObserver>>(emptyList())
    private val observerLock = object : Any() {}

    init {
        updateObserversOnStateChanged()
    }

    private fun updateObserversOnStateChanged() = this.coroutineScope.launch {
        withContext(Dispatchers.Default) {
            _state.asStateFlow()
                .collect { state ->
                    synchronized(observerLock) {
                        _observers.value.forEach {
                            it.setCurrentState(state)
                        }
                    }
                }
        }
    }

    override fun addObserver(observer: LifecycleObserver) {
        synchronized(observerLock) {
            if (observer is ManPageTabObserver) {
                _observers.value = _observers.value.add(observer)
                observer.setCurrentState(currentState)
            }
        }
    }

    override fun removeObserver(observer: LifecycleObserver) {
        synchronized(observerLock) {
            if (observer is ManPageTabObserver) {
                _observers.value = _observers.value.remove(observer)

            }
        }
    }

    override val currentState: State
        get() = _state.value

    /**
     * Called externally to control lifecycle from nav host.
     */
    fun moveTo(state: State, saveStateToRestore: Boolean = true) {
        _state.value = state
        if (saveStateToRestore) {
            setRestoreState(state)
        }
    }

    abstract class ManPageTabObserver(private val lifecycle: ManPageTabLifecycle) :
        LifecycleObserver,
        LifecycleEventObserver {
        private val stateLock = object : Any() {}
        private var state: State = INITIALIZED
            set(value) {
                synchronized(stateLock) {
                    state.getEventForMoveTo(value)?.let {
                        this.onStateChanged(
                            source = lifecycle.owner,
                            event = it,
                        )
                    }
                    field = value
                }
            }

        fun setCurrentState(toState: State) {
            synchronized(stateLock) {
                if (state == DESTROYED) return

                if (state.lessThan(toState)) {
                    while (state.lessThan(toState)) {
                        state = State.entries.first {
                            it.ordinal == state.ordinal.plus(1)
                        }
                    }
                } else if (toState.lessThan(state)) {
                    while (state.greaterThan(toState)) {
                        state = State.entries.first {
                            it.ordinal == state.ordinal.minus(1)
                        }
                    }
                }
            }
        }
    }
}
