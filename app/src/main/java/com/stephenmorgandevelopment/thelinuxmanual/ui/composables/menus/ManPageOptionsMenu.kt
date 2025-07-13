package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.appbarTitleStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.releaseMenuItemStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.toolbarHeight

@Composable
fun ManPageOptionsMenu(
    manPageName: String,
    sections: List<String>,
    onItemClicked: (OptionsMenuAction) -> Unit,
) {
    var jumpToExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .background(Colors.transparent)
            .wrapContentWidth()
            .height(toolbarHeight)
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {

        JumpToSectionButton { jumpToExpanded = !jumpToExpanded }
        JumpToDropdown(
            jumpToExpanded,
            sections,
            { jumpToExpanded = false },
        ) {
            onItemClicked(ManPageOptionsMenuAction.JumpTo(it))
            jumpToExpanded = false
        }

        SearchButton(manPageName) { onItemClicked(ManPageOptionsMenuAction.ToggleSearch) }
        CloseButton(manPageName) { onItemClicked(ManPageOptionsMenuAction.Close) }
    }

}

@Composable
private fun JumpToDropdown(
    expanded: Boolean,
    sections: List<String>,
    onDismissed: () -> Unit,
    onItemClicked: (String) -> Unit,
) {
    val scrollState = rememberScrollState()

    DropdownMenu(
        modifier = Modifier.heightIn(max = 450.dp),
        expanded = expanded,
        onDismissRequest = onDismissed,
        scrollState = scrollState,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 1.dp,
    ) {

        for (section in sections) {
            DropdownMenuItem(
                text = { Text(text = section, fontSize = 16.sp, style = releaseMenuItemStyle) },
                onClick = { onItemClicked(section) }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLookupOptionsMenu() {
    val sections = listOf<String>("first", "second", "third", "forth")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "dumb shit",
            style = appbarTitleStyle,
        )

        ManPageOptionsMenu(
            "random",
            sections,
        ) { }
    }
}
