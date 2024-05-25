package com.linhnvtit.fun_cache.cache.pool

import com.linhnvtit.fun_cache.utils.FunCacheLog

private class LRUNode<T>(
    var key: String,
    var value: T?,
    var prev: LRUNode<T>? = null,
    var next: LRUNode<T>? = null
)

class LRUPool<T>(private val capacity: Int) : FunCachePool<T> {
    private val hash = mutableMapOf<String, LRUNode<T>>()
    private val head: LRUNode<T> = LRUNode("head", null)
    private val tail: LRUNode<T> = LRUNode("tail", null)

    init {
        head.next = tail
        tail.prev = head
    }

    override fun get(key: String): T? {
        return try {
            if (!hash.containsKey(key)) {
                null
            } else {
                val node = hash[key]!!
                remove(node)
                add(node)
                node.value
            }
        } catch (e: Exception) {
            FunCacheLog.e("${e.message}")
            null
        }
    }

    override fun has(key: String): Boolean = key in hash

    override fun put(key: String, value: T) {
        try {
            val newNode = LRUNode(key, value)

            if (hash.containsKey(key)) {
                remove(hash[key]!!)
            }

            add(newNode)
            hash[key] = newNode

            if (hash.size > capacity) {
                hash.remove(head.next!!.key)
                head.next = head.next!!.next
                head.next?.prev = head
            }
        } catch (e: Exception) {
            FunCacheLog.e("${e.message}")
        }
    }

    private fun remove(node: LRUNode<T>) {
        node.prev!!.next = node.next
        node.next!!.prev = node.prev
    }

    private fun add(node: LRUNode<T>) {
        val tailPrev = tail.prev
        tail.prev = node
        tailPrev!!.next = node
        node.next = tail
        node.prev = tailPrev
    }
}