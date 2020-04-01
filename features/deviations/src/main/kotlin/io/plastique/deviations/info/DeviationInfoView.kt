package io.plastique.deviations.info

import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.OnButtonClickListener
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.deviations.R
import io.plastique.deviations.databinding.ActivityDeviationInfoBinding
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

internal class DeviationInfoView(
    activity: AppCompatActivity,
    onRetryClick: OnButtonClickListener,
    onTagClick: OnTagClickListener
) {
    private val binding = ActivityDeviationInfoBinding.inflate(activity.layoutInflater)
    private val imageLoader = ImageLoader.from(activity)
    private val tagListAdapter = TagListAdapter(onTagClick)
    private val contentStateController: ContentStateController

    init {
        activity.setContentView(binding.root)
        activity.setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        binding.description.movementMethod = LinkMovementMethod.getInstance()
        binding.empty.onButtonClick = onRetryClick
        binding.tags.apply {
            adapter = tagListAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            disableChangeAnimations()
        }

        contentStateController = ContentStateController(activity, binding.content, binding.progress, binding.empty)
    }

    fun render(state: DeviationInfoViewState) {
        when (state) {
            is DeviationInfoViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is DeviationInfoViewState.Content -> {
                contentStateController.state = ContentState.Content
                binding.title.text = state.title
                binding.authorAvatar.contentDescription = binding.root.resources.getString(R.string.common_avatar_description, state.author.name)
                binding.authorName.text = state.author.name
                binding.description.text = state.description.value
                binding.publishDate.text = PUBLISH_DATE_FORMATTER.format(state.publishTime)

                imageLoader.load(state.author.avatarUrl)
                    .params {
                        fallbackDrawable = R.drawable.default_avatar_64dp
                        transforms += TransformType.CircleCrop
                    }
                    .into(binding.authorAvatar)

                tagListAdapter.items = state.tags
                tagListAdapter.notifyDataSetChanged()
                binding.tags.isVisible = state.tags.isNotEmpty()
            }

            is DeviationInfoViewState.Error -> {
                contentStateController.state = ContentState.Empty
                binding.empty.state = state.emptyViewState
            }
        }
    }

    companion object {
        private val PUBLISH_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)
    }
}
