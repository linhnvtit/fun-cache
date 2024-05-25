package com.linhnvtit.fun_cache.cache.pool


interface FunCachePool<T> {
    operator fun get(key: String): T?
    fun put(key: String, value: T)
    fun has(key: String): Boolean
}