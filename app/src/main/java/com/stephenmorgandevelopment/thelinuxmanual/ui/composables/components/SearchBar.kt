@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.searchBarTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull

@Composable
fun SearchBar(
    text: String,
    modifier: Modifier,
    label: String = getString(R.string.search_command),
    onTextUpdate: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = modifier
            .padding(3.dp),
        value = text,
        onValueChange = {
            onTextUpdate(it)
        },
        singleLine = true,
        textStyle = searchBarTextStyle,
        shape = RoundedCornerShape(8.dp),
        label = { Text(label) },
        colors = TextFieldDefaults.colors(),
    )
}

@Composable
fun SearchBarWithButton(
    modifier: Modifier,
    iconContentDescription: String,
    label: String = getString(R.string.search_man_page),
    searchText: String,
    searchResults: TextSearchResult?,
    onAction: (ManPageAction) -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = if (searchResults.isNull()) 8.dp else 4.dp,
            )
            .background(
                shape = RoundedCornerShape(12.dp),
                color = Colors.offWhite
            ),
    ) {
        val (field, button) = createRefs()

        SearchBar(
            text = searchText,
            modifier = Modifier
                .constrainAs(field) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(button.start)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .padding(horizontal = 3.dp, vertical = 5.dp),
            label = label,
        ) { onAction(ManPageAction.OnSearchTextUpdated(it)) }

        IconButton(
            modifier = Modifier
                .constrainAs(button) {
                    top.linkTo(field.top)
                    start.linkTo(field.end)
                    end.linkTo(parent.end)
                    bottom.linkTo(field.bottom)
                    width = Dimension.wrapContent
                    height = Dimension.fillToConstraints
                }
                .padding(top = 12.dp, bottom = 4.dp, start = 0.dp, end = 5.dp),
            onClick = {
                onAction(ManPageAction.OnSearchPressed)
            },
            colors = IconButtonColors(
                containerColor = Colors.offWhite,
                contentColor = Colors.black,
                disabledContentColor = Colors.offWhite,
                disabledContainerColor = Colors.transparent,
            )
        ) {
            Icon(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
                imageVector = Icons.Filled.Search,
                contentDescription = iconContentDescription,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSearchBar() {
    SearchBar(
        "",
        Modifier,
    ) { }
}
