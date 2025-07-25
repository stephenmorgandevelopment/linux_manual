package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.utils.stringFromRes

@Composable
fun TextMatchControlBar(
    modifier: Modifier,
    query: String,
    index: Int,
    count: Int,
    showSearchBar: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val matchCountText by remember(index, count) {
        mutableStateOf(
            if (count > 0) "${index.plus(1)}/$count"
            else "0/0"
        )
    }

    val queryContentDescription by remember(query) {
        mutableStateOf(stringFromRes(R.string.query_change_content_description, query))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                shape = RoundedCornerShape(8.dp),
                color = Colors.offWhite,
            ),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .clearAndSetSemantics {
                    contentDescription = queryContentDescription
                }
                .clickable(onClick = showSearchBar)
                .padding(start = 8.dp),
            text = query,
            style = TextStyle(
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                textDecoration = TextDecoration.Underline,
            ),
            overflow = TextOverflow.MiddleEllipsis
        )

        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 10.dp),
            text = matchCountText,
            style = TextStyle(fontSize = 18.sp),
        )

        Surface(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 4.dp),
            onClick = onPrev,
            color = Colors.darkGray,
            contentColor = Colors.offWhite,
            shape = RoundedCornerShape(3.dp)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                text = "Prev",
                style = TextStyle(fontSize = 20.sp),
            )
        }

        Surface(
            modifier = Modifier
                .wrapContentSize()
                .padding(end = 8.dp, start = 8.dp),
            onClick = onNext,
            color = Colors.darkGray,
            contentColor = Colors.offWhite,
            shape = RoundedCornerShape(3.dp)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                text = "Next",
                style = TextStyle(fontSize = 20.sp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTextMatchControllerBar() {
    TextMatchControlBar(
        Modifier,
        "Matching Text",
        14,
        37,
        {},
        {},
        {},
    )
}
