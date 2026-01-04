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

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.misc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListPrefetchScope
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.foundation.lazy.layout.NestedPrefetchScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.stephenmorgandevelopment.thelinuxmanual.utils.coroutineScopeFor
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val UnspecifiedNestedPrefetchCount = -1
private const val UnsetItemCount = -1
private const val initialNestedPrefetchItemCount: Int = 2

/**
 *  A lot of this class was copied over from DefaultLazyListPrefetchStrategy inside
 *  of LazyListPrefetchStrategy.kt.
 *
 * Very large man pages (ie ffmpeg, rsync, etc) have such large text sections that
 * Compose has difficulty measuring and laying them, in a lazy column, due to the
 * large amount of text in one item.
 *
 * Since this is primarily a limitation of Compose itself, and because I have limited time
 * to work on this project, I'm not going to implement text premeasuring or other work arounds.
 * This was the most time efficient method for allowing somewhat smooth scrolling on these
 * larger man pages.
 *
 * The idea is to schedule all items in the lazy list for prefetching.
 * On the ManPageScreen, items are pinned.  This eliminates long Text reneder times
 * when scrolled off screen and back again.
 */
@OptIn(ExperimentalFoundationApi::class)
@Stable
class ManPagePrefetchStrategy(
    val id: Long,
    private val listSize: Int,
    private val updateSection: (String) -> Unit,
    private val tabLifecycle: Lifecycle,
) : LazyListPrefetchStrategy {

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

    private var previousPassItemCount = UnsetItemCount
    private var previousPassDelta = 0f

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
                        tabLifecycle.removeObserver(this)
                    }
                }
            })
    }

    private val prefetchHandles = mutableListOf<LazyLayoutPrefetchState.PrefetchHandle>()

    /**
     * Adds all items in lazy list to be scheduled for prefetching.  When they are
     * prefetched the ManPageScreen pins them so they can scroll relatively smoothly.
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
    }

    override fun LazyListPrefetchScope.onVisibleItemsUpdated(layoutInfo: LazyListLayoutInfo) {
        layoutInfo.visibleItemsInfo
            .firstNotNullOfOrNull { it.key as? String }
            ?.let { updateSection(it) }

        layoutInfo.evaluatePrefetchForCancellation(indexToPrefetch, wasScrollingForward)

        val currentPassItemCount = layoutInfo.totalItemsCount
        // total item count changed, re-trigger prefetch.
        if (
            previousPassItemCount != UnsetItemCount && // we already have info about the item count
            previousPassDelta != 0.0f && // and scroll direction
            previousPassItemCount != currentPassItemCount && // and the item count changed
            layoutInfo.visibleItemsInfo.isNotEmpty()
        ) {
            val indexToPrefetch = layoutInfo.calculateIndexToPrefetch(previousPassDelta < 0)
            if (indexToPrefetch in 0 until currentPassItemCount) {
                this@ManPagePrefetchStrategy.indexToPrefetch = indexToPrefetch
                currentPrefetchHandle = schedulePrefetch(indexToPrefetch)
            }
        }

        previousPassItemCount = currentPassItemCount
    }

    override fun NestedPrefetchScope.onNestedPrefetch(firstVisibleItemIndex: Int) {
        val resolvedNestedPrefetchItemCount =
            if (nestedPrefetchItemCount == UnspecifiedNestedPrefetchCount) {
                initialNestedPrefetchItemCount
            } else {
                nestedPrefetchItemCount
            }
        repeat(resolvedNestedPrefetchItemCount) { i ->
            schedulePrecomposition(firstVisibleItemIndex + i)
        }
    }

    private fun resetPrefetchState() {
        indexToPrefetch = -1
        currentPrefetchHandle?.cancel()
        currentPrefetchHandle = null
    }

    private fun LazyListLayoutInfo.calculateIndexToPrefetch(scrollingForward: Boolean): Int {
        return if (scrollingForward) {
            visibleItemsInfo.last().index + 1
        } else {
            visibleItemsInfo.first().index - 1
        }
    }

    private fun LazyListLayoutInfo.evaluatePrefetchForCancellation(
        currentPrefetchingIndex: Int,
        scrollingForward: Boolean,
    ) {
        if (currentPrefetchingIndex != -1 && visibleItemsInfo.isNotEmpty()) {
            val expectedPrefetchIndex = calculateIndexToPrefetch(scrollingForward)
            if (currentPrefetchingIndex != expectedPrefetchIndex) {
                resetPrefetchState()
            }
        }
    }
}
