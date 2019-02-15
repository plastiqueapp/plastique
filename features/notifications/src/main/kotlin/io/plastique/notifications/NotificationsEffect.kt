package io.plastique.notifications

import io.plastique.core.flow.Effect

sealed class NotificationsEffect : Effect() {
    object LoadNotificationsEffect : NotificationsEffect()
    object LoadMoreEffect : NotificationsEffect()
    object RefreshEffect : NotificationsEffect()

    data class DeleteMessageEffect(val messageId: String) : NotificationsEffect()
    data class UndoDeleteMessageEffect(val messageId: String) : NotificationsEffect()
}
