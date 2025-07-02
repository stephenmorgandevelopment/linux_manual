package com.stephenmorgandevelopment.thelinuxmanual.domain

import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPartialMatchesUseCase @Inject constructor(
    private val roomDatabase: SimpleCommandsDatabase,
) {
    operator fun invoke(searchText: String): Flow<List<MatchingItem>> =
        roomDatabase.dao().partialMatches(searchText)
}