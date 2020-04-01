package io.plastique.settings.licenses

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.OnButtonClickListener
import io.plastique.core.lists.DividerItemDecoration
import io.plastique.settings.R
import io.plastique.settings.databinding.ActivityLicensesBinding

internal class LicensesView(
    activity: AppCompatActivity,
    onRetryClick: OnButtonClickListener,
    onLicenseClick: OnLicenseClickListener
) {
    private val binding = ActivityLicensesBinding.inflate(activity.layoutInflater)
    private val licensesAdapter = LicensesAdapter(onLicenseClick)
    private val contentStateController: ContentStateController

    init {
        activity.setContentView(binding.root)
        activity.setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.licenses.apply {
            adapter = licensesAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration.Builder(context)
                .divider(R.drawable.preference_list_divider)
                .viewTypes(LicensesAdapter.TYPE_LICENSE)
                .build())
        }
        binding.empty.onButtonClick = onRetryClick

        contentStateController = ContentStateController(activity, binding.licenses, binding.progress, binding.empty)
    }

    fun render(state: LicensesViewState) {
        when (state) {
            LicensesViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is LicensesViewState.Content -> {
                contentStateController.state = ContentState.Content
                licensesAdapter.update(state.items)
            }

            is LicensesViewState.Empty -> {
                contentStateController.state = ContentState.Empty
                binding.empty.state = state.emptyState
            }
        }
    }
}
