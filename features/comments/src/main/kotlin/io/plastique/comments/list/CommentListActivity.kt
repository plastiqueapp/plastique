package io.plastique.comments.list

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.add
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsActivityComponent
import io.plastique.comments.R
import io.plastique.core.BaseActivity
import io.plastique.core.ScrollableToTop
import io.plastique.core.navigation.Route
import io.plastique.core.navigation.activityRoute
import io.plastique.inject.getComponent

class CommentListActivity : BaseActivity(R.layout.activity_comment_list) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()

        if (savedInstanceState == null) {
            val username = intent.getParcelableExtra<CommentThreadId>(EXTRA_THREAD_ID)!!
            supportFragmentManager.beginTransaction()
                .add<CommentListFragment>(R.id.comments_container, args = CommentListFragment.newArgs(username))
                .commit()
        }
    }

    override fun injectDependencies() {
        getComponent<CommentsActivityComponent>().inject(this)
    }

    private fun initToolbar() {
        val toolbar = setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        val onClickListener = View.OnClickListener {
            val contentFragment = supportFragmentManager.findFragmentById(R.id.comments_container) as ScrollableToTop
            contentFragment.scrollToTop()
        }
        toolbar.setTitleOnClickListener(onClickListener)
        toolbar.setSubtitleOnClickListener(onClickListener)
    }

    companion object {
        private const val EXTRA_THREAD_ID = "thread_id"

        fun route(context: Context, threadId: CommentThreadId): Route = activityRoute<CommentListActivity>(context) {
            putExtra(EXTRA_THREAD_ID, threadId)
        }
    }
}
