package io.plastique.deviations.info

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.technoir42.android.extensions.disableChangeAnimations
import com.github.technoir42.android.extensions.setActionBar
import io.plastique.core.BaseActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.image.ImageLoader
import io.plastique.core.image.TransformType
import io.plastique.core.mvvm.viewModel
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.core.navigation.navigationContext
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.R
import io.plastique.deviations.databinding.ActivityDeviationInfoBinding
import io.plastique.deviations.info.DeviationInfoEvent.RetryClickEvent
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class DeviationInfoActivity : BaseActivity() {
    @Inject lateinit var navigator: DeviationsNavigator

    private val imageLoader = ImageLoader.from(this)
    private val viewModel: DeviationInfoViewModel by viewModel()

    private lateinit var binding: ActivityDeviationInfoBinding
    private lateinit var tagListAdapter: TagListAdapter
    private lateinit var contentStateController: ContentStateController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviationInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        navigator.attach(navigationContext)

        binding.description.movementMethod = LinkMovementMethod.getInstance()
        binding.empty.onButtonClick = { viewModel.dispatch(RetryClickEvent) }

        tagListAdapter = TagListAdapter(onTagClick = { tag -> navigator.openTag(tag) })
        binding.tags.apply {
            adapter = tagListAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            disableChangeAnimations()
        }

        contentStateController = ContentStateController(this, binding.content, binding.progress, binding.empty)

        val deviationId = intent.getStringExtra(EXTRA_DEVIATION_ID)!!
        viewModel.init(deviationId)
        viewModel.state
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { renderState(it) }
            .disposeOnDestroy()
    }

    private fun renderState(state: DeviationInfoViewState) {
        when (state) {
            is DeviationInfoViewState.Loading -> {
                contentStateController.state = ContentState.Loading
            }

            is DeviationInfoViewState.Content -> {
                contentStateController.state = ContentState.Content
                binding.title.text = state.title
                binding.authorAvatar.contentDescription = getString(R.string.common_avatar_description, state.author.name)
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

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_DEVIATION_ID = "deviation_id"
        private val PUBLISH_DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)

        fun route(context: Context, deviationId: String): Route = activityRoute<DeviationInfoActivity>(context) {
            putExtra(EXTRA_DEVIATION_ID, deviationId)
        }
    }
}
