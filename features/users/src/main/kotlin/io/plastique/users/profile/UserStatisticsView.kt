package io.plastique.users.profile

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.github.technoir42.android.extensions.layoutInflater
import io.plastique.users.R
import io.plastique.users.databinding.ViewUsersProfileStatisticsBinding
import io.plastique.util.CompactDecimalFormatter

class UserStatisticsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val binding = ViewUsersProfileStatisticsBinding.inflate(layoutInflater, this)

    var onWatchersClick: OnWatchersClickListener = {}

    init {
        binding.watchers.setOnClickListener { onWatchersClick() }
    }

    fun render(stats: UserProfile.Stats) {
        binding.deviationsLabel.text = resources.getQuantityString(R.plurals.users_profile_statistics_deviations, stats.deviations)
        binding.deviationsValue.text = CompactDecimalFormatter.format(stats.deviations)
        binding.favoritesLabel.text = resources.getQuantityString(R.plurals.users_profile_statistics_favorites, stats.favorites)
        binding.favoritesValue.text = CompactDecimalFormatter.format(stats.favorites)
        binding.watchersLabel.text = resources.getQuantityString(R.plurals.users_profile_statistics_watchers, stats.watchers)
        binding.watchersValue.text = CompactDecimalFormatter.format(stats.watchers)
    }
}

private typealias OnWatchersClickListener = () -> Unit
