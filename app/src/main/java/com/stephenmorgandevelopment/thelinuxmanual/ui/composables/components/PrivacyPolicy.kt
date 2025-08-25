package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchDescriptionTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.matchTitleTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.privacyPolicyTextPadding
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.privacyPolicyTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.privacyPolicyTextStyleBold
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.standardInfoTextStyle
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString

private const val UBUNTU_PRIVACY_POLICY = "https://ubuntu.com/legal/data-privacy"

private val viewUbuntuPrivacyPolicyIntent = Intent().apply {
    action = Intent.ACTION_VIEW
    data = UBUNTU_PRIVACY_POLICY.toUri()
}

@Composable
fun PrivacyPolicy() {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val context = LocalContext.current

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
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(matchTitleTextPadding),
                    text = getString(R.string.privacy_policy_button),
                    style = matchTitleTextStyle,
                )
            }

            SelectionContainer {
                Text(
                    modifier = Modifier.padding(privacyPolicyTextPadding),
                    text = getString(R.string.privacy_policy_one),
                    style = privacyPolicyTextStyleBold,
                )
            }

            SelectionContainer {
                Text(
                    modifier = Modifier.padding(privacyPolicyTextPadding),
                    text = getString(R.string.privacy_policy_two),
                    style = privacyPolicyTextStyle,
                )
            }

            SelectionContainer {
                Text(
                    modifier = Modifier
                        .padding(privacyPolicyTextPadding)
                        .clickable {
                            try {
                                backPressedDispatcher?.onBackPressed()
                                context.startActivity(viewUbuntuPrivacyPolicyIntent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(
                                    context.applicationContext,
                                    R.string.no_web_browser_found,
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        },
                    text = getString(R.string.ubuntu_privacy_policy_more_info),
                    style = standardInfoTextStyle,
                    color = Colors.webLinkBlue
                )
            }

            SelectionContainer {
                Text(
                    modifier = Modifier.padding(privacyPolicyTextPadding),
                    text = getString(R.string.privacy_policy_three),
                    style = matchDescriptionTextStyle,
                )
            }

            SelectionContainer {
                Text(
                    modifier = Modifier.padding(privacyPolicyTextPadding),
                    text = getString(R.string.privacy_policy_four),
                    style = matchDescriptionTextStyle,
                )
            }

            SelectionContainer {
                Text(
                    modifier = Modifier.padding(privacyPolicyTextPadding),
                    text = getString(R.string.privacy_policy_five),
                    style = matchDescriptionTextStyle,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewPrivacyPolicy() {
    PrivacyPolicy()
}
