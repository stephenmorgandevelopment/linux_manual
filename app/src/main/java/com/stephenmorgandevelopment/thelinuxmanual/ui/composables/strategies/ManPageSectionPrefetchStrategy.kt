@file:OptIn(ExperimentalFoundationApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.strategies

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListPrefetchScope
import androidx.compose.foundation.lazy.LazyListPrefetchStrategy
import androidx.compose.foundation.lazy.layout.LazyLayoutPrefetchState
import androidx.compose.foundation.lazy.layout.NestedPrefetchScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction
import com.stephenmorgandevelopment.thelinuxmanual.ui.lifecycle.ManPageTabLifecycle
import com.stephenmorgandevelopment.thelinuxmanual.utils.coroutineScopeFor
import com.stephenmorgandevelopment.thelinuxmanual.utils.dlog
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManPageSectionPrefetchStrategy(
    private val listSize: Int,
    private val onAction: (ManPageAction) -> Unit,
    private val tabLifecycle: ManPageTabLifecycle,
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
            object : ManPageTabLifecycle.ManPageTabObserver(tabLifecycle) {
                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event,
                ) {
                    val id = tabLifecycle.id
                    javaClass.ilog("tab id: $id - emitted lifecycle event: $event")
                    if (event == Lifecycle.Event.ON_STOP) {
                        coroutineScope.coroutineContext.cancelChildren()
                        hasRanOnce = false
                    }

                    if (event == Lifecycle.Event.ON_DESTROY) {
                        javaClass.dlog("Destroyed tab with tab id: $id")
                        tabLifecycle.removeObserver(this)
                    }
                }
            })
    }

    private val prefetchHandles = mutableListOf<LazyLayoutPrefetchState.PrefetchHandle>()

    @OptIn(DelicateCoroutinesApi::class)
    private fun schedulePersisted(scheduleFun: (Int) -> LazyLayoutPrefetchState.PrefetchHandle) {
        if (hasRanOnce) return
        hasRanOnce = true

        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                if (prefetchHandles.isEmpty()) {
                    for (idx in 0 until listSize) {
                        prefetchHandles.add(scheduleFun(idx)/*.also { it.markAsUrgent() }*/)
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
