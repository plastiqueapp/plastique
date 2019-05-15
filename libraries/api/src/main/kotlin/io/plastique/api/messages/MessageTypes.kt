package io.plastique.api.messages

object MessageTypes {
    const val BADGE_GIVEN = "feedback.badge_given"
    const val COLLECT = "feedback.collect"
    const val COMMENT = "feedback.comment"
    const val DAILY_DEVIATION = "feedback.dd_to_author"
    const val DAILY_DEVIATION_ACCEPTED = "feedback.dd_to_suggester"
    const val FAVORITE = "feedback.favourite"
    const val MENTION_DEVIATION_IN_COMMENT = "mention.comment_mentions_deviation"
    const val MENTION_DEVIATION_IN_DEVIATION = "mention.deviation_mentions_deviation"
    const val MENTION_DEVIATION_IN_STATUS = "mention.status_mentions_deviation"
    const val MENTION_USER_IN_COMMENT = "mention.comment_mentions_user"
    const val MENTION_USER_IN_DEVIATION = "mention.deviation_mentions_user"
    const val MENTION_USER_IN_STATUS = "mention.status_mentions_user"
    const val REPLY = "feedback.reply"
    const val WATCH = "feedback.watch"
}
