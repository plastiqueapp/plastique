package io.plastique.notifications

import io.plastique.core.content.ContentState
import io.plastique.core.lists.ListItem
import io.plastique.core.snackbar.SnackbarState

data class NotificationsViewState(
    val contentState: ContentState,
    val isSignedIn: Boolean,

    val items: List<ListItem> = emptyList(),
    val contentItems: List<ListItem> = emptyList(),

    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val snackbarState: SnackbarState? = null
) {
    val isPagingEnabled: Boolean
        get() = contentState === ContentState.Content && hasMore && !isLoadingMore && !isRefreshing

    override fun toString(): String {
        return "NotificationsViewState(" +
                "contentState=$contentState, " +
                "isSignedIn=$isSignedIn, " +
                "items=${items.size}, " +
                "contentItems=${contentItems.size}, " +
                "hasMore=$hasMore, " +
                "isLoadingMore=$isLoadingMore, " +
                "isRefreshing=$isRefreshing, " +
                "snackbarState=$snackbarState" +
                ")"
    }
}
