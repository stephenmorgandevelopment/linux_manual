package com.stephenmorgandevelopment.thelinuxmanual.domain

import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSectionsById @Inject constructor(
    private val repo: UbuntuRepository,
) {
    suspend operator fun invoke(id: Long): List<String> = withContext(Dispatchers.IO) {
        repo.getMatchingItemById(id)?.sections ?: emptyList()
    }
}