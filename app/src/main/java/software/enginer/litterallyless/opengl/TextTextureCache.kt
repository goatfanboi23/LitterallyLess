package software.enginer.litterallyless.opengl


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.opengl.GLES30
import software.enginer.litterallyless.opengl.renderers.SampleRender
import java.nio.ByteBuffer

/**
 * Generates and caches GL textures for label names.
 */
class TextTextureCache {
    companion object {
        @JvmField
        var greenTextPaint: Paint = Paint().apply {
            textSize = 26f
            setARGB(0xff, 0x35, 0xea, 0x35)
            style = Paint.Style.FILL
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            strokeWidth = 2f
        }
        @JvmField
        var redTextPaint = Paint().apply {
            textSize = 26f
            setARGB(0xff, 0xea, 0x43, 0x35)
            style = Paint.Style.FILL
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            strokeWidth = 2f
        }
        @JvmField
        var purpleTextPaint = Paint().apply {
            textSize = 26f
            setARGB(0xff, 0x4B, 0x00, 0x82)
            style = Paint.Style.FILL
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            strokeWidth = 2f
        }
        private const val TAG = "TextTextureCache"
    }

    private val cacheMap = mutableMapOf<PaintedString, Texture>()

    /**
     * Get a texture for a given string. If that string hasn't been used yet, create a texture for it
     * and cache the result.
     */
    fun get(render: SampleRender, string: String, textPaint: Paint): Texture {
        return cacheMap.computeIfAbsent(
            PaintedString(
                string,
                textPaint
            )
        ) {
            generateTexture(render, string, textPaint)
        }
    }

    private fun generateTexture(render: SampleRender, string: String, textPaint: Paint): Texture {
        val texture = Texture(render, Texture.Target.TEXTURE_2D, Texture.WrapMode.CLAMP_TO_EDGE)

        val bitmap = generateBitmapFromString(string, textPaint)
        val buffer = ByteBuffer.allocateDirect(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        buffer.rewind()

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.textureId)
        GLError.maybeThrowGLException("Failed to bind texture", "glBindTexture")
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA8,
            bitmap.width,
            bitmap.height,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            buffer
        )
        GLError.maybeThrowGLException("Failed to populate texture data", "glTexImage2D")
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLError.maybeThrowGLException("Failed to generate mipmaps", "glGenerateMipmap")

        return texture
    }

    val strokePaint = Paint().apply {
        setARGB(0xff, 0x00, 0x00, 0x00)
        textSize = 26f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private fun generateBitmapFromString(string: String, textPaint: Paint): Bitmap {
        val w = 256
        val h = 256
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            eraseColor(0)

            Canvas(this).apply {
                drawText(string, w / 2f, h / 2f, strokePaint)

                drawText(string, w / 2f, h / 2f, textPaint)
            }
        }
    }
}