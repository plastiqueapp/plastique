package io.plastique.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.main.MainPage

class NotificationsFragment : Fragment(), MainPage {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun getTitle(): Int = R.string.notifications_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }
}
