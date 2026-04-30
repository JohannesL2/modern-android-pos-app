package com.example.test_design.scancontrol

import android.content.Context
import android.media.MediaPlayer
import com.example.test_design.R

object ScanSoundPlayer {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun playScanSuccess() {
        play(R.raw.beep_succeed)
    }

    fun playScanDenied() {
        play(R.raw.beep_denied)
    }


    fun playSuccess() {
        play(R.raw.access_succeed)
    }

    fun playDenied() {
        play(R.raw.access_denied)
    }

    private fun play(soundResId: Int) {
        val context = appContext ?: return
        runCatching {
            MediaPlayer.create(context, soundResId)?.apply {
                setOnCompletionListener { player ->
                    player.release()
                }
                setOnErrorListener { player, _, _ ->
                    player.release()
                    true
                }
                start()
            }
        }
    }
}
