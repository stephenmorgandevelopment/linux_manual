package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.appbarTitleStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.LookupOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.ManPageOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.toolbarHeight

@Composable
fun AppToolbar(
    title: String,
    optionsMenu: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(Colors.darkGray)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .height(toolbarHeight)
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    vertical = 0.dp,
                    horizontal = 8.dp,
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = appbarTitleStyle,
            color = Colors.offWhite,
        )

        optionsMenu()
    }
}

@Preview(heightDp = 600)
@Composable
private fun PreviewAppToolbarLookupMenu() {
    Box(contentAlignment = Alignment.TopCenter) {
        AppToolbar("Blah") {
            LookupOptionsMenu(
                AvailableReleases.releaseStrings,
                false,
                false,
            ) { }
        }
    }
}

@Preview(heightDp = 600)
@Composable
private fun PreviewAppToolbarManPageMenu() {
    Box(contentAlignment = Alignment.TopCenter) {
        AppToolbar("Blah") {
            ManPageOptionsMenu(
                "for accessibility",
                AvailableReleases.releaseStrings,
            ) { }
        }
    }
}
