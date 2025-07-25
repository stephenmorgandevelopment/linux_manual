package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchDescriptionPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchDescriptionTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchListItemPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.utils.loadingString

@Composable
fun MatchingListItem(
    name: String,
    description: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = Colors.offWhite,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(matchListItemPadding)
        ) {
            Text(
                modifier = Modifier.padding(matchTitleTextPadding),
                style = matchTitleTextStyle,
                text = name,
            )

            Text(
                modifier = Modifier.padding(matchDescriptionPadding),
                style = matchDescriptionTextStyle,
                text =
                    if (description.isNullOrBlank()) loadingString
                    else description,
                maxLines = 3,
                minLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
