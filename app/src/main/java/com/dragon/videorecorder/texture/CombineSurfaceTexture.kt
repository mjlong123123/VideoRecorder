package com.dragon.renderlib.texture

import android.graphics.SurfaceTexture
import android.view.Surface
import com.dragon.renderlib.extension.MirrorType
import com.dragon.renderlib.utils.OpenGlUtils

class CombineSurfaceTexture(
    width: Int,
    height: Int,
    rotate: Float,
    mirrorType: MirrorType,
    private val surfaceCallback: (Surface) -> Unit = {},
    private val notify: () -> Unit = {}
) :
    BasicTexture(width, height, rotate, mirrorType) {
    private lateinit var surfaceTexture: SurfaceTexture
    private lateinit var surface: Surface

    init {
        recreate()
    }

    override fun recreate() {
        textureId = OpenGlUtils.createTexture()
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setDefaultBufferSize(width, height)
        surfaceTexture.setOnFrameAvailableListener { notify.invoke() }
        surface = Surface(surfaceTexture)
        surfaceCallback.invoke(surface)
    }

    fun update() {
        if (surface.isValid) {
            surfaceTexture.updateTexImage()
        }
    }

    override fun release() {
        super.release()
        surface.release()
        surfaceTexture.release()
    }
}