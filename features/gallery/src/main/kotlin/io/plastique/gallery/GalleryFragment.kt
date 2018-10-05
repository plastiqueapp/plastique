package io.plastique.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.plastique.core.ExpandableToolbarLayout
import io.plastique.core.MvvmFragment
import io.plastique.core.extensions.args
import io.plastique.core.extensions.withArguments
import io.plastique.inject.getComponent
import io.plastique.main.MainPage
import javax.inject.Inject

class GalleryFragment : MvvmFragment<GalleryViewModel>(), MainPage {
    private lateinit var submitButton: FloatingActionButton
    @Inject lateinit var navigator: GalleryNavigator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        submitButton = view.findViewById(R.id.button_submit)
        submitButton.setOnClickListener {
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val username = args.getString(ARG_USERNAME)
        viewModel.init(username)
    }

    override fun getTitle(): Int = R.string.gallery_title

    override fun createAppBarViews(parent: ExpandableToolbarLayout) {
    }

    override fun injectDependencies() {
        getComponent<GalleryFragmentComponent>().inject(this)
    }

    companion object {
        private const val ARG_USERNAME = "username"

        fun newInstance(username: String? = null): GalleryFragment {
            return GalleryFragment().withArguments {
                putString(ARG_USERNAME, username)
            }
        }
    }
}
