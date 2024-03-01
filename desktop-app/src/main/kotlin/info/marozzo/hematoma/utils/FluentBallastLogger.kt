/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.utils

import com.copperleaf.ballast.BallastLogger
import com.google.common.flogger.FluentLogger
import com.google.common.flogger.LogSites
import com.google.common.flogger.MetadataKey

class FluentBallastLogger(private val tag: String) : BallastLogger {
    companion object {
        @JvmStatic
        val logger = FluentLogger.forEnclosingClass()!!

        @JvmStatic
        val tagKey = MetadataKey.single("tag", String::class.java)!!
    }

    override fun debug(message: String): Unit = logger.atFine()
        .withInjectedLogSite(LogSites.callerOf(FluentBallastLogger::class.java))
        .with(tagKey, tag)
        .log(message)

    override fun error(throwable: Throwable): Unit = logger.atSevere()
        .withInjectedLogSite(LogSites.callerOf(FluentBallastLogger::class.java))
        .with(tagKey, tag)
        .log()

    override fun info(message: String): Unit = logger.atInfo()
        .withInjectedLogSite(LogSites.callerOf(FluentBallastLogger::class.java))
        .with(tagKey, tag)
        .log(message)
}
