package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Colors {
    val transparent = Color(0x00000000)
    val offWhite = Color(0xFFe0e0e0)
    val black = Color(0xFF000000)
    val darkGray = Color(0xFF222222)
    val semiDarkGray = Color(0xFF4a4a4a)
    val blueish = Color(0xFF1843a9)
    val indicatorColor = Color(0xFFdd9b1a)
    val searchForeground = Color(0xFFFBA700)
    val searchBackground = Color(0xFF1b1b1b)
}

@Composable
fun UbuntuManPageTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Colors.darkGray,
            onPrimary = Colors.offWhite,
            primaryContainer = Colors.semiDarkGray,
            onPrimaryContainer = Colors.offWhite,
            secondary = Colors.blueish,
            tertiary = Colors.indicatorColor,
            tertiaryContainer = Colors.offWhite,
        )
    ) { content() }
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

val optionButtonPadding = PaddingValues(vertical = 8.dp, horizontal = 1.dp)

val releaseOptionsItemPadding = PaddingValues(
    horizontal = 12.dp,
    vertical = 8.dp,
)

val optionsMenuItemPadding = PaddingValues(vertical = 5.dp, horizontal = 3.dp)

val toolbarHeight = 68.dp
val appbarTitleStyle = TextStyle(
    fontWeight = FontWeight(625),
    fontSize = 29.sp,
    textAlign = TextAlign.Start,
)

val appbarSubtitleStyle = TextStyle(
    fontWeight = FontWeight(450),
    fontSize = 17.sp,
    textAlign = TextAlign.Start,
    textIndent = TextIndent(3.sp)
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
    start = 12.dp,
)

val matchTitleTextStyle = TextStyle(
    fontSize = 21.sp,
    fontWeight = FontWeight(700),
)

val matchDescriptionPadding = PaddingValues(vertical = 3.dp, horizontal = 8.dp)
val matchDescriptionTextStyle = TextStyle(fontSize = 16.sp)

//val matchDescriptionTotalPadding = 56.dp
/**
 * Privacy Policy styles
 */
val privacyPolicyTextPadding = PaddingValues(vertical = 12.dp, horizontal = 5.dp)
val privacyPolicyTextStyle = TextStyle(fontSize = 16.sp)
val privacyPolicyTextStyleBold = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(700))

/**
 * Search Bar styles
 */
val searchBarTextStyle = TextStyle(
    fontSize = 20.sp,
)

/**
 * Search reuslts styles
 */
val matchingTextSpanStyle = SpanStyle(
    color = Colors.searchForeground,
    background = Colors.searchBackground,
)
