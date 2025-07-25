package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

/**
 * Circular loading indicator that will center itself in parent container automatically, when no
 *   modifier is passed in.  If passing in a modifier, it will center itself within the bounds
 *   created by sizing modifier's passed in.
 *
 *   If default sizing is desired with the use of non sizing modifiers, add `fillMaxSize()` to the
 *   modifier chain.
 *
 *   @param modifier: Modifier
 */
@Composable
fun LoadingIndicator(
    // We want this to fill the parent (for centering) when we don't pass a modifier in, but we want to
    //  make sure not to interfere with other sizing modifiers when we do.
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier.fillMaxSize(),
) {

    Box(
        modifier = modifier.zIndex(10_000f),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
