package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.appbarTitleStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.optionsMenuItemPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.optionsMenuTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.releaseMenuItemStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.releaseOptionsItemPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.toolbarHeight
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString
import com.stephenmorgandevelopment.thelinuxmanual.utils.makeComposableText


@Composable
fun LookupOptionsMenu(
    releasesAvailable: List<String>,
    tabsOnBottom: Boolean = false,
    searchOnBottom: Boolean = false,
    onItemClicked: (OptionsMenuAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var releaseOptionsExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun closeMenu() {
        expanded = false
    }

    Box(
        modifier = Modifier
            .background(Colors.transparent)
            .wrapContentWidth()
            .height(toolbarHeight)
            .padding(0.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        SettingsButton { expanded = !expanded }

        DropdownMenu(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .heightIn(max = 550.dp)
                .padding(
                    horizontal = 5.dp,
                    vertical = 3.dp,
                ),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shadowElevation = 4.dp,
        ) {
            DropdownMenuItem(
                modifier = Modifier.padding(optionsMenuItemPadding),
                text = makeComposableText(R.string.resync_button, optionsMenuTextStyle),
                onClick = { onItemClicked(MainScreenOptionsMenuAction.ReSync); closeMenu() },
            )

            DropdownMenuItem(
                modifier = Modifier.padding(optionsMenuItemPadding),
                text = makeComposableText(R.string.tabs_on_bottom, optionsMenuTextStyle),
                onClick = { onItemClicked(MainScreenOptionsMenuAction.ToggleTabsOnBottom); closeMenu() },
                trailingIcon = {
                    if (tabsOnBottom) Icon(Icons.Filled.Check, "Enabled")
                    else null
                }
            )

            DropdownMenuItem(
                modifier = Modifier.padding(optionsMenuItemPadding),
                text = makeComposableText(R.string.search_on_bottom, optionsMenuTextStyle),
                onClick = { onItemClicked(MainScreenOptionsMenuAction.ToggleSearchOnBottom); closeMenu() },
                trailingIcon = {
                    if (searchOnBottom) Icon(Icons.Filled.Check, "Enabled")
                    else null
                }
            )

            DropdownMenuItem(
                modifier = Modifier.padding(optionsMenuItemPadding),
                text = makeComposableText(R.string.change_release_button, optionsMenuTextStyle),
                onClick = { releaseOptionsExpanded = !releaseOptionsExpanded },
                trailingIcon = {
                    if (releaseOptionsExpanded) Icon(
                        Icons.Filled.ExpandLess,
                        getString(R.string.hide_release_options)
                    )
                    else Icon(Icons.Filled.ExpandMore, getString(R.string.show_release_options))
                }
            )

            if (releaseOptionsExpanded) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 175.dp)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = 3.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    for (release in releasesAvailable) {
                        Text(
                            modifier = Modifier
                                .padding(releaseOptionsItemPadding)
                                .clickable(
                                    onClick = {
                                        onItemClicked(
                                            MainScreenOptionsMenuAction.ChangeVersion(
                                                release
                                            )
                                        )
                                        closeMenu()
                                    }
                                ),
                            text = release,
                            style = releaseMenuItemStyle
                        )
                    }
                }
            }

            DropdownMenuItem(
                modifier = Modifier.padding(optionsMenuItemPadding),
                text = makeComposableText(R.string.privacy_policy_button, optionsMenuTextStyle),
                onClick = {
                    onItemClicked(MainScreenOptionsMenuAction.ShowPrivacyPolicyDialog)
                    closeMenu()
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewLookupOptionsMenu() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(toolbarHeight)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Ubuntu Man Pages",
            style = appbarTitleStyle,
        )

        LookupOptionsMenu(AvailableReleases.releaseStrings) { }
    }
}

@Preview(heightDp = 600)
@Composable
private fun PreviewLookupOptionsMenuExpanded() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(64.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Ubuntu Man Pages",
                style = appbarTitleStyle,
            )

            LookupOptionsMenu(AvailableReleases.releaseStrings) { }
        }
    }

}