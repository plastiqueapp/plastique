package io.plastique.users.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.plastique.core.CompactDecimalFormatter
import io.plastique.users.R

class UserStatisticsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private val deviationsLabel: TextView
    private val deviationsValueView: TextView
    private val favoritesLabel: TextView
    private val favoritesValueView: TextView
    private val watchersView: View
    private val watchersLabel: TextView
    private val watchersValueView: TextView

    init {
        orientation = LinearLayout.HORIZONTAL
        inflate(context, R.layout.view_users_profile_statistics, this)
        deviationsLabel = findViewById(R.id.statistics_deviations_label)
        deviationsValueView = findViewById(R.id.statistics_deviations_value)
        favoritesLabel = findViewById(R.id.statistics_favorites_label)
        favoritesValueView = findViewById(R.id.statistics_favorites_value)
        watchersView = findViewById(R.id.statistics_watchers)
        watchersLabel = findViewById(R.id.statistics_watchers_label)
        watchersValueView = findViewById(R.id.statistics_watchers_value)
    }

    fun render(stats: UserProfile.Stats) {
        deviationsLabel.text = resources.getQuantityString(R.plurals.users_profile_statistics_deviations, stats.deviations)
        deviationsValueView.text = CompactDecimalFormatter.format(stats.deviations)
        favoritesLabel.text = resources.getQuantityString(R.plurals.users_profile_statistics_favorites, stats.favorites)
        favoritesValueView.text = CompactDecimalFormatter.format(stats.favorites)
        watchersLabel.text = resources.getQuantityString(R.plurals.users_profile_statistics_watchers, stats.watchers)
        watchersValueView.text = CompactDecimalFormatter.format(stats.watchers)
    }

    fun setOnWatchersClickListener(listener: OnClickListener) {
        watchersView.setOnClickListener(listener)
    }
}
