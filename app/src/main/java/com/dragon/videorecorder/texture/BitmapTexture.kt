package com.dragon.renderlib.texture

import android.graphics.Bitmap
import com.dragon.renderlib.extension.MirrorType
import com.dragon.renderlib.utils.OpenGlUtils

class BitmapTexture(
    private val bitmap: Bitmap,
    rotate: Float,
    mirrorType: MirrorType
) : BasicTexture(bitmap.width, bitmap.height, rotate, mirrorType) {
    init {
        recreate()
    }

    override fun release() {
        super.release()
        OpenGlUtils.releaseTexture(textureId)
        bitmap.recycle()
    }

    override fun recreate() {
        textureId = OpenGlUtils.createBitmapTexture(bitmap)
    }

    fun hasAlpha() = bitmap.hasAlpha()
}