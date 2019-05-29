package io.plastique.deviations.info

import com.github.technoir42.kotlin.extensions.truncate
import io.plastique.users.User
import org.threeten.bp.ZonedDateTime

data class DeviationInfo(
    val title: String,
    val author: User,
    val publishTime: ZonedDateTime,
    val description: String,
    val tags: List<String>
) {
    @Suppress("MagicNumber")
    override fun toString(): String {
        return "DeviationInfo(" +
                "title=$title, " +
                "author=$author, " +
                "publishTime=$publishTime, " +
                "description=${description.truncate(20)}, " +
                "tags=$tags" +
                ")"
    }
}
