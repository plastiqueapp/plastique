package io.plastique.comments.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.github.technoir42.android.extensions.instantiate
import com.github.technoir42.android.extensions.setActionBar
import com.github.technoir42.android.extensions.setSubtitleOnClickListener
import com.github.technoir42.android.extensions.setTitleOnClickListener
import io.plastique.comments.CommentThreadId
import io.plastique.comments.CommentsActivityComponent
import io.plastique.comments.R
import io.plastique.core.BaseActivity
import io.plastique.inject.getComponent

class CommentListActivity : BaseActivity() {
    private lateinit var contentFragment: CommentListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_list)
        initToolbar()

        if (savedInstanceState == null) {
            val username = intent.getParcelableExtra<CommentThreadId>(EXTRA_THREAD_ID)!!
            contentFragment = supportFragmentManager.fragmentFactory.instantiate(this, args = CommentListFragment.newArgs(username))
            supportFragmentManager.beginTransaction()
                .add(R.id.comments_container, contentFragment)
                .commit()
        } else {
            contentFragment = supportFragmentManager.findFragmentById(R.id.comments_container) as CommentListFragment
        }
    }

    override fun injectDependencies() {
        getComponent<CommentsActivityComponent>().inject(this)
    }

    private fun initToolbar() {
        val toolbar = setActionBar(R.id.toolbar) {
            setDisplayHomeAsUpEnabled(true)
        }

        val onClickListener = View.OnClickListener { contentFragment.scrollToTop() }
        toolbar.setTitleOnClickListener(onClickListener)
        toolbar.setSubtitleOnClickListener(onClickListener)
    }

    companion object {
        private const val EXTRA_THREAD_ID = "thread_id"

        fun createIntent(context: Context, threadId: CommentThreadId): Intent {
            return Intent(context, CommentListActivity::class.java).apply {
                putExtra(EXTRA_THREAD_ID, threadId)
            }
        }
    }
}
