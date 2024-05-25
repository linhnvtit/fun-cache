package com.linhnvtit.fun_cache.cache.pool

import java.util.Queue
import java.util.concurrent.LinkedTransferQueue

class FIFOPool<T>(private var capacity: Int): FunCachePool<T> {
    private val hash: HashMap<String, T> = hashMapOf()
    private val queue: Queue<String> = LinkedTransferQueue()
    override fun get(key: String): T? {
        return hash[key]
    }

    override fun has(key: String): Boolean = key in hash

    override fun put(key: String, value: T) {
        if (key !in hash) queue.offer(key)
        hash[key] = value
        if (queue.size > capacity) hash.remove(queue.poll())
    }
}