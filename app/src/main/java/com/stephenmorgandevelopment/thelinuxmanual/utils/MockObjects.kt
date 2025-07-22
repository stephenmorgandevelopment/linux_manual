package com.stephenmorgandevelopment.thelinuxmanual.utils

import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo

object MockObjects {
    val testUrl = "https://ubuntu.com/"
    val longDescription = buildString {
        for (i in 0 until 12) {
            append("Just a test item with a long description. ")
        }
    }

    val matchItems = listOf<MatchingItem>(
        MatchingItem(17L, "Item 17", "Just a test item with a short description.", testUrl),
        MatchingItem(843294854L, "Item 843294854", null, testUrl),
    )

    val tabInfos = List<TabInfo>(4) {
        TabInfo("Item${it + it * it}")
    }

    val commandData = mapOf<String, String>(
        "name" to "Random",
        "short" to "not very long",
        "long" to longDescription,
        "longer" to longDescription.plus(longDescription).plus(longDescription),
    )

    val lookupItemsLongList = listOf<MatchingItem>(
        MatchingItem(
            17L,
            "Item 17",
            "Just a test item with a short description.",
            testUrl
        ),
        MatchingItem(745L, "Item 745", longDescription, testUrl),

        MatchingItem(742L, "Item 742", longDescription, testUrl),

        MatchingItem(741L, "Item 741", longDescription, testUrl),

        MatchingItem(748L, "Item 748", longDescription, testUrl),
        MatchingItem(746L, "Item 746", longDescription, testUrl),

        MatchingItem(7427L, "Item 7427", longDescription, testUrl),

        MatchingItem(7411L, "Item 7411", longDescription, testUrl),

        MatchingItem(7486L, "Item 7486", longDescription, testUrl),
        MatchingItem(843294854L, "Item 843294854", null, testUrl),
    )

    val lookupItemsShortList = listOf<MatchingItem>(
        MatchingItem(
            17L,
            "Item 17",
            "Just a test item with a short description.",
            testUrl
        ),
        MatchingItem(741L, "Item 741", longDescription, testUrl),
        MatchingItem(843294854L, "Item 843294854", null, testUrl),
    )

    val mockSearchState = ManPageSearchState(
        id = 27L,
        index = 1,
        query = "Winning?",
        results = TextSearchResult("Winning?", emptyList()),
        visible = true,
    )
}
