package com.airbnb.viaduct.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

private const val DEFAULT_CAPACITY = 256

fun <A, R> ((A) -> R).memoize(
    initialCapacity: Int = DEFAULT_CAPACITY
): (A) -> R = memoize(ConcurrentHashMap(initialCapacity))

fun <A, R> ((A) -> R).memoize(
    cache: ConcurrentMap<A, R>
): (A) -> R = { a: A ->
    cache.getOrPut(a) { this(a) }
}

fun <A, B, R> ((A, B) -> R).memoize(
    initialCapacity: Int = DEFAULT_CAPACITY
): (A, B) -> R = memoize(ConcurrentHashMap(initialCapacity))

fun <A, B, R> ((A, B) -> R).memoize(
    cache: ConcurrentMap<Pair<A, B>, R>
): (A, B) -> R = { a: A, b: B ->
    cache.getOrPut(a to b) { this(a, b) }
}

fun <A, B, C, R> ((A, B, C) -> R).memoize(
    initialCapacity: Int = DEFAULT_CAPACITY
): (A, B, C) -> R = memoize(ConcurrentHashMap(initialCapacity))

fun <A, B, C, R> ((A, B, C) -> R).memoize(
    cache: ConcurrentMap<Triple<A, B, C>, R>
): (A, B, C) -> R = { a: A, b: B, c: C ->
    cache.getOrPut(Triple(a, b, c)) { this(a, b, c) }
}

fun <A, B, C, D, R> ((A, B, C, D) -> R).memoize(
    initialCapacity: Int = DEFAULT_CAPACITY
): (A, B, C, D) -> R = memoize(ConcurrentHashMap(initialCapacity))

fun <A, B, C, D, R> ((A, B, C, D) -> R).memoize(
    cache: ConcurrentMap<Quadruple<A, B, C, D>, R>
): (A, B, C, D) -> R = { a: A, b: B, c: C, d: D ->
    cache.getOrPut(Quadruple(a, b, c, d)) { this(a, b, c, d) }
}

fun <A, B, C, D, E, R> ((A, B, C, D, E) -> R).memoize(
    initialCapacity: Int = DEFAULT_CAPACITY
): (A, B, C, D, E) -> R = memoize(ConcurrentHashMap(initialCapacity))

fun <A, B, C, D, E, R> ((A, B, C, D, E) -> R).memoize(
    cache: ConcurrentMap<Quintuple<A, B, C, D, E>, R>
): (A, B, C, D, E) -> R = { a: A, b: B, c: C, d: D, e: E ->
    cache.getOrPut(Quintuple(a, b, c, d, e)) { this(a, b, c, d, e) }
}
