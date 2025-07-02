package com.stephenmorgandevelopment.thelinuxmanual

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.BaseScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val activityViewModel: ActivityViewModel by viewModels()
//    private val lookupViewModel: LookupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalLifecycleOwner provides this,
                LocalActivity provides this,
                LocalViewModelStoreOwner provides this,
            ) {
                BaseScreen(activityViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isFinishing) {
            if (!CommandSyncService.isWorking()) {
                activityViewModel.onAction(MainScreenAction.CloseDatabase)
            }
        }
    }

}