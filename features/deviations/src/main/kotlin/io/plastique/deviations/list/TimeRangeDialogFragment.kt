package io.plastique.deviations.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.github.technoir42.android.extensions.getCallback
import com.github.technoir42.android.extensions.layoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.dialogRoute
import io.plastique.deviations.R
import io.plastique.deviations.TimeRange
import io.plastique.deviations.databinding.DialogTimeRangeBinding
import io.plastique.deviations.databinding.ItemTimeRangeBinding

class TimeRangeDialogFragment : BottomSheetDialogFragment() {
    private var onTimeRangeSelectedListener: OnTimeRangeSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onTimeRangeSelectedListener = getCallback()
    }

    override fun onDetach() {
        super.onDetach()
        onTimeRangeSelectedListener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogTimeRangeBinding.inflate(inflater, container, false)
        val layoutInflater = binding.root.layoutInflater
        val onClickListener = View.OnClickListener { view ->
            onTimeRangeSelectedListener?.onTimeRangeSelected(view.tag as TimeRange)
            dismiss()
        }
        for (timeRange in TimeRange.values()) {
            ItemTimeRangeBinding.inflate(layoutInflater, binding.root, true).apply {
                root.setText(getTimeRangeResId(timeRange))
                root.tag = timeRange
                root.setOnClickListener(onClickListener)
            }
        }
        return binding.root
    }

    @StringRes
    private fun getTimeRangeResId(timeRange: TimeRange): Int = when (timeRange) {
        TimeRange.Hours8 -> R.string.deviations_popular_time_range_8_hours
        TimeRange.Hours24 -> R.string.deviations_popular_time_range_24_hours
        TimeRange.Days3 -> R.string.deviations_popular_time_range_3_days
        TimeRange.Week -> R.string.deviations_popular_time_range_1_week
        TimeRange.Month -> R.string.deviations_popular_time_range_1_month
        TimeRange.AllTime -> R.string.deviations_popular_time_range_all_time
    }

    companion object {
        fun route(tag: String): Route = dialogRoute<TimeRangeDialogFragment>(tag)
    }
}

interface OnTimeRangeSelectedListener {
    fun onTimeRangeSelected(timeRange: TimeRange)
}
