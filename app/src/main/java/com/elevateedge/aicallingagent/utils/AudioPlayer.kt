package com.elevateedge.aicallingagent.utils

import android.media.MediaPlayer
import android.util.Log

object AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    fun playAudio(path: String) {
        stopAudio()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Playback failed: ${e.message}")
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}
