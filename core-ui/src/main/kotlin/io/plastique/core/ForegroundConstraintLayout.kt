package io.plastique.core

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.commit451.foregroundviews.ForegroundDelegate

class ForegroundConstraintLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var foregroundDelegate: ForegroundDelegate? = null

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            foregroundDelegate = ForegroundDelegate(this).apply {
                init(context, attrs, defStyleAttr, 0)
            }
        }
    }

    override fun getForegroundGravity(): Int {
        return foregroundDelegate?.foregroundGravity ?: super.getForegroundGravity()
    }

    override fun setForegroundGravity(foregroundGravity: Int) {
        if (foregroundDelegate != null) {
            foregroundDelegate!!.foregroundGravity = foregroundGravity
        } else {
            super.setForegroundGravity(foregroundGravity)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === foregroundDelegate?.foreground
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        foregroundDelegate?.jumpDrawablesToCurrentState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        foregroundDelegate?.drawableStateChanged()
    }

    override fun setForeground(foreground: Drawable?) {
        if (foregroundDelegate != null) {
            foregroundDelegate!!.foreground = foreground
        } else {
            super.setForeground(foreground)
        }
    }

    override fun getForeground(): Drawable? {
        return if (foregroundDelegate != null) {
            foregroundDelegate!!.foreground
        } else {
            super.getForeground()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        foregroundDelegate?.onLayout(changed, left, top, right, bottom)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        foregroundDelegate?.onSizeChanged(w, h, oldw, oldh)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        foregroundDelegate?.draw(canvas)
    }

    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        foregroundDelegate?.drawableHotspotChanged(x, y)
    }
}
