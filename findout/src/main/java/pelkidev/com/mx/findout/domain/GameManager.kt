package pelkidev.com.mx.findout.domain

/** Gestiona el progreso del nivel: total, encontrados y victoria. */
class GameManager {
    var totalObjects: Int = 0
        private set
    var foundCount: Int = 0
        private set

    private val foundIds = linkedSetOf<String>()

    fun startLevel(total: Int) {
        totalObjects = total.coerceAtLeast(0)
        foundCount = 0
        foundIds.clear()
    }

    fun markFound(objectId: String): Boolean {
        if (objectId.isEmpty() || foundIds.contains(objectId)) return false
        if (totalObjects <= 0) return false
        foundIds.add(objectId)
        foundCount += 1
        return true
    }

    fun isFound(objectId: String): Boolean = foundIds.contains(objectId)

    fun isLevelComplete(): Boolean = totalObjects > 0 && foundCount >= totalObjects

    fun remaining(): Int = (totalObjects - foundCount).coerceAtLeast(0)
}
