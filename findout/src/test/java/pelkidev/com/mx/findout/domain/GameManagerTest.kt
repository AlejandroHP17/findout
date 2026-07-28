package pelkidev.com.mx.findout.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameManagerTest {
    @Test
    fun marksFoundAndCompletesLevel() {
        val manager = GameManager()
        manager.startLevel(2)
        assertTrue(manager.markFound("a"))
        assertFalse(manager.markFound("a"))
        assertEquals(1, manager.foundCount)
        assertFalse(manager.isLevelComplete())
        assertTrue(manager.markFound("b"))
        assertTrue(manager.isLevelComplete())
    }
}
