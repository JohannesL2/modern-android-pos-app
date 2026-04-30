package com.example.integration.internal.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * awaitEvent
 *
 * Väntar på första värdet i ett Flow som matchar predicate.
 * Returnerar true om eventet inträffar inom timeout, annars false.
 */
internal suspend fun <T> awaitEvent(
    flow: Flow<T>,
    timeoutMillis: Long,
    predicate: (T) -> Boolean
): Boolean =
    withTimeoutOrNull(timeoutMillis) {
        flow.first { predicate(it) }
        true
    } ?: false