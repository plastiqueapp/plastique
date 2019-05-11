package io.plastique.glide

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.graphics.drawable.DrawableWrapper
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

@Suppress("unused")
fun <T : Drawable> GlideRequest<T>.enableDebugIndicators(): GlideRequest<T> {
    return transition(DrawableTransitionOptions.with(DebugTransitionFactory))
}

private object DebugTransitionFactory : TransitionFactory<Drawable> {
    private const val INDICATOR_SIZE_DP = 16.0f

    override fun build(dataSource: DataSource, isFirstResource: Boolean): Transition<Drawable> {
        return DebugTransition(dataSource)
    }

    private class DebugTransition(private val dataSource: DataSource) : Transition<Drawable> {
        override fun transition(current: Drawable, adapter: Transition.ViewAdapter): Boolean {
            val indicatorColor = getIndicatorColor(dataSource)
            val indicatorSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INDICATOR_SIZE_DP, adapter.view.resources.displayMetrics)
            adapter.setDrawable(DebugDrawableWrapper(current, indicatorColor, indicatorSize))
            return true
        }

        @ColorInt
        @Suppress("MagicNumber")
        private fun getIndicatorColor(dataSource: DataSource): Int = when (dataSource) {
            DataSource.LOCAL -> Color.YELLOW
            DataSource.REMOTE -> Color.RED
            DataSource.DATA_DISK_CACHE,
            DataSource.RESOURCE_DISK_CACHE -> 0xff0066ff.toInt()
            DataSource.MEMORY_CACHE -> Color.GREEN
        }
    }

    @SuppressLint("RestrictedApi")
    private class DebugDrawableWrapper(
        drawable: Drawable,
        @ColorInt indicatorColor: Int,
        private val indicatorSize: Float
    ) : DrawableWrapper(drawable) {

        private val currentMatrix = Matrix()
        private val inverseMatrix = Matrix()
        private val paint = Paint().apply { color = indicatorColor }
        private val triangle = createTrianglePath(indicatorSize)

        override fun draw(canvas: Canvas) {
            super.draw(canvas)
            drawDebugIndicator(canvas)
        }

        private fun drawDebugIndicator(canvas: Canvas) {
            val count = canvas.save()

            // Set transformation matrix to identity
            @Suppress("DEPRECATION")
            canvas.getMatrix(currentMatrix)
            currentMatrix.invert(inverseMatrix)
            canvas.concat(inverseMatrix)

            canvas.translate(canvas.width - indicatorSize, 0.0f)
            canvas.drawPath(triangle, paint)

            canvas.restoreToCount(count)
        }

        private fun createTrianglePath(size: Float): Path = Path().apply {
            moveTo(0.0f, 0.0f)
            lineTo(size, 0.0f)
            lineTo(size, size)
            close()
        }
    }
}
