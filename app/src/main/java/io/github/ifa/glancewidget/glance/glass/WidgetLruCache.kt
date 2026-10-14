package io.github.ifa.glancewidget.glance.glass

class WidgetLruCache<K, V>(
    val maxSizeBytes: Int,
    private val sizeOf: (K, V) -> Int
) {
    private var currentSize: Int = 0
    private val map = LinkedHashMap<K, V>(16, 0.75f, true)

    @Synchronized
    fun get(key: K): V? = map[key]

    @Synchronized
    fun put(key: K, value: V) {
        val existing = map.put(key, value)
        if (existing != null) {
            currentSize -= sizeOf(key, existing)
        }
        currentSize += sizeOf(key, value)
        trimToSize(maxSizeBytes)
    }

    @Synchronized
    fun remove(key: K): V? {
        val removed = map.remove(key)
        if (removed != null) {
            currentSize -= sizeOf(key, removed)
        }
        return removed
    }

    @Synchronized
    private fun trimToSize(max: Int) {
        val iterator = map.entries.iterator()
        while (iterator.hasNext() && currentSize > max) {
            val entry = iterator.next()
            currentSize -= sizeOf(entry.key, entry.value)
            iterator.remove()
        }
    }

    @Synchronized
    fun size(): Int = currentSize

    @Synchronized
    fun entryCount(): Int = map.size

    @Synchronized
    fun clear() {
        map.clear()
        currentSize = 0
    }
}
