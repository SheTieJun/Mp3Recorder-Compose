package me.shetj.compose.demo.ui.home.home_func.record

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import me.shetj.compose.demo.ui.home.home_func.record.BGMState.BGMError
import me.shetj.compose.demo.ui.home.home_func.record.BGMState.BGMIng
import me.shetj.compose.demo.ui.home.home_func.record.BGMState.BGMPause
import me.shetj.compose.demo.ui.home.home_func.record.BGMState.BGMStop
import me.shetj.compose.demo.ui.home.home_func.record.RecorderState.RecordError
import me.shetj.compose.demo.ui.home.home_func.record.RecorderState.RecordIng
import me.shetj.compose.demo.ui.home.home_func.record.RecorderState.RecordPause
import me.shetj.compose.demo.ui.home.home_func.record.RecorderState.RecordPermission
import me.shetj.compose.demo.ui.home.home_func.record.RecorderState.RecordStop
import me.shetj.player.PlayerListener
import me.shetj.recorder.core.AudioUtils
import me.shetj.recorder.core.BaseRecorder
import me.shetj.recorder.core.PermissionListener
import me.shetj.recorder.core.RecordListener
import me.shetj.recorder.core.RecordState.PAUSED
import me.shetj.recorder.core.RecordState.RECORDING
import me.shetj.recorder.core.RecordState.STOPPED
import me.shetj.recorder.core.recorder
import me.shetj.recorder.mixRecorder.buildMix


sealed class RecorderState {
    object RecordIng : RecorderState()
    object RecordPause : RecorderState()
    object RecordStop : RecorderState()
    class RecordError(val e: Exception) : RecorderState()
    object RecordPermission : RecorderState()
}


sealed class BGMState {
    object BGMIng : BGMState()
    object BGMPause : BGMState()
    object BGMStop : BGMState()
    class BGMError(val e: Exception?) : BGMState()
}

@Composable
fun rememberRecorderState(
    onSucceed: (isAutoComplete: Boolean, file: String) -> Unit
): Mp3RecorderState {
    val context = LocalContext.current
    val recordSate: MutableState<RecorderState> = remember { mutableStateOf(RecordStop) }
    val recordTime: MutableState<Long> = remember { mutableStateOf(0L) }
    val volume: MutableState<Int> = remember { mutableStateOf(0) }
    val maxTime: MutableState<Int> = remember { mutableStateOf(60 * 60 * 1000) }
    val isPlayIngBgm: MutableState<BGMState> = remember { mutableStateOf(BGMStop) }
    val bgmDuration: MutableState<Int> = remember { mutableStateOf(0) }
    val bgmTime: MutableState<Int> = remember { mutableStateOf(0) }
    return remember(recordSate, recordTime, volume, maxTime, isPlayIngBgm, bgmDuration, bgmTime) {
        Mp3RecorderState(
            context,
            recordSate,
            recordTime,
            volume,
            maxTime,
            isPlayIngBgm,
            bgmDuration,
            bgmTime,
            onSucceed
        )
    }
}

@Stable
class Mp3RecorderState(
    context: Context,
    val recorderState: MutableState<RecorderState>, //录音状态
    val recordTime: MutableState<Long>, //录音时间的长度
    val currentVolume: MutableState<Int>, //当前声音大小
    val maxTime: MutableState<Int>,//最大录制时间
    val isPlayIngBgm: MutableState<BGMState>, //背景音乐的状态
    val bgmDuration: MutableState<Int>, //背景音乐总时长
    val bgmTime: MutableState<Int>,//背景音乐播放的时长
    private val onSucceed: (isAutoComplete: Boolean, file: String) -> Unit
) : RecordListener, PermissionListener {

    private val playerListener = object : PlayerListener {
        override fun onCompletion() {
            isPlayIngBgm.value = BGMStop
        }

        override fun onError(throwable: Exception?) {
            isPlayIngBgm.value = BGMError(throwable)
        }

        override fun onPause() {
            isPlayIngBgm.value = BGMPause
        }

        override fun onProgress(current: Int, duration: Int) {
            bgmTime.value = current
        }

        override fun onResume() {
            isPlayIngBgm.value = BGMIng
        }

        override fun onStart(duration: Int) {
            bgmDuration.value = duration
            isPlayIngBgm.value = BGMIng
        }

        override fun onStop() {
            isPlayIngBgm.value = BGMStop
        }
    }

    val isActive: Boolean
        get() = mRecorder.isActive


    private val mRecorder: BaseRecorder = recorder {
        mMaxTime = 60 * 60 * 1000
        isDebug = true
        wax = 1f
        samplingRate = 44100
        audioSource = MediaRecorder.AudioSource.MIC
        audioChannel = 2
        recordListener = this@Mp3RecorderState
        permissionListener = this@Mp3RecorderState
    }.buildMix(context).also {
        it.setBackgroundMusicListener(playerListener)
    }

    fun reset() {
        recorderState.value = RecordStop
        recordTime.value = 0
        currentVolume.value = 0
        isPlayIngBgm.value = BGMStop
        bgmDuration.value = 0
        bgmTime.value = 0
    }

    fun startOrPause(file: String) {
        if (TextUtils.isEmpty(file)) {
            return
        }
        when (mRecorder.state) {
            STOPPED -> {
                mRecorder.setOutputFile(file, true)
                mRecorder.start()
            }
            PAUSED -> {
                mRecorder.resume()
            }
            RECORDING -> {
                mRecorder.pause()
            }
        }
    }

    fun pause(){
        mRecorder.pause()
    }

    fun resume(){
        mRecorder.resume()
    }

    fun complete() {
        mRecorder.complete()
    }

    fun destroy(){
        mRecorder.destroy()
    }

    fun setBackGroundUrl(context: Context?, url: Uri) {
        if (context != null) {
            mRecorder.setAudioChannel(AudioUtils.getAudioChannel(context, url))
            mRecorder.setBackgroundMusic(context, url, null)
        }
    }

    fun setBackGroundUrl(url: String?) {
        if (url != null) {
            mRecorder.setBackgroundMusic(url)
        }
    }

    fun updateMaxTime(maxTime: Long) {
        mRecorder.setMaxTime(maxTime)
    }

    fun startOrPauseBGM() {
        if (mRecorder.isPlayMusic()) {
            if (mRecorder.isPauseMusic()) {
                mRecorder.resumeMusic()
            } else {
                mRecorder.pauseMusic()
            }
        } else {
            mRecorder.startPlayMusic()
        }
    }

    override fun needPermission() {
        recorderState.value = RecordPermission
    }

    override fun onError(e: Exception) {
        recorderState.value = RecordError(e)
    }

    override fun onMaxChange(time: Long) {
        maxTime.value = time.toInt()
    }

    override fun onPause() {
        recorderState.value = RecordPause
    }

    override fun onRecording(time: Long, volume: Int) {
        Log.i("onRecording",time.toString())
        recordTime.value = time
        currentVolume.value = volume
    }

    override fun onRemind(duration: Long) {

    }

    override fun onReset() {
        recorderState.value = RecordStop
    }

    override fun onResume() {
        recorderState.value = RecordIng
    }

    override fun onStart() {
        recorderState.value = RecordIng
    }

    override fun onSuccess(isAutoComplete: Boolean, file: String, time: Long) {
        recorderState.value = RecordStop
        onSucceed.invoke(isAutoComplete, file)
        reset()
    }

}