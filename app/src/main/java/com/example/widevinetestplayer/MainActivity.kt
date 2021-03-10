package com.example.widevinetestplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.TransferListener
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import kotlinx.android.synthetic.main.activity_player.*
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener
import tv.danmaku.ijk.media.exo2.ExoSourceManager
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var orientationUtils : OrientationUtils

    lateinit var drmSessionManager: DefaultDrmSessionManager

    //Test OK: live http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8
    //Test OK: vod https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8
    //Widevine provide sample
    //Test OK: h264 "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"
    //Test OK: widevine h264 https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd
    //Test OK: widevine hevc https://storage.googleapis.com/wvmedia/cenc/hevc/tears/tears.mpd
    private val video_url = "https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears.mpd"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)


        //Helper:
        orientationUtils = OrientationUtils(this, detail_player)
        val gsyVideoOption = GSYVideoOptionBuilder()
        gsyVideoOption
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(false)
            .setUrl(video_url)
            .setCacheWithPlay(true)//vod: true //live: false
            .setVideoTitle("Test Sample")
            .build(detail_player)

        //default: fullscreen
        val player = detail_player.startWindowFullscreen(this, true, true)

        //must use surfaceView to play when drm
        //Error Msg:
        //from media / libstagefright / SurfaceUtils.cpp
        //E SurfaceUtils: native window cannot handle protected buffers: the consumer should either be a hardware composer or support hardware protection
        GSYVideoType.setRenderType(GSYVideoType.SUFRACE)


        /**@note use exoplayer core*/
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        player.startButton.performClick()

        //interrupt data source to fill in drmed data source
        ExoSourceManager.setExoMediaSourceInterceptListener(object :
            ExoMediaSourceInterceptListener {
            /**
             * @param dataSource  链接
             * @param preview     是否带上header，默认有header自动设置为true
             * @param cacheEnable 是否需要缓存
             * @param isLooping   是否循环
             * @param cacheDir    自定义缓存目录
             * @return 返回不为空时，使用返回的自定义mediaSource
             */
            override fun getMediaSource(
                dataSource: String?,
                preview: Boolean,
                cacheEnable: Boolean,
                isLooping: Boolean,
                cacheDir: File?
            ): MediaSource? {

                drmSessionManager =
                    DefaultDrmSessionManager.Builder()
                        //keyRequest not needed to write
//                        .setKeyRequestParameters(mapOf(
//                            "MDAwMDAwMDAwMDAwMDAwMQ==" to "eKHcBkYRlwfpA1FNigBzXw==",
//                            "MDAwMDAwMDAwMDAwMDAwMw==" to "QkZshCrBxUObHgwJ+7Th0g==",
//                            "MDAwMDAwMDAwMDAwMDAwMg==" to "Hzeeo4xw5Af3ayPsZAHK7w==",
//                            "MDAwMDAwMDAwMDAwMDAwMA==" to "Pwoz80CYueIrwHjgobXoVA==",
//                            "MDAwMDAwMDAwMDAwMDAwNA==" to "IvCfhLVopdAH5LHRFpQ1gQ==",
//                            "MDAwMDAwMDAwMDAwMDAwNQ==" to "msMDbgSsnSvpRu1iQFFJvA==",
//                            "MDAwMDAwMDAwMDAwMDAwNg==" to "MUWYWCQzTsTLSsS9w+K+7w==",
//                            "MDAwMDAwMDAwMDAwMDAwNw==" to "ebhzT7mNJ1qQempaFQEouw=="
//                        ))
                        .build(HttpMediaDrmCallback(
                            "https://proxy.staging.widevine.com/proxy",
                            DefaultHttpDataSourceFactory("user-agent")
                        ))

                val mediaItem : MediaItem =
                    MediaItem.fromUri(video_url)

                /**
                 * MediaSource
                 *  HLS -> HlsMediaSource
                    DASH -> DashMediaSource
                    SS -> SsMediaSource
                    MP4 and others -> ExtractorMediaSource
                 */
                return DefaultMediaSourceFactory(baseContext) /*沒有處理rtmp*/
                    .setDrmSessionManager(drmSessionManager)
                    .createMediaSource(mediaItem)

            }

            /**
             * for certificate ignore or signed
             * default = null
             * */
            override fun getHttpDataSourceFactory(
                userAgent: String,
                listener: TransferListener?,
                connectTimeoutMillis: Int,
                readTimeoutMillis: Int,
                allowCrossProtocolRedirects: Boolean
            ): HttpDataSource.BaseFactory? {
                return null
            }
        })


    }



    override fun onPause() {
        detail_player.currentPlayer.onVideoPause()
        super.onPause()
    }

    override fun onResume() {
        detail_player.currentPlayer.onVideoResume(false)
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

        detail_player.currentPlayer.release()
        orientationUtils.releaseListener()
    }
}