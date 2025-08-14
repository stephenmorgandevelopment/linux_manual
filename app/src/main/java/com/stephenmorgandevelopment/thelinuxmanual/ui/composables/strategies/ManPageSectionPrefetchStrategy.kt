/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Most of this code came from androidx.compose.foundation.lazy.LazyListPrefetchStrategy.kt\
 *
 * I modified onScroll and added the init block, prefetchHandles, and schedulePersisted.
 *
 */


@file:OptIn(ExperimentalFoundationApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.strategies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListPrefetchScope
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.foundation.lazy.layout.NestedPrefetchScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.stephenmorgandevelopment.thelinuxmanual.utils.coroutineScopeFor
import com.stephenmorgandevelopment.thelinuxmanual.utils.dlog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *  Most of this class was copied over from DefaultLazyListPrefetchStrategy.
 *
 *  There is an addition to add prefetching for all items, allowing them to be pinned
 *  asap.  This is a tradeoff placing higher demands on device memory with the potential for
 *  more jank early on in the screen loading versus continual jank when recomposing large
 *  character counts inside Text composables.
 */
class ManPageSectionPrefetchStrategy(
    val id: Long,
    private val listSize: Int,
    private val updateSection: (String) -> Unit,
    private val tabLifecycle: Lifecycle,
) : LazyListPrefetchStrategy {
    private val coroutineScope = coroutineScopeFor(tabLifecycle)

    private var hasRanOnce = false

    init {
        tabLifecycle.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event,
                ) {
                    if (event == Lifecycle.Event.ON_STOP) {
                        coroutineScope.coroutineContext.cancelChildren()
                        hasRanOnce = false
                    }

                    if (event == Lifecycle.Event.ON_DESTROY) {
                        javaClass.dlog("Cleaning up ManPageSectionPrefetchStrategy")
                        tabLifecycle.removeObserver(this)
                    }
                }
            })
    }

    /**
     * Adds all items in lazy list to be scheduled for prefetching.  When they are
     * prefetched the ManPageScreen pins them so they can scroll smoothly.
     *
     * I had also experimented with using a character breakpoint to only prefetch and pin
     * certain large items, but pinning all has a limited impact compared to pinning only the
     * very large sections.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun schedulePersisted(scheduleFun: (Int) -> LazyLayoutPrefetchState.PrefetchHandle) {
        if (hasRanOnce) return
        hasRanOnce = true

        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                for (idx in 0 until listSize) {
                    scheduleFun(idx).also { it.markAsUrgent() }
                }
            }
        }.invokeOnCompletion {
            // If we have an error that isn't cancellation exception
            // assume we didn't complete the scheduling for prefetch.
            if (it != null && it !is CancellationException) hasRanOnce = false
        }
    }

    override fun LazyListPrefetchScope.onScroll(delta: Float, layoutInfo: LazyListLayoutInfo) {
        schedulePersisted(::schedulePrefetch)
    }

    override fun LazyListPrefetchScope.onVisibleItemsUpdated(layoutInfo: LazyListLayoutInfo) {
        // We only need this to update the section name.
        // All prefetching will have taken place already.
        layoutInfo.visibleItemsInfo
            .firstNotNullOfOrNull { it.key as? String }
            ?.let { updateSection(it) }
    }

    override fun NestedPrefetchScope.onNestedPrefetch(firstVisibleItemIndex: Int) {
        repeat(listSize) { i -> schedulePrecomposition(firstVisibleItemIndex + i) }
    }
}
