package com.stephenmorgandevelopment.thelinuxmanual.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.defaultTextStyle

val loadingAnnotatedString = stringFromRes(R.string.fetching_data)

private val charactersToSanitize = listOf<Char>(
    '!', ';', '&', '"', '#', '\'', '\\',
)

fun sanitizeInput(text: String): String {
    var mutableText: String = text
    charactersToSanitize.forEach { char -> mutableText.filter { it == char } }
    return mutableText
}

val String.queryAdjusted get() = if (length >= 4) "%$this%" else "$this%"

fun stringFromRes(id: Int) =
    Helpers.getApplicationContext().resources.getString(id)


@Composable
fun getString(id: Int): String {
    return LocalContext.current.resources.getString(id)
}

@Composable
fun getString(id: Int, vararg params: Any): String {
    return LocalContext.current.resources.getString(id, *params)
}

@Composable
fun makeComposableText(id: Int, style: TextStyle = defaultTextStyle): @Composable () -> Unit {
    return { Text(getString(id), style = style) }
}

fun TextLayoutResult.calculateScrollOffsetFor(charIndex: Int): Int {
    return getLineBaseline(
        getLineForOffset(charIndex)
    ).toInt()
}

private var syncTextCalls: Int = 0
fun showSyncText(title: String, syncProgress: String): String {
    val textDots = (++syncTextCalls % 3).let {
        return@let when (it) {
            0 -> "."
            1 -> ".."
            2 -> "..."
            else -> ""
        }
    }
    return "$title\n\nSyncing$textDots\n\n$syncProgress"
}
