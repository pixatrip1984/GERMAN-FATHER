package com.pixatrip1984.germanfather.media

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioRotationStoreTest {
    @Test
    fun deterministicRoundRobinRepeatsAfterThirdEntry() {
        val store = InMemoryStore()
        val rotation = AudioRotation(store)
        val configured = listOf("1.mp3", "2.mp3", "3.mp3")

        val first = rotation.current(configured)
        assertEquals("1.mp3", first.assetName)
        rotation.advanceAfterPlaybackStarted(first, configured.size)

        val second = rotation.current(configured)
        assertEquals("2.mp3", second.assetName)
        rotation.advanceAfterPlaybackStarted(second, configured.size)

        val third = rotation.current(configured)
        assertEquals("3.mp3", third.assetName)
        rotation.advanceAfterPlaybackStarted(third, configured.size)

        assertEquals("1.mp3", rotation.current(configured).assetName)
    }

    @Test
    fun selectionDoesNotAdvanceUntilPlaybackStartIsConfirmed() {
        val store = InMemoryStore()
        val rotation = AudioRotation(store)
        val configured = listOf("1.mp3", "2.mp3", "3.mp3")

        assertEquals("1.mp3", rotation.current(configured).assetName)
        assertEquals("1.mp3", rotation.current(configured).assetName)
        assertEquals(0, store.currentIndex())
    }

    @Test
    fun persistedIndexSelectsSameNextEntryAfterReconstruction() {
        val store = InMemoryStore(initial = 2)
        val configured = listOf("1.mp3", "2.mp3", "3.mp3")

        assertEquals("3.mp3", AudioRotation(store).current(configured).assetName)
        val selection = AudioRotation(store).current(configured)
        AudioRotation(store).advanceAfterPlaybackStarted(selection, configured.size)

        assertEquals("1.mp3", AudioRotation(store).current(configured).assetName)
    }

    private class InMemoryStore(
        initial: Int = 0,
    ) : AudioRotationIndexStore {
        private var value = initial

        override fun currentIndex(): Int = value

        override fun saveNextIndex(index: Int) {
            value = index
        }
    }
}
