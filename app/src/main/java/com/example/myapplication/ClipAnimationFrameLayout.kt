package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout


class ClipAnimationFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : FrameLayout(context, attrs, style) {
    var path: Path = Path()
    lateinit var bounds: RectF

    var cornerRadius: Float = 100.0f
        set(value) {
            field = value
            invalidate()
        }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        path.rewind()
        path.addRoundRect(bounds, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds = RectF(0f, 0f, w.toFloat(), h.toFloat() + cornerRadius)
    }
}