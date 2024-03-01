/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.utils

import com.copperleaf.ballast.BallastInterceptor
import com.copperleaf.ballast.BallastInterceptorScope
import com.copperleaf.ballast.BallastNotification
import com.google.common.flogger.FluentLogger
import com.google.common.flogger.MetadataKey
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FluentLoggingInterceptor<Inputs : Any, Events : Any, State : Any> : BallastInterceptor<Inputs, Events, State> {

    companion object {
        private val flogger = FluentLogger.forEnclosingClass()!!
        private val modelNameKey = MetadataKey.single("name", String::class.java)!!
        private val modelTypeKey = MetadataKey.single("type", String::class.java)!!
        private val sideJobKey = MetadataKey.single("sidejob", String::class.java)!!
    }

    override fun BallastInterceptorScope<Inputs, Events, State>.start(notifications: Flow<BallastNotification<Inputs, Events, State>>) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            var latestState: State? = null

            notifications.collect { notification ->
                when (notification) {
                    is BallastNotification.ViewModelStatusChanged -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("View-Model Status changed to %s", notification.status)

                    is BallastNotification.InputQueued -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Input %s queued", notification.input)

                    is BallastNotification.InputAccepted -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Input %s accepted", notification.input)

                    is BallastNotification.InputRejected -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Input %s rejected while in state %s", notification.input, notification.stateWhenRejected)

                    is BallastNotification.InputDropped -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Input %s dropped", notification.input)

                    is BallastNotification.InputHandledSuccessfully -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Input %s handled successfully", notification.input)

                    is BallastNotification.InputCancelled -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Input %s cancelled", notification.input)

                    is BallastNotification.InputHandlerError -> flogger.atWarning()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .withCause(notification.throwable)
                        .log("Error while handling input %s in state %s", notification.input, latestState)

                    is BallastNotification.EventQueued -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Event %s queued", notification.event)

                    is BallastNotification.EventEmitted -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Event %s emitted", notification.event)

                    is BallastNotification.EventHandledSuccessfully -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Event %s handled successfully", notification.event)

                    is BallastNotification.EventHandlerError -> flogger.atWarning()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .withCause(notification.throwable)
                        .log("Error while handling event %s in state %s", notification.event, latestState)

                    is BallastNotification.EventProcessingStarted -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Event processing started")

                    is BallastNotification.EventProcessingStopped -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Event processing stopped")

                    is BallastNotification.StateChanged -> {
                        latestState = notification.state
                        flogger.atInfo()
                            .with(modelNameKey, hostViewModelName)
                            .with(modelTypeKey, hostViewModelType)
                            .log("State changed to %s", notification.state)
                    }

                    is BallastNotification.SideJobQueued -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .with(sideJobKey, notification.key)
                        .log("Side-Job %s queued", notification.key)

                    is BallastNotification.SideJobStarted -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .with(sideJobKey, notification.key)
                        .log("Side-Job %s started: %s", notification.key, notification.restartState)

                    is BallastNotification.SideJobCompleted -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .with(sideJobKey, notification.key)
                        .log("Side-Job %s completed successfully", notification.key)

                    is BallastNotification.SideJobCancelled -> flogger.atInfo()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .with(sideJobKey, notification.key)
                        .log("Side-Job %s cancelled", notification.key)

                    is BallastNotification.SideJobError -> flogger.atWarning()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .with(sideJobKey, notification.key)
                        .withCause(notification.throwable)
                        .log("Error in side-job %s", notification.key)

                    is BallastNotification.InterceptorAttached -> flogger.atFine()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .log("Interceptor %s attached", notification.interceptor.javaClass)

                    is BallastNotification.InterceptorFailed -> flogger.atWarning()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .withCause(notification.throwable)
                        .log("Error in interceptor %s", notification.interceptor.javaClass)

                    is BallastNotification.UnhandledError -> flogger.atSevere()
                        .with(modelNameKey, hostViewModelName)
                        .with(modelTypeKey, hostViewModelType)
                        .withCause(notification.throwable)
                        .log("An unhandled error occurred")
                }
            }
        }
    }


}
