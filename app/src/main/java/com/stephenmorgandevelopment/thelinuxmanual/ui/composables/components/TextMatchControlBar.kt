package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors

@Composable
fun TextMatchControlBar(
    modifier: Modifier,
    query: String,
    index: Int,
    count: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val matchCountText = if (count > 0) "${index.plus(1)}/$count" else "0/0"

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
                .padding(start = 8.dp),
            text = query,
            style = TextStyle(fontSize = 16.sp),
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
    )
}