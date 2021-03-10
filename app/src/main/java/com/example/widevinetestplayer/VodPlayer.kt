package com.example.widevinetestplayer

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.widget.ImageView
import android.widget.SeekBar
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView


/**
 * Created by luyiling on 2019-07-16
 * Modified by luyiling on 2020-03-04
 * 因為這個類別會直接連到xml, 但可能會給不同的activity使用同一份layout
 * 所以在finish() 不特別定義是哪一個child activity
 * 而是用parent activity
 *
<title> </title>
 * TODO:
 * Description:
 *
 *<IMPORTANT>
 * @params
 * @params
 *</IMPORTANT>
 */
class VodPlayer : StandardGSYVideoPlayer {
    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet):  super(context, attrs)
//    lateinit var backBtn : ImageView
    lateinit var playbackStateBtn : ImageView

    override fun init(context: Context?) {
        super.init(context)

        playbackStateBtn = findViewById(R.id.small_play)
    }


    override fun getLayoutId(): Int = R.layout.player_vod

    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        val gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar)
        val sampleCoverVideo = gsyBaseVideoPlayer as VodPlayer
        return gsyBaseVideoPlayer
    }


    override fun showSmallVideo(size: Point?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        //下面这里替换成你自己的强制转化
        val sampleCoverVideo = super.showSmallVideo(size, actionBar, statusBar) as VodPlayer
        sampleCoverVideo.mStartButton.visibility = GONE
        sampleCoverVideo.mStartButton = null
        return sampleCoverVideo
    }


    /**
     * 定义开始按键显示
     */
   override fun updateStartImage() {
        val imageView = mStartButton as ImageView
        if (mCurrentState == GSYVideoView.CURRENT_STATE_PLAYING) {
            imageView.setImageResource(R.mipmap.ic_pause_big)
            playbackStateBtn.setImageResource(R.mipmap.ic_play_small)
        } else {
            imageView.setImageResource(R.mipmap.ic_play_big)
            playbackStateBtn.setImageResource(R.mipmap.ic_pause_small)
        }
    }




    /******************* 下方重载方法，在播放开始不显示底部进度和按键，不需要可屏蔽 ********************/

    var byStartedClick : Boolean = false

    override fun onClickUiToggle(e: MotionEvent?) {

        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            setViewShowState(mLockScreen, VISIBLE)
            return
        }

        byStartedClick = true
        super.onClickUiToggle(e)
    }

    override fun changeUiToNormal() {
        super.changeUiToNormal()

        byStartedClick = false
    }

    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        setViewShowState(mStartButton, INVISIBLE)
    }

    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        if (!byStartedClick) {
            setViewShowState(mStartButton, INVISIBLE)
        }
    }

   override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        if (!byStartedClick) {
            setViewShowState(mStartButton, INVISIBLE)
        }
    }

    override fun startAfterPrepared() {
        super.startAfterPrepared()
        setViewShowState(mStartButton, INVISIBLE)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        byStartedClick = true
        super.onStartTrackingTouch(seekBar)
    }

    override fun changeUiToError() {
        super.changeUiToError()
        setViewShowState(mTopContainer, VISIBLE)
    }

    /**
     * 由於此類別直接繼承GSYVideoControlView
     * 在此監聽state
     */
    override fun setStateAndUi(state: Int){
        super.setStateAndUi(state)

        Log.d("Video", "mCurrentState: $mCurrentState")

    }

    /**
     * @note crash point
     *
     */
    override fun onAutoCompletion() {
        //cause exception without stop()
//        gsyVideoManager.stop()
        super.onAutoCompletion()
    }



    //setStateAndUi -> onSurfaceDestroyed -> Error: native window.... -> releaseSurface ->-> onAutoCompletion

    override fun onSurfaceDestroyed(surface: Surface?): Boolean {
        Log.d("Video", "surface destroyed")
        return super.onSurfaceDestroyed(surface)
    }

    override fun releaseSurface(surface: Surface?) {
        Log.d("Video", "surface release")
        super.releaseSurface(surface)
    }

    override fun onError(what: Int, extra: Int) {
        Log.d("Video", "on error: $what, $extra")
        super.onError(what, extra)
    }
}

