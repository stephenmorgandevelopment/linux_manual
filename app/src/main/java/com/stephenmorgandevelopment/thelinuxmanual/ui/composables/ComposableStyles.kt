package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Colors {
    val transparent = Color(0x00000000)
    val offWhite = Color(0xFFe0e0e0)
    val black = Color(0xFF000000)
    val darkGray = Color(0xFF222222)
    val indicatorColor = Color(0xFFdd9b1a)
}

val defaultTextStyle = TextStyle(
    fontSize = 14.sp,
)

val optionsMenuTextStyle = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight(550),
    lineHeight = 20.sp,
    textAlign = TextAlign.Start,
)

val releaseMenuItemStyle = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight(400),
)


val optionButtonHeight = 48.dp

//val optionButtonPadding = PaddingValues(vertical = 4.dp, horizontal = 10.dp)
val optionButtonPadding = PaddingValues(
    top = 4.dp,
    bottom = 4.dp,
    start = 10.dp,
    end = 10.dp,
)

val releaseOptionsItemPadding = PaddingValues(
    horizontal = 12.dp,
    vertical = 8.dp,
)

val optionsMenuPadding = PaddingValues(
    vertical = 12.dp,
    horizontal = 16.dp,
)

val optionsMenuItemPadding = PaddingValues(vertical = 5.dp, horizontal = 3.dp)

val toolbarHeight = 64.dp
val appbarTitleStyle = TextStyle(
    fontWeight = FontWeight(600),
    fontSize = 24.sp,
    textAlign = TextAlign.Start,

    )


/**
Lookup match list item styles
 */

val matchListItemPadding = PaddingValues(
    vertical = 5.dp,
    horizontal = 12.dp
)

val matchTitleTextPadding = PaddingValues(
    top = 0.dp,
    bottom = 5.dp,
    end = 8.dp,
    start = 18.dp,
)

val matchTitleTextStyls = TextStyle(
    fontSize = 21.sp,
    fontWeight = FontWeight(700),
)

val matchDescriptionPadding = PaddingValues(vertical = 3.dp, horizontal = 8.dp)
val matchDescriptionTextStyle = TextStyle(fontSize = 14.sp)

/**
 * Search Bar styles
 */
val searchBarTextStyle = TextStyle(
    fontSize = 20.sp,
)


