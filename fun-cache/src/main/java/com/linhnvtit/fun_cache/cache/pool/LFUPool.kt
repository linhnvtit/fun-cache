package com.linhnvtit.fun_cache.cache.pool

import com.linhnvtit.fun_cache.utils.FunCacheLog
import java.util.concurrent.ConcurrentHashMap


private class LFUNode<T>(
    val key: String, var value: T?, var prev: LFUNode<T>? = null, var next: LFUNode<T>? = null, var freq: Int = 1
)

class LFUPool<T>(private var capacity: Int) : FunCachePool<T> {
    private var head: LFUNode<T> = LFUNode("head", null)
    private var tail: LFUNode<T> = LFUNode("tail", null)
    private val hash: ConcurrentHashMap<String, LFUNode<T>> = ConcurrentHashMap()
    private val freqs: ConcurrentHashMap<Int, LFUNode<T>> = ConcurrentHashMap(hashMapOf(1 to head))

    init {
        head.next = tail
        tail.prev = head
    }

    override fun has(key: String): Boolean = hash.containsKey(key)

    override fun get(key: String): T? {
        try {
            if (hash.containsKey(key)) {
                val node = hash[key]!!
                node.freq += 1

                val lastNodeWithFreq = freqs.getOrDefault(node.freq, freqs[node.freq - 1])!!

                if (freqs[node.freq - 1] == node) {
                    if (node.prev?.freq == node.freq - 1) freqs[node.freq - 1] = node.prev!!
                    else freqs.remove(node.freq - 1)
                }

                if (lastNodeWithFreq.key != node.key) {
                    remove(node)
                    insert(lastNodeWithFreq, node)
                }

                freqs[node.freq] = node
                return node.value

            } else {
                return null
            }
        } catch (e: Exception) {
            FunCacheLog.e("${e.message}")
            return null
        }
    }

    override fun put(key: String, value: T) {
        try {
            if (hash.containsKey(key)) {
                val node = hash[key]!!
                node.freq += 1
                node.value = value

                val lastNodeWithFreq = freqs.getOrDefault(node.freq, freqs[node.freq - 1])!!

                if (freqs[node.freq - 1] == node) {
                    if (node.prev?.freq == node.freq - 1) freqs[node.freq - 1] = node.prev!!
                    else freqs.remove(node.freq - 1)
                }

                if (lastNodeWithFreq.key != node.key) {
                    remove(node)
                    insert(lastNodeWithFreq, node)
                }

                freqs[node.freq] = node
            } else {
                if (hash.size == capacity) {
                    if (freqs[head.next!!.freq] == head.next) {
                        if (head.next?.freq == 1) freqs[1] = head
                        else freqs.remove(head.next!!.freq)
                    }

                    hash.remove(head.next!!.key)
                    if (head.next?.next != null) {
                        head.next!!.next!!.prev = head
                    }
                    head.next = head.next!!.next
                }

                val newNode = LFUNode(key, value, null, null)

                val lastNodeWithFreq = freqs[1]!!
                insert(lastNodeWithFreq, newNode)
                freqs[1] = newNode
                hash[key] = newNode
            }
        } catch (e: Exception) {
            FunCacheLog.e("${e.message}")
        }
    }

    private fun insert(node1: LFUNode<T>, node2: LFUNode<T>) {
        node2.next = node1.next
        node1.next!!.prev = node2
        node2.prev = node1
        node1.next = node2
    }

    private fun remove(node: LFUNode<T>) {
        node.prev!!.next = node.next
        node.next!!.prev = node.prev
        node.next = null
        node.prev = null
    }
}