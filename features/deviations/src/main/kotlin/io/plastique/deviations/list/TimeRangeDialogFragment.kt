package io.plastique.deviations.list

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import com.github.technoir42.android.extensions.getCallback
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.core.BaseBottomSheetDialogFragment
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.dialogRoute
import io.plastique.deviations.R
import io.plastique.deviations.TimeRange

class TimeRangeDialogFragment : BaseBottomSheetDialogFragment(R.layout.dialog_time_range), View.OnClickListener {
    private var onTimeRangeSelectedListener: OnTimeRangeSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onTimeRangeSelectedListener = getCallback()
    }

    override fun onDetach() {
        super.onDetach()
        onTimeRangeSelectedListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        require(view is ViewGroup)
        val layoutInflater = view.layoutInflater
        for (timeRange in TimeRange.values()) {
            val textView = layoutInflater.inflate(R.layout.item_time_range, view, false) as TextView
            textView.setText(getTimeRangeResId(timeRange))
            textView.tag = timeRange
            textView.setOnClickListener(this)
            view.addView(textView)
        }
    }

    override fun onClick(view: View) {
        onTimeRangeSelectedListener?.onTimeRangeSelected(view.tag as TimeRange)
        dismiss()
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
