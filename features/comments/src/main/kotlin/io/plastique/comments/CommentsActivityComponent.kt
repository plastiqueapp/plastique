package io.plastique.comments

import io.plastique.comments.list.CommentListActivity

interface CommentsActivityComponent {
    fun inject(activity: CommentListActivity)
}
