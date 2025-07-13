package com.stephenmorgandevelopment.thelinuxmanual.presentation
//
//import android.os.Bundle
//import androidx.core.os.bundleOf
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.savedstate.SavedStateRegistry
//import androidx.savedstate.SavedStateRegistryOwner
//import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
//import com.stephenmorgandevelopment.thelinuxmanual.utils.vlog
//
//class ManPageSearchManager(
//    private val id: Long,
////    private val registryOwner: SavedStateRegistryOwner,
//) { //: SavedStateRegistry.SavedStateProvider, LifecycleEventObserver {
//    companion object {
//        val PROVIDER: (Long) -> String = { "search_manager_$it" }
//        private const val QUERY = "query"
//        private const val MATCHES = "matches"
//        private const val VISIBLE = "visible"
//        private const val INDEX = "index"
//    }
//
//    val providerId: String = PROVIDER(id)
//
//    var visible: Boolean = false
//    var query: String = ""
//    var results: TextSearchResult? = null
//    var index: Int = 0
//
////    override fun onStateChanged(
////        source: LifecycleOwner,
////        event: Lifecycle.Event,
////    ) {
////        javaClass.vlog("Lifecycle changed - source: $source - event: $event")
////
////        if (event == Lifecycle.Event.ON_CREATE) {
////            val registry = registryOwner.savedStateRegistry
////
////            registry.registerSavedStateProvider(PROVIDER(id), this)
////            val state = registry.consumeRestoredStateForKey(PROVIDER(id))
////
////            query = state?.getString(QUERY) ?: ""
////            visible = state?.getBoolean(VISIBLE) ?: false
////            results = state?.getParcelable(MATCHES)
////            index = state?.getInt(INDEX) ?: 0
////        }
////    }
//
////    init {
////        registryOwner.lifecycle.addObserver(this)
////    }
////
////    override fun saveState(): Bundle {
////        return toBundle()
////    }
////
////    fun toBundle() = bundleOf(
////        VISIBLE to visible,
////        QUERY to query,
////        MATCHES to results,
////        INDEX to index,
////    )
//
//    fun toParcelable(): ManPageSearchState = ManPageSearchState(
//        id, visible, query, results, index,
//    )
//}