/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import kotlinx.browser.*
import kotlinx.coroutines.internal.*
import kotlin.coroutines.*

private external val navigator: dynamic
private const val UNDEFINED = "undefined"
internal external val process: dynamic

internal actual fun createDefaultDispatcher(): CoroutineDispatcher = when {
    // Check if we are running under jsdom. WindowDispatcher doesn't work under jsdom because it accesses MessageEvent#source.
    // It is not implemented in jsdom, see https://github.com/jsdom/jsdom/blob/master/Changelog.md
    // "It's missing a few semantics, especially around origins, as well as MessageEvent source."
    isJsdom() -> NodeDispatcher
    // Check if we are in the browser and must use window.postMessage to avoid setTimeout throttling
    jsTypeOf(window) != UNDEFINED && window.asDynamic() != null && jsTypeOf(window.asDynamic().addEventListener) != UNDEFINED ->
        window.asCoroutineDispatcher()
    // If process is undefined (e.g. in NativeScript, #1404), use SetTimeout-based dispatcher
    jsTypeOf(process) == UNDEFINED || jsTypeOf(process.nextTick) == UNDEFINED -> SetTimeoutDispatcher
    // Fallback to NodeDispatcher when browser environment is not detected
    else -> NodeDispatcher
}

private fun isJsdom() = jsTypeOf(navigator) != UNDEFINED &&
    navigator != null &&
    navigator.userAgent != null &&
    jsTypeOf(navigator.userAgent) != UNDEFINED &&
    jsTypeOf(navigator.userAgent.match) != UNDEFINED &&
    navigator.userAgent.match("\\bjsdom\\b")

@PublishedApi // Used from kotlinx-coroutines-test via suppress, not part of ABI
internal actual val DefaultDelay: Delay
    get() = Dispatchers.Default as Delay

public actual fun CoroutineScope.newCoroutineContext(context: CoroutineContext): CoroutineContext {
    val combined = coroutineContext + context
    return if (combined !== Dispatchers.Default && combined[ContinuationInterceptor] == null)
        combined + Dispatchers.Default else combined
}

public actual fun CoroutineContext.newCoroutineContext(addedContext: CoroutineContext): CoroutineContext {
    return this + addedContext
}

// No debugging facilities on JS
internal actual inline fun <T> withCoroutineContext(context: CoroutineContext, countOrElement: Any?, block: () -> T): T = block()
internal actual inline fun <T> withContinuationContext(continuation: Continuation<*>, countOrElement: Any?, block: () -> T): T = block()
internal actual fun Continuation<*>.toDebugString(): String = toString()
internal actual val CoroutineContext.coroutineName: String? get() = null // not supported on JS

internal actual class UndispatchedCoroutine<in T> actual constructor(
    context: CoroutineContext,
    uCont: Continuation<T>
) : ScopeCoroutine<T>(context, uCont) {
    override fun afterResume(state: Any?) = uCont.resumeWith(recoverResult(state, uCont))
}
