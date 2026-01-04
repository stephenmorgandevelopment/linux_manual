package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stephenmorgandevelopment.thelinuxmanual.utils.showSyncText

@Composable
internal fun SyncText(
    progressString: String,
    padding: PaddingValues,
) {
    val syncProgressScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        Text(
            text = showSyncText(progressString),
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .verticalScroll(syncProgressScrollState),
            style = TextStyle(fontSize = 18.sp),
        )
    }
}