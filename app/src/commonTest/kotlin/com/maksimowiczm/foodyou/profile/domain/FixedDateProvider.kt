package com.maksimowiczm.foodyou.profile.domain

import com.maksimowiczm.foodyou.common.domain.date.DateProvider
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Clock stub whose "now" is set by the test, so timestamp assertions stay deterministic. */
internal class FixedDateProvider(var epochSeconds: Long) : DateProvider {
    override fun nowInstant(): Instant = Instant.fromEpochSeconds(epochSeconds)

    override fun observeInstant(interval: Duration): Flow<Instant> = flowOf(nowInstant())

    override fun observeDate(timeZone: TimeZone): Flow<LocalDate> =
        flowOf(nowInstant().toLocalDateTime(timeZone).date)
}
