package com.stephenmorgandevelopment.thelinuxmanual.ui

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.BaseViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.icons


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaseScreen(viewModel: BaseViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBarsIgnoringVisibility,
        topBar = { AppToolbar() },
    ) { paddingValues ->



    }
}

@Composable
fun AppToolbar() {
    Row(
        modifier = Modifier.requiredHeight(64.dp)
            .fillMaxWidth()
            .padding(PaddingValues(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(), bottom = 12.dp, start = 8.dp, end = 8.dp))
    ) {  }
}

@Composable
fun OptionsMenuButton(
    modifier: Modifier,
    title: String,
    icon: Icon,
    onClick: () -> Unit,
) {

}

