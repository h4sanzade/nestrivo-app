package com.materialdesign.nestrivo_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import kotlin.math.ceil

/**
 * Three-dot loading indicator. Only one dot is lit at a time: dot 1 lights up emerald and goes
 * out, then dot 2, then dot 3, then it starts again. Driven from outside through [cycle]
 * so the owning fragment controls the animation lifecycle.
 */
class LoadingDotsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    /**
     * Position within one full 1-2-3 round, 0..1. A negative value means no dot is lit.
     */
    var cycle: Float = -1f
        set(value) {
            if (value != field) {
                field = value
                invalidate()
            }
        }

    private val dotRadius = resources.getDimension(R.dimen.loading_dot_radius)
    private val dotGap = resources.getDimension(R.dimen.loading_dot_gap)
    private val idleColor = ContextCompat.getColor(context, R.color.nestrivo_navy_25)
    private val litColor = ContextCompat.getColor(context, R.color.nestrivo_emerald)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    init {
        if (isInEditMode) cycle = 0.1f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = ceil(DOT_COUNT * 2f * dotRadius * MAX_SCALE + (DOT_COUNT - 1) * dotGap).toInt()
        val desiredHeight = ceil(2f * dotRadius * MAX_SCALE).toInt()
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec),
        )
    }

    override fun onDraw(canvas: Canvas) {
        val contentWidth = DOT_COUNT * 2f * dotRadius + (DOT_COUNT - 1) * dotGap
        val left = (width - contentWidth) / 2f
        val cy = height / 2f

        for (i in 0 until DOT_COUNT) {
            val glow = glowOf(i)
            paint.color = ColorUtils.blendARGB(idleColor, litColor, glow)
            val cx = left + dotRadius + i * (2f * dotRadius + dotGap)
            canvas.drawCircle(cx, cy, dotRadius * (1f + (MAX_SCALE - 1f) * glow), paint)
        }
    }

    /** 0 = idle, 1 = fully lit. A dot is only non-zero inside its own third of the cycle. */
    private fun glowOf(index: Int): Float {
        if (cycle < 0f) return 0f
        val slot = cycle * DOT_COUNT - index
        if (slot < 0f || slot >= 1f) return 0f
        return when {
            slot < FADE -> slot / FADE
            slot > 1f - FADE -> (1f - slot) / FADE
            else -> 1f
        }
    }

    private companion object {
        const val DOT_COUNT = 3
        const val MAX_SCALE = 1.3f
        const val FADE = 0.2f
    }
}