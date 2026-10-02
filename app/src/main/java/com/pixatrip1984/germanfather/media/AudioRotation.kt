package com.pixatrip1984.germanfather.media

data class AudioSelection(
    val index: Int,
    val assetName: String,
)

interface AudioRotationIndexStore {
    fun currentIndex(): Int
    fun saveNextIndex(index: Int)
}

class AudioRotation(
    private val store: AudioRotationIndexStore,
) {
    fun current(rotation: List<String>): AudioSelection {
        require(rotation.isNotEmpty()) { "audio rotation must not be empty" }
        val index = Math.floorMod(store.currentIndex(), rotation.size)
        return AudioSelection(index = index, assetName = rotation[index])
    }

    fun advanceAfterPlaybackStarted(selection: AudioSelection, rotationSize: Int) {
        require(rotationSize > 0) { "rotationSize must be positive" }
        store.saveNextIndex((selection.index + 1) % rotationSize)
    }
}
