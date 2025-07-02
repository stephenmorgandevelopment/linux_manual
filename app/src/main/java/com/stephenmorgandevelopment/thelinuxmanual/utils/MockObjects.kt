package com.stephenmorgandevelopment.thelinuxmanual.utils

import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.presentation.PagerTab

object MockObjects {
    val testUrl = "https://ubuntu.com/"
    val longDescription = buildString {
        for (i in 0 until 12) {
            append("Just a test item with a long description. ")
        }
    }

    val matchItems = listOf<MatchingItem>(
        MatchingItem(17L, "Item 17", "Just a test item with a short description.", testUrl, 7),
        MatchingItem(843294854L, "Item 843294854", null, testUrl, 3),
    )

    val pagerTabs = List<PagerTab>(4) {
        PagerTab("Item${it + it * it}")
    }


    val lookupItemsLongList = listOf<MatchingItem>(
        MatchingItem(
            17L,
            "Item 17",
            "Just a test item with a short description.",
            MockObjects.testUrl,
            7
        ),
        MatchingItem(745L, "Item 745", MockObjects.longDescription, MockObjects.testUrl, 7),

        MatchingItem(742L, "Item 742", MockObjects.longDescription, MockObjects.testUrl, 2),

        MatchingItem(741L, "Item 741", MockObjects.longDescription, MockObjects.testUrl, 8),

        MatchingItem(748L, "Item 748", MockObjects.longDescription, MockObjects.testUrl, 4),
        MatchingItem(746L, "Item 746", MockObjects.longDescription, MockObjects.testUrl, 7),

        MatchingItem(7427L, "Item 7427", MockObjects.longDescription, MockObjects.testUrl, 2),

        MatchingItem(7411L, "Item 7411", MockObjects.longDescription, MockObjects.testUrl, 8),

        MatchingItem(7486L, "Item 7486", MockObjects.longDescription, MockObjects.testUrl, 4),
        MatchingItem(843294854L, "Item 843294854", null, MockObjects.testUrl, 3),
    )

    val lookupItemsShortList = listOf<MatchingItem>(
        MatchingItem(
            17L,
            "Item 17",
            "Just a test item with a short description.",
            MockObjects.testUrl,
            7
        ),
        MatchingItem(741L, "Item 741", MockObjects.longDescription, MockObjects.testUrl, 4),
        MatchingItem(843294854L, "Item 843294854", null, MockObjects.testUrl, 3),
    )

}