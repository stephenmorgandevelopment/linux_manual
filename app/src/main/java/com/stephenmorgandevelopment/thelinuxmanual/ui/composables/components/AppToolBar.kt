package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.appbarSubtitleStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.appbarTitleStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.LookupOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.ManPageOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.toolbarHeight

@Composable
fun AppToolbar(
    title: String,
    subTitle: String? = null,
    optionsMenu: @Composable () -> Unit,
) {
    val config = LocalConfiguration.current
    val orientation = remember(config.orientation) {
        config.orientation
    }

    ConstraintLayout(
        modifier = Modifier
            .background(Colors.darkGray)
            .padding(
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    WindowInsets.statusBars.asPaddingValues()
                } else {
                    WindowInsets.statusBars.add(WindowInsets.navigationBars).asPaddingValues()
                }
            )
            .height(toolbarHeight)
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    vertical = 0.dp,
                    horizontal = 8.dp,
                ),
            ),
    ) {
        val (text, menu) = createRefs()

        Column(
            modifier = Modifier.constrainAs(text) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(menu.start)

                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "$title:",
                style = appbarTitleStyle,
                color = Colors.offWhite,
                fontSize = if (subTitle.isNullOrEmpty()) 21.sp else 18.sp
            )

            subTitle?.let {
                Text(
                    text = it,
                    style = appbarSubtitleStyle,
                    color = Colors.offWhite,
                )
            }
        }

        Box(
            modifier = Modifier.constrainAs(menu) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(text.end)
                end.linkTo(parent.end)

                height = Dimension.wrapContent
                width = Dimension.wrapContent
            },
            contentAlignment = Alignment.CenterEnd
        ) {
            optionsMenu()
        }
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
