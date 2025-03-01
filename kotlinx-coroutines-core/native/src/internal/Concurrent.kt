/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.internal

import kotlinx.atomicfu.*
import kotlinx.cinterop.*
import kotlinx.atomicfu.locks.withLock as withLock2

@Suppress("ACTUAL_WITHOUT_EXPECT")
internal actual typealias ReentrantLock = kotlinx.atomicfu.locks.SynchronizedObject

@OptIn(UnsafeNumber::class)
internal actual inline fun <T> ReentrantLock.withLock(action: () -> T): T = this.withLock2(action)

internal actual fun <E> identitySet(expectedSize: Int): MutableSet<E> = HashSet()


// "Suppress-supporting throwable" is currently used for tests only
internal open class SuppressSupportingThrowableImpl : Throwable() {
    private val _suppressed = atomic<Array<Throwable>>(emptyArray())

    val suppressed: Array<Throwable>
        get() = _suppressed.value

    fun addSuppressed(other: Throwable) {
        _suppressed.update { current ->
            current + other
        }
    }
}

@Suppress("ACTUAL_WITHOUT_EXPECT") // This suppress can be removed in 2.0: KT-59355
internal actual typealias BenignDataRace = kotlin.concurrent.Volatile
