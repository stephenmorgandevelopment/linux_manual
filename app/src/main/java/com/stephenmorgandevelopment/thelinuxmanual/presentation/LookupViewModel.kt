package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.domain.GetPartialMatchesUseCase
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction.UpdateSearchText
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.queryAdjusted
import com.stephenmorgandevelopment.thelinuxmanual.utils.sanitizeInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.min

@HiltViewModel
class LookupViewModel @Inject constructor(
    private val ubuntuRepository: UbuntuRepository,
    private val getPartialMatchesUseCase: GetPartialMatchesUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LookupState, LookupAction>(savedStateHandle) {

    private val restored = savedStateHandle.get<LookupState>(FULL_STATE_KEY)
        ?: LookupState("", emptyList())

    private val _searchText = MutableStateFlow<String>(restored.searchText)
    private val _matches: MutableStateFlow<List<MatchingItem>> = MutableStateFlow(restored.matches)

    override val state: StateFlow<LookupState> =
        combine(_searchText, _matches) { searchText, matches ->
            LookupState(searchText, matches).also { persistState(it) }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(15_000L),
            restored,
        )

    override fun onAction(action: LookupAction) {
        when (action) {
            is UpdateSearchText -> processTextInput(sanitizeInput(action.text))
        }
    }

    private var textProcessJob: Job? = null
    private fun processTextInput(text: String) {
        _searchText.value = text

        textProcessJob?.cancel()
        textProcessJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                findMatches(text)
            }
        }
    }

    // TODO:  Potential race condition issue where a job started in global scope
    //  before user finishes typing, gets launched a second time, via addDescription,
    //  when the user finishes typing.

    // Creating these in global scope because if we start these jobs, we want them to finish.
    @OptIn(DelicateCoroutinesApi::class)
    private fun addDescription(matchingItem: MatchingItem) = GlobalScope.async(
        Dispatchers.IO,
        start = CoroutineStart.LAZY,
    ) {
        ubuntuRepository.addDescription(matchingItem)
    }.apply {
        invokeOnCompletion { e ->
            javaClass.ilog(
                "addDescription job completed for ${matchingItem.name} with " +
                        "error: ${e?.message}"
            )
        }
    }

    private var scopedJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun findMatches(searchText: String) {
        scopedJob?.cancel()
        when {
            searchText.length < 2 || searchText.isBlank() ->
                _matches.value = emptyList<MatchingItem>()

            else -> getPartialMatchesUseCase(searchText.queryAdjusted).let { partialMatches ->
                _matches.value = partialMatches

                scopedJob = viewModelScope.async(Dispatchers.Default) {
                    delay(120) // to reduce possibilities of above mentioned race condition.

                    val jobs = partialMatches.mapNotNull { match ->
                        if (match.needsDescription) addDescription(match)
                        else null
                    }.toMutableList()

                    while (jobs.isNotEmpty()) {
                        val takeCount = min(12, jobs.size)

                        val updatedMatches = mutableListOf<MatchingItem>()
                        jobs.take(takeCount)
                            .onEach { jobInst ->
                                if (!jobs.remove(jobInst)) javaClass.ilog("failed removing $jobInst from jobs list.")
                                jobInst.apply {
                                    invokeOnCompletion {
                                        updatedMatches.add(getCompleted())
                                    }
                                }.start()
                            }.joinAll()

                        val mutatedMatches = _matches.value.map { match ->
                            updatedMatches.firstOrNull { it.id == match.id } ?: match
                        }

                        _matches.value = mutatedMatches
                    }
                }
            }
        }
    }
}
