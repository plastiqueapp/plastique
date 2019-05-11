package io.plastique.deviations.info

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.plastique.core.MvvmActivity
import io.plastique.core.content.ContentState
import io.plastique.core.content.ContentStateController
import io.plastique.core.content.EmptyView
import io.plastique.core.extensions.setActionBar
import io.plastique.core.navigation.navigationContext
import io.plastique.deviations.DeviationsActivityComponent
import io.plastique.deviations.DeviationsNavigator
import io.plastique.deviations.R
import io.plastique.deviations.info.DeviationInfoEvent.RetryClickEvent
import io.plastique.glide.GlideApp
import io.plastique.inject.getComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class DeviationInfoActivity : MvvmActivity<DeviationInfoViewModel>() {
    @Inject lateinit var navigator: DeviationsNavigator

    private lateinit var authorNameView: TextView
    private lateinit var authorAvatarView: ImageView
    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var publishDateView: TextView
    private lateinit var emptyView: EmptyView
    private lateinit var contentStateController: ContentStateController
    private lateinit var tagsView: RecyclerView
    private lateinit var tagListAdapter: TagListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deviation_info)
        setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        authorNameView = findViewById(R.id.deviation_author_name)
        authorAvatarView = findViewById(R.id.deviation_author_avatar)
        titleView = findViewById(R.id.deviation_title)
        descriptionView = findViewById(R.id.deviation_description)
        descriptionView.movementMethod = LinkMovementMethod.getInstance()
        tagsView = findViewById(R.id.deviation_tags)
        publishDateView = findViewById(R.id.publish_date)
        emptyView = findViewById(android.R.id.empty)
        emptyView.setOnButtonClickListener { viewModel.dispatch(RetryClickEvent) }

        tagListAdapter = TagListAdapter(onTagClick = { tag -> navigator.openTag(navigationContext, tag) })
        tagsView.adapter = tagListAdapter
        tagsView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        contentStateController = ContentStateController(this, R.id.content, android.R.id.progress, android.R.id.empty)

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
                titleView.text = state.title
                authorNameView.text = state.author.name
                descriptionView.text = state.description.value
                publishDateView.text = PUBLISH_DATE_FORMATTER.format(state.publishTime)

                GlideApp.with(this)
                    .load(state.author.avatarUrl)
                    .fallback(R.drawable.default_avatar_64dp)
                    .circleCrop()
                    .dontAnimate()
                    .into(authorAvatarView)

                tagListAdapter.items = state.tags
                tagListAdapter.notifyDataSetChanged()
                tagsView.isVisible = state.tags.isNotEmpty()
            }
            is DeviationInfoViewState.Error -> {
                contentStateController.state = ContentState.Empty(state.emptyViewState)
                emptyView.state = state.emptyViewState
            }
        }
    }

    override fun injectDependencies() {
        getComponent<DeviationsActivityComponent>().inject(this)
    }

    companion object {
        private const val EXTRA_DEVIATION_ID = "deviation_id"
        private val PUBLISH_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

        fun createIntent(context: Context, deviationId: String): Intent {
            return Intent(context, DeviationInfoActivity::class.java)
                .putExtra(EXTRA_DEVIATION_ID, deviationId)
        }
    }
}
