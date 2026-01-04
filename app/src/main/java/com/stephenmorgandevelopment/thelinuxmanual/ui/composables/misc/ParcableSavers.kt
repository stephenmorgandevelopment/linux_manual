package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.misc

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.Saver
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LazyListStateWithId
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState

internal val parcelableMapSaver =
    Saver<Map<Long, ManPageSearchState>, Array<ManPageSearchState>>(
        save = {
            it.map { entry -> entry.value }.toTypedArray()
        },
        restore = {
            it.associateBy { searchState -> searchState.id }
        }
    )

internal val lazyListStateMapSavable =
    Saver<Map<Long, LazyListState>, Array<LazyListStateWithId>>(
        save = {
            it.map { entry ->
                LazyListStateWithId(
                    id = entry.key,
                    firstVisibleItemIndex = entry.value.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = entry.value.firstVisibleItemScrollOffset,
                )
            }.toTypedArray()
        },
        restore = {
            it.associateBy { stateWithId -> stateWithId.id }
                .mapValues { entry ->
                    LazyListState(
                        entry.value.firstVisibleItemIndex,
                        entry.value.firstVisibleItemScrollOffset,
                    )
                }
        }
    )