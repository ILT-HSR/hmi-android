package ch.hsr.ifs.gcs.support.concurrent

import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

fun ScheduledExecutorService.every(interval: Duration, block: () -> Unit) {
    this.scheduleAtFixedRate(block, 0, interval.toNanos(), TimeUnit.NANOSECONDS)
}