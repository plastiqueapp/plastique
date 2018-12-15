package io.plastique.comments

import io.plastique.comments.list.CommentListFragment

interface CommentsFragmentComponent {
    fun inject(fragment: CommentListFragment)
}
