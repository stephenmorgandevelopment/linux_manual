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
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction
import com.stephenmorgandevelopment.thelinuxmanual.utils.coroutineScopeFor
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
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
    private val onAction: (ManPageAction) -> Unit,
    private val tabLifecycle: Lifecycle,
) : LazyListPrefetchStrategy {
    private val coroutineScope = coroutineScopeFor(tabLifecycle)

    /**
     * The index scheduled to be prefetched (or the last prefetched index if the prefetch is done).
     */
    private var indexToPrefetch = -1

    /** The handle associated with the current index from [indexToPrefetch]. */
    private var currentPrefetchHandle: LazyLayoutPrefetchState.PrefetchHandle? = null

    /**
     * Keeps the scrolling direction during the previous calculation in order to be able to detect
     * the scrolling direction change.
     */
    private var wasScrollingForward = false

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
                        tabLifecycle.removeObserver(this)
                    }
                }
            })
    }

    private val prefetchHandles = mutableListOf<LazyLayoutPrefetchState.PrefetchHandle>()

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
                if (prefetchHandles.isEmpty()) {
                    for (idx in 0 until listSize) {
                        prefetchHandles.add(scheduleFun(idx).also { it.markAsUrgent() })
                    }
                }
            }
        }.invokeOnCompletion {
            if (it.isNotNull() && it !is CancellationException) hasRanOnce = false
        }
    }

    override fun LazyListPrefetchScope.onScroll(delta: Float, layoutInfo: LazyListLayoutInfo) {
        schedulePersisted(::schedulePrefetch)

        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val scrollingForward = delta < 0
            val indexToPrefetch =
                if (scrollingForward) {
                    layoutInfo.visibleItemsInfo.last().index + 1
                } else {
                    layoutInfo.visibleItemsInfo.first().index - 1
                }
            if (indexToPrefetch in 0 until layoutInfo.totalItemsCount) {
                if (indexToPrefetch != this@ManPageSectionPrefetchStrategy.indexToPrefetch) {
                    if (wasScrollingForward != scrollingForward) {
                        // the scrolling direction has been changed which means the last prefetched
                        // is not going to be reached anytime soon so it is safer to dispose it.
                        // if this item is already visible it is safe to call the method anyway
                        // as it will be no-op
                        currentPrefetchHandle?.cancel()
                    }
                    this@ManPageSectionPrefetchStrategy.wasScrollingForward = scrollingForward
                    this@ManPageSectionPrefetchStrategy.indexToPrefetch = indexToPrefetch
                    currentPrefetchHandle = schedulePrefetch(indexToPrefetch)
                }
                if (scrollingForward) {
                    val lastItem = layoutInfo.visibleItemsInfo.last()
                    val spacing = layoutInfo.mainAxisItemSpacing
                    val distanceToPrefetchItem =
                        lastItem.offset + lastItem.size + spacing - layoutInfo.viewportEndOffset
                    // if in the next frame we will get the same delta will we reach the item?
                    if (distanceToPrefetchItem < -delta) {
                        currentPrefetchHandle?.markAsUrgent()
                    }
                } else {
                    val firstItem = layoutInfo.visibleItemsInfo.first()
                    val distanceToPrefetchItem = layoutInfo.viewportStartOffset - firstItem.offset
                    // if in the next frame we will get the same delta will we reach the item?
                    if (distanceToPrefetchItem < delta) {
                        currentPrefetchHandle?.markAsUrgent()
                    }
                }
            }
        }
    }

    override fun LazyListPrefetchScope.onVisibleItemsUpdated(layoutInfo: LazyListLayoutInfo) {
        layoutInfo.visibleItemsInfo
            .firstNotNullOfOrNull { it.key as? String }
            ?.let { onAction(ManPageAction.OnScroll(it)) }

        if (indexToPrefetch != -1 && layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val expectedPrefetchIndex =
                if (wasScrollingForward) {
                    layoutInfo.visibleItemsInfo.last().index + 1
                } else {
                    layoutInfo.visibleItemsInfo.first().index - 1
                }
            if (indexToPrefetch != expectedPrefetchIndex) {
                indexToPrefetch = -1
                currentPrefetchHandle?.cancel()
                currentPrefetchHandle = null
            }
        }
    }

    override fun NestedPrefetchScope.onNestedPrefetch(firstVisibleItemIndex: Int) {
        repeat(2) { i -> schedulePrefetch(firstVisibleItemIndex + i) }
    }
}
