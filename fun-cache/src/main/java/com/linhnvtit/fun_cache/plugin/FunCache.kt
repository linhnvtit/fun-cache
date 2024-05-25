package com.linhnvtit.fun_cache.plugin

import com.linhnvtit.fun_cache.cache.CacheStrategy
import com.linhnvtit.fun_cache.cache.pool.FIFOPool
import com.linhnvtit.fun_cache.cache.pool.FunCachePool
import com.linhnvtit.fun_cache.cache.pool.LFUPool
import com.linhnvtit.fun_cache.cache.pool.LRUPool
import com.linhnvtit.fun_cache.utils.CACHE_DEFAULT_CAPACITY
import com.linhnvtit.fun_cache.utils.FunCacheLog
import kotlin.reflect.KFunction

object FunCache {
    private val valueStore: HashMap<String, FunCachePool<Any>> = hashMapOf()

    @Synchronized
    @JvmStatic
    internal fun get(funcSignature: String, key: String): Any {
        FunCacheLog.d("[Get] $funcSignature [key] $key")

        if (funcSignature in valueStore && valueStore[funcSignature]?.has(key) == true) {
            FunCacheLog.d("[Get] Value: ${valueStore[funcSignature]?.get(key)}")
            return valueStore[funcSignature]!![key]!!
        }
        throw Exception("Function signature not found.")
    }

    @Synchronized
    @JvmStatic
    internal fun has(funcSignature: String, key: String): Boolean {
        FunCacheLog.d("[Has] $funcSignature $key")

        return funcSignature in valueStore && valueStore[funcSignature]?.has(key) == true
    }

    @Synchronized
    @JvmStatic
    internal fun put(funcSignature: String, key: String, value: Any) {
        FunCacheLog.d("[Put]: $value $funcSignature $key ${key in valueStore}")

        valueStore[funcSignature]?.put(key, value)
    }

    @Synchronized
    @JvmStatic
    internal fun tryInitFunctionCache(
        funcSignature: String,
        capacity: Int = CACHE_DEFAULT_CAPACITY,
        strategy: CacheStrategy = CacheStrategy.LRU
    ) {
        FunCacheLog.d("[tryInitFunctionCache]: $funcSignature $capacity $strategy")

        if (funcSignature !in valueStore) {
            valueStore[funcSignature] = when (strategy) {
                CacheStrategy.LFU -> LFUPool(capacity)
                CacheStrategy.LRU -> LRUPool(capacity)
                CacheStrategy.FIFO -> FIFOPool(capacity)
            }
        }
    }

    @Synchronized
    fun clearCache(func: KFunction<*>) {
        val name = func.funCacheSignature
        FunCacheLog.d("[Clear]: $name")

        if (name in valueStore) valueStore.remove(name)
    }
}