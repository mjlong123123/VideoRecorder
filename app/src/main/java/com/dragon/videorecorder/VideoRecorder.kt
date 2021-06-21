package com.dragon.videorecorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.dragon.renderlib.codec.BaseCodec
import com.dragon.renderlib.codec.SurfaceEncodeCodec
import com.dragon.renderlib.net.NaluData
import com.dragon.rtplib.RtpWrapper

/**
 * @author dragon
 */
class VideoRecorder(
    val width: Int,
    val height: Int,
    val createSurface: (Surface) -> Unit = {},
    val destroySurface: (Surface) -> Unit = {}
) {
    private val videoPayloadType = 96;
    private val videoRtpPort = 40018;
    private val videoBitRate = 2432 * 1024;//720p 960*720
    private val videoFrameRate = 30;
    private val videoSampleRate = 90000
    private val videoTimeIncrease = videoSampleRate / videoFrameRate
    private val spsByteArray = ByteArray(50)
    private val ppsByteArray = ByteArray(50)
    private var spsByteArraySize = 0
    private var ppsByteArraySize = 0
    private var countIFrame = 0


    private var videoRtpWrapper: RtpWrapper? = null;

    private var videoCodec: BaseCodec? = null
    var isStarted = false

    private val naluData = NaluData();


    fun startVideoEncoder(ip: String) {
        if(isStarted){
            return
        }
        isStarted = true
        val mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, videoBitRate)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, videoFrameRate)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        videoCodec?.release()
        videoCodec = object : SurfaceEncodeCodec(mediaFormat) {
            override fun onCreateInputSurface(surface: Surface) {
                createSurface.invoke(surface)
            }

            override fun onDestroyInputSurface(surface: Surface) {
                destroySurface.invoke(surface)
            }

            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                val buffer = codec.getOutputBuffer(index) ?: return
                when {
                    info.flags.and(MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == MediaCodec.BUFFER_FLAG_CODEC_CONFIG -> {
                        log { "video config frame +++++++++++++" }
                    }
                    info.flags.and(MediaCodec.BUFFER_FLAG_KEY_FRAME) == MediaCodec.BUFFER_FLAG_KEY_FRAME -> {
                        countIFrame++
                        if (countIFrame.rem(3) == 0) {
                            log { "video ssspppssspppsss frame +++++++++++++" }
                            videoRtpWrapper?.sendData(ppsByteArray, ppsByteArraySize, videoPayloadType, true, 0)
                            videoRtpWrapper?.sendData(spsByteArray, spsByteArraySize, videoPayloadType, true, 0)
                        }
                        naluData.split2FU(buffer, info.offset, info.size) { b, o, s, m, increase ->
                            videoRtpWrapper?.sendData(b, s, videoPayloadType, m, if (increase) videoTimeIncrease else 0)
                        }
                    }
                    info.flags.and(MediaCodec.BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM -> {
                        log { "video end frame -------------" }
                    }
                    info.flags.and(MediaCodec.BUFFER_FLAG_PARTIAL_FRAME) == MediaCodec.BUFFER_FLAG_PARTIAL_FRAME -> {
                        log { "video partial frame -------------" }
                    }
                    else -> {
                        naluData.split2FU(buffer, info.offset, info.size) { b, o, s, m, increase ->
                            videoRtpWrapper?.sendData(b, s, videoPayloadType, m, if (increase) videoTimeIncrease else 0)
                        }
                    }
                }
                codec.releaseOutputBuffer(index, false)
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                format.getByteBuffer("csd-0")?.apply {
                    position(4)
                    spsByteArraySize = limit() - 4
                    get(spsByteArray, 0, spsByteArraySize)
                }
                format.getByteBuffer("csd-1")?.apply {
                    position(4)
                    ppsByteArraySize = limit() - 4
                    get(ppsByteArray, 0, ppsByteArraySize)
                }
                videoRtpWrapper = RtpWrapper()
                videoRtpWrapper?.open(videoRtpPort, videoPayloadType, videoSampleRate)
                videoRtpWrapper?.addDestinationIp(ip)

                videoRtpWrapper?.sendData(ppsByteArray, ppsByteArraySize, videoPayloadType, true, 0)
                videoRtpWrapper?.sendData(spsByteArray, spsByteArraySize, videoPayloadType, true, 0)
            }
        }
    }

    fun stopVideoEncoder() {
        if(!isStarted){
            return
        }
        isStarted = false
        videoCodec?.release {
            videoRtpWrapper?.close()
        }
    }

    private fun log(block: () -> String) {
        Log.d("recorder", block.invoke())
    }
}