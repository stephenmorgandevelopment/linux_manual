package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchDescriptionPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchDescriptionTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchingTextSpanStyle
import com.stephenmorgandevelopment.thelinuxmanual.utils.MockObjects
import com.stephenmorgandevelopment.thelinuxmanual.utils.calculateScrollOffsetFor

@Composable
fun ManPageSection(
    name: String,
    data: String,
    singleTextMatch: SingleTextMatch? = null,
    onTextMatchedOffset: ((Int) -> Unit)? = null,
) {
    val annotatedString = remember(singleTextMatch?.startIndex, singleTextMatch?.endIndex) {
        singleTextMatch?.let {
            buildAnnotatedString {
                with(AnnotatedString.fromHtml(data)) {
                    append(subSequence(0, it.startIndex))
                    withStyle(matchingTextSpanStyle) {
                        append(
                            subSequence(
                                it.startIndex,
                                it.endIndex
                            )
                        )
                    }
                    append(subSequence(it.endIndex, length))
                }
            }
        } ?: AnnotatedString.fromHtml(data)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = Colors.offWhite,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            modifier = Modifier.padding(matchTitleTextPadding),
            text = name,
            style = matchTitleTextStyle,
        )

        Text(
            modifier = Modifier.padding(matchDescriptionPadding),
            text = annotatedString,
            style = matchDescriptionTextStyle,
            onTextLayout = { layout ->
                singleTextMatch?.let {
                    onTextMatchedOffset?.invoke(
                        layout.calculateScrollOffsetFor(it.startIndex)
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun PreviewManPageSection() {
    ManPageSection(
        "Description",
        MockObjects.longDescription,
        null,
        {}
    )
}

@Preview
@Composable
private fun PreviewManPageSectionWithTextMatch() {
    ManPageSection(
        "Description",
        MockObjects.longDescription,
        SingleTextMatch(
            "Description",
            49,
            58,
        ),
    ) {}
}
