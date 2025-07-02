package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.optionButtonHeight
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.optionButtonPadding
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString

@Composable
fun OptionsMenuButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .padding(optionButtonPadding)
            .requiredHeight(optionButtonHeight)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .semantics {
                role = Role.Button
            },
        onClick = onClick,
        colors = IconButtonColors(
            containerColor = Colors.transparent,
            contentColor = Colors.offWhite,
            disabledContentColor = Colors.offWhite,
            disabledContainerColor = Colors.transparent,
        )
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            imageVector = imageVector,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun JumpToSectionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OptionsMenuButton(
        modifier = modifier,
        imageVector = Icons.Outlined.ExpandCircleDown,
        contentDescription = getString(R.string.jump_to_section),
        onClick = onClick,
    )
}

@Composable
fun SearchButton(
    tabTitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OptionsMenuButton(
        modifier = modifier,
        imageVector = Icons.Outlined.Search,
        contentDescription = getString(R.string.search_text_in_page, tabTitle),
        onClick = onClick,
    )
}

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OptionsMenuButton(
        modifier = modifier,
        imageVector = Icons.Outlined.Settings,
        contentDescription = getString(R.string.app_settings),
        onClick = onClick,
    )
}

@Composable
fun CloseButton(
    tabTitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OptionsMenuButton(
        modifier = modifier,
        imageVector = Icons.Outlined.Close,
        contentDescription = getString(R.string.close_tab, tabTitle),
        onClick = onClick,
    )
}

private const val buttonGroup = "Individual Buttons"
private const val buttonHeight = 64

@Preview(group = buttonGroup, heightDp = buttonHeight)
@Composable
private fun PreviewCloseButton() {
    CloseButton("ffmpeg") { }
}

@Preview(group = buttonGroup, heightDp = buttonHeight)
@Composable
private fun PreviewSettingsButton() {
    SettingsButton { }
}

@Preview(group = buttonGroup, heightDp = buttonHeight)
@Composable
private fun PreviewSearchButton() {
    SearchButton("ffmpeg") { }
}

@Preview(group = buttonGroup, heightDp = buttonHeight)
@Composable
private fun PreviewJumpToSectionButton() {
    JumpToSectionButton { }
}