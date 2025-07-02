package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.appbarTitleStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.LookupOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString

@Composable
fun AppToolbar(
    releaseStrings: List<String>,
    showTabsOnBottom: Boolean,
    showSearchOnBottom: Boolean,
    onOptionItemClicked: (MainScreenAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(Colors.darkGray)
            .requiredHeight(64.dp)
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        .plus(4.dp),
                    bottom = 4.dp,
                    start = 8.dp,
                    end = 8.dp,
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = getString(R.string.app_name),
            style = appbarTitleStyle,
            color = Colors.offWhite,
        )

        LookupOptionsMenu(
            releaseStrings,
            showTabsOnBottom,
            showSearchOnBottom,
            onOptionItemClicked,
        )
    }
}