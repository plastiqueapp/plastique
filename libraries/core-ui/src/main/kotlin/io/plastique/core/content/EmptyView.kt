package io.plastique.core.content

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.ui.R
import io.plastique.core.ui.databinding.ViewEmptyBinding

class EmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = R.attr.emptyViewStyle,
    defStyleRes: Int = R.style.Widget_App_EmptyView
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewEmptyBinding

    var state: EmptyState? = null
        set(value) {
            field = value
            if (value != null) {
                renderState(value)
            }
        }

    var onButtonClick: OnButtonClickListener = {}

    init {
        orientation = VERTICAL

        val a = context.obtainStyledAttributes(attrs, R.styleable.EmptyView, defStyleAttr, defStyleRes)
        val text = a.getString(R.styleable.EmptyView_android_text)
        val buttonText = a.getString(R.styleable.EmptyView_buttonText)
        val textAppearance = a.getResourceId(R.styleable.EmptyView_android_textAppearance, 0)
        val buttonTextAppearance = a.getResourceId(R.styleable.EmptyView_buttonTextAppearance, 0)
        val showButton = a.getBoolean(R.styleable.EmptyView_showButton, true)
        a.recycle()

        binding = ViewEmptyBinding.inflate(layoutInflater, this)

        TextViewCompat.setTextAppearance(binding.message, textAppearance)
        binding.message.text = text

        TextViewCompat.setTextAppearance(binding.button1, buttonTextAppearance)
        binding.button1.text = buttonText
        binding.button1.isVisible = showButton
        binding.button1.setOnClickListener { onButtonClick() }
    }

    private fun renderState(state: EmptyState) {
        when (state) {
            is EmptyState.Message -> {
                binding.message.text = getMessageWithArgs(state.messageResId, state.messageArgs)
                binding.button1.isVisible = false
            }

            is EmptyState.MessageWithButton -> {
                binding.message.text = getMessageWithArgs(state.messageResId, state.messageArgs)
                binding.button1.setText(state.buttonTextId)
                binding.button1.isVisible = true
            }
        }
    }

    private fun getMessageWithArgs(@StringRes messageResId: Int, args: List<Any>): CharSequence {
        val html = if (args.isNotEmpty()) {
            resources.getString(messageResId, *args.toTypedArray())
        } else {
            resources.getString(messageResId)
        }
        return HtmlCompat.fromHtml(html, 0)
    }
}

typealias OnButtonClickListener = () -> Unit
