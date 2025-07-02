package com.stephenmorgandevelopment.thelinuxmanual.domain

import android.util.Log
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.rx2.await
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.toList
import kotlin.math.roundToInt

class AddDescriptionUseCase @Inject constructor(
    private val roomDatabase: SimpleCommandsDatabase,
) {
    operator fun invoke(noDescriptionMatch: MatchingItem) {

    }
}


//@Singleton
//class AddDescriptionHandler @Inject constructor(
//    private val ubuntuRepository: UbuntuRepository,
//) {
//    private val jobsQueue = mutableListOf<Deferred<SimpleCommand>>()
//    private val allQueuedIds: MutableList<Long> = mutableListOf()
//
//    private val descriptionDispatcherThreadCount =
//        (Runtime.getRuntime().availableProcessors() * .425).roundToInt()
//
//    private val addDescriptionDispatcher =
//        Executors.newFixedThreadPool(descriptionDispatcherThreadCount)
//            .asCoroutineDispatcher()
//
//    private val addDescriptionScope = CoroutineScope(addDescriptionDispatcher)
//
//    private val canStartDescriptionJob
//        get() = jobsQueue.toList().filter { job -> job.isActive }
//            .size <= (descriptionDispatcherThreadCount*2)
//
//    private val hasUnstartedJobs
//        get() = jobsQueue.toList().any { job -> !job.isActive && !job.isCompleted && !job.isCancelled }
//
//    private val firstUnstartedJob
//        get() = jobsQueue.toList().firstOrNull { job ->
//            !job.isActive && !job.isCompleted && !job.isCancelled
//        }
//
//    fun queue(simpleCommand: SimpleCommand): Flow<SimpleCommand> {
//        val mutableStateFlow = MutableStateFlow<SimpleCommand>(simpleCommand)
//
//        if (!allQueuedIds.contains(simpleCommand.id)) {
//            javaClass.ilog("addToAllQueuedIds-${simpleCommand.name} via queue")
//            allQueuedIds.add(simpleCommand.id)
//            queueAsyncJob(simpleCommand, mutableStateFlow)
//        } else {
//            javaClass.ilog("bounced-${simpleCommand.name} from allQueuedIds.")
//        }
//
//        return mutableStateFlow.asStateFlow()
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    private fun queueAsyncJob(
//        simpleCommand: SimpleCommand,
//        commandUpdateFlow: MutableStateFlow<SimpleCommand>
//    ) {
//            addDescriptionScope.async(start = CoroutineStart.LAZY) {
//                ubuntuRepository.addDescription(simpleCommand).await()
//            }.let { descriptionJob ->
//                descriptionJob.apply {
//                    invokeOnCompletion { _ ->
//                         jobsQueue.let { it.removeAt(it.indexOf(descriptionJob)) }
//                                .getCompleted().let {
//                                    commandUpdateFlow.tryEmit(it).also {
//                                        javaClass.ilog("tryEmit-${simpleCommand.name} via invokeOnCompletion")
//                                    }
//                                }
//
//                        if (canStartDescriptionJob) {
//                            firstUnstartedJob?.also {
//                                javaClass.ilog("startJob-${simpleCommand.name} via invokeOnCompletion")
//                            }?.start()
//                        }
//                        if (!hasUnstartedJobs && jobsQueue.isEmpty()) {
//                            javaClass.ilog("jobsQueue is empty, clearing ids queue.")
//                            allQueuedIds.clear()
//                        }
//                    }
//                }
//
//                jobsQueue.add(descriptionJob)
//                if (canStartDescriptionJob) {
//                    descriptionJob.start().also {
//                        javaClass.ilog("startJob-${simpleCommand.name} via directly}")
//                    }
//                }
//            }
//    }
//
////    @OptIn(DelicateCoroutinesApi::class)
////    private fun processQueue() {
////        if (managingJob?.isActive == true) return
////
////        managingJob = GlobalScope.launchCompletable {
////            withContext(Dispatchers.IO) {
////                while (jobsQueue.isNotEmpty()) {
////                    while (hasUnstartedJobs && canStartDescriptionJob) {
////                        firstUnstartedJob?.start()
////                    }
////                }
////            }
////        }.apply { invokeOnCompletion { managingJob = null } }
////    }
//
//
//}