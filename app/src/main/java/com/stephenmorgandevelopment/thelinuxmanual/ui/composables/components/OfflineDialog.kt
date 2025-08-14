package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.privacyPolicyTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.standardInfoPadding
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString

@Composable
fun OfflineDialog() {
    Box(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
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
                text = getString(R.string.offline_header),
                style = matchTitleTextStyle,
            )

            Text(
                modifier = Modifier.padding(standardInfoPadding),
                text = getString(R.string.you_are_offline),
                style = privacyPolicyTextStyle,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPrivacyPolicy() {
    OfflineDialog()
}
