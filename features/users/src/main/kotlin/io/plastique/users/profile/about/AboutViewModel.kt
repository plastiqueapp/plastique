package io.plastique.users.profile.about

import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ViewModel
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.users.profile.UserProfileRepository
import io.plastique.users.profile.about.AboutEffect.LoadEffect
import io.plastique.users.profile.about.AboutEvent.LoadErrorEvent
import io.plastique.users.profile.about.AboutEvent.RetryClickEvent
import io.plastique.users.profile.about.AboutEvent.UserProfileChangedEvent
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    stateReducer: AboutStateReducer,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    lateinit var state: Observable<AboutViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG))

    fun init(username: String) {
        if (::state.isInitialized) return

        val initialState = AboutViewState.Loading(username = username)
        state = loop.loop(initialState, LoadEffect(username)).disposeOnDestroy()
    }

    fun dispatch(event: AboutEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<AboutEffect>): Observable<AboutEvent> {
        return effects.ofType<LoadEffect>()
                .switchMap { effect ->
                    userProfileRepository.getUserProfileByName(effect.username)
                            .map<AboutEvent> { userProfile -> UserProfileChangedEvent(userProfile) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(error) }
                }
    }

    companion object {
        private const val LOG_TAG = "AboutViewModel"
    }
}

class AboutStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider,
    private val richTextFormatter: RichTextFormatter
) : Reducer<AboutEvent, AboutViewState, AboutEffect> {
    override fun invoke(state: AboutViewState, event: AboutEvent): Next<AboutViewState, AboutEffect> = when (event) {
        is UserProfileChangedEvent -> {
            next(AboutViewState.Content(
                    username = state.username,
                    bio = event.userProfile.bio?.let { SpannedWrapper(richTextFormatter.format(it)) } ?: SpannedWrapper.EMPTY))
        }

        is LoadErrorEvent -> {
            next(AboutViewState.Error(username = state.username, emptyState = errorMessageProvider.getErrorState(event.error)))
        }

        RetryClickEvent -> {
            next(AboutViewState.Loading(state.username), LoadEffect(state.username))
        }
    }
}
