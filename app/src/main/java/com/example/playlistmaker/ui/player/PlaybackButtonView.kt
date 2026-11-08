package com.example.playlistmaker.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toBitmap
import com.example.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var playBitmap: Bitmap? = null
    private var pauseBitmap: Bitmap? = null
    private val imageRect = RectF()

    private var isPlaying = false

    init {
        context.withStyledAttributes(attrs, R.styleable.PlaybackButtonView, defStyleAttr, defStyleRes) {
            playBitmap = getDrawable(R.styleable.PlaybackButtonView_playImage)?.toBitmap()
            pauseBitmap = getDrawable(R.styleable.PlaybackButtonView_pauseImage)?.toBitmap()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = if (isPlaying) pauseBitmap else playBitmap
        bitmap?.let { canvas.drawBitmap(it, null, imageRect, null) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                if (imageRect.contains(event.x, event.y)) {
                    setPlaying(!isPlaying)
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setPlaying(playing: Boolean) {
        if (isPlaying == playing) return
        isPlaying = playing
        invalidate()
    }
}
