package com.tk.choosr.util

class ShuffleSessionManager {
    private val queues = mutableMapOf<String, ArrayDeque<Int>>()
    private val sizes = mutableMapOf<String, Int>()

    fun nextIndex(
        listId: String,
        size: Int,
        avoidPreviousResults: Boolean = false,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Int {
        require(size > 0) { "size must be > 0" }
        
        if (avoidPreviousResults) {
            // Efficient queue-based approach: shuffle all items once, then return them in order
            if ((sizes[listId] ?: -1) != size || queues[listId].isNullOrEmpty()) {
                queues[listId] = ArrayDeque((0 until size).shuffled(random))
                sizes[listId] = size
            }
            return queues[listId]!!.removeFirst()
        } else {
            // Simple random selection: pick any item randomly (allows immediate repeats)
            return (0 until size).random(random)
        }
    }

    fun clear(listId: String) {
        queues.remove(listId)
        sizes.remove(listId)
    }
}


