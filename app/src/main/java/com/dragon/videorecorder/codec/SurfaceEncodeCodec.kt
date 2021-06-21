package com.dragon.renderlib.codec

import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaFormat
import android.view.Surface

/**
 * @author dragon
 */
abstract class SurfaceEncodeCodec(mediaFormat: MediaFormat) : BaseCodec("Encode surface", mediaFormat) {
    private var inputSurface: Surface? = null
    override fun onCreateMediaCodec(mediaFormat: MediaFormat): MediaCodec {
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val codecName = mediaCodecList.findEncoderForFormat(mediaFormat)
        check(!codecName.isNullOrEmpty()) { throw RuntimeException("not find the matched codec!!!!!!!") }
        return MediaCodec.createByCodecName(codecName)
    }

    override fun onConfigMediaCodec(mediaCodec: MediaCodec) {
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = mediaCodec.createInputSurface()
        inputSurface?.let {
            onCreateInputSurface(it)
        }
    }

    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        //do nothing.because we set the input surface for encoder.
    }

    protected abstract fun onCreateInputSurface(surface: Surface)

    /**
     * we need to release surface ourselves.
     */
    protected abstract fun onDestroyInputSurface(surface: Surface)

    override fun releaseInternal() {
        inputSurface?.let {
            onDestroyInputSurface(it)
        }
        super.releaseInternal()
    }
}