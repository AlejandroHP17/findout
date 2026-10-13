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

    @Test
    fun fiveObjectsMatchNivel1Count() {
        val manager = GameManager()
        manager.startLevel(5)
        listOf("sock", "paper_plane", "rubik", "keys", "dino").forEach {
            assertTrue(manager.markFound(it))
        }
        assertEquals(5, manager.foundCount)
        assertTrue(manager.isLevelComplete())
        assertEquals(0, manager.remaining())
    }

    @Test
    fun fiveObjectsMatchNivel2Kitchen() {
        val manager = GameManager()
        manager.startLevel(5)
        listOf("spatula", "egg", "mug", "recipe", "chili").forEach {
            assertTrue(manager.markFound(it))
        }
        assertEquals(5, manager.foundCount)
        assertTrue(manager.isLevelComplete())
    }
}
