package com.dragon.renderlib.texture

import android.opengl.GLES20
import com.dragon.renderlib.extension.MirrorType
import com.dragon.renderlib.utils.OpenGlUtils

abstract class BasicTexture(
    val width: Int,
    val height: Int,
    val rotate: Float = 0f,
    val mirrorType: MirrorType = MirrorType.NONE
) {
    var textureId: Int = GLES20.GL_NONE
    var released: Boolean = false
    open fun release() {
        OpenGlUtils.releaseTexture(textureId)
        released = true
    }

    abstract fun recreate()
}