package io.plastique.users

import android.text.TextUtils
import com.sch.rxjava2.extensions.ofType
import io.plastique.core.ErrorMessageProvider
import io.plastique.core.ResourceProvider
import io.plastique.core.ViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.plastique.core.flow.next
import io.plastique.core.snackbar.SnackbarState
import io.plastique.inject.scopes.ActivityScope
import io.plastique.users.UserProfileEffect.CopyProfileLinkEffect
import io.plastique.users.UserProfileEffect.LoadUserProfileEffect
import io.plastique.users.UserProfileEvent.CopyProfileLinkClickEvent
import io.plastique.users.UserProfileEvent.LoadErrorEvent
import io.plastique.users.UserProfileEvent.RetryClickEvent
import io.plastique.users.UserProfileEvent.SnackbarShownEvent
import io.plastique.users.UserProfileEvent.UserProfileChangedEvent
import io.plastique.util.Clipboard
import io.plastique.util.HtmlCompat
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class UserProfileViewModel @Inject constructor(
    stateReducer: UserProfileStateReducer,
    private val clipboard: Clipboard,
    private val userProfileRepository: UserProfileRepository,
    private val errorMessageProvider: ErrorMessageProvider,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    lateinit var state: Observable<UserProfileViewState>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
            listener = TimberLogger(LOG_TAG)
    )

    fun init(username: String) {
        if (::state.isInitialized) return

        val initialState = UserProfileViewState(
                username = username,
                contentState = ContentState.Loading,
                title = username)

        state = loop.loop(initialState, LoadUserProfileEffect(username)).disposeOnDestroy()
    }

    fun dispatch(event: UserProfileEvent) {
        loop.dispatch(event)
    }

    private fun effectHandler(effects: Observable<UserProfileEffect>): Observable<UserProfileEvent> {
        return Observable.merge(
                handleLoadUserProfile(effects),
                handleCopyProfileLink(effects))
    }

    private fun handleLoadUserProfile(effects: Observable<UserProfileEffect>): Observable<UserProfileEvent> {
        return effects.ofType<LoadUserProfileEffect>()
                .switchMap { effect ->
                    userProfileRepository.getUserProfileByName(effect.username)
                            .map<UserProfileEvent> { userProfile -> UserProfileChangedEvent(userProfile) }
                            .doOnError(Timber::e)
                            .onErrorReturn { error -> LoadErrorEvent(getErrorState(error)) }
                }
    }

    private fun handleCopyProfileLink(effects: Observable<UserProfileEffect>): Observable<UserProfileEvent> {
        return effects.ofType<CopyProfileLinkEffect>()
                .map { effect -> clipboard.setText(effect.profileUrl) }
                .ignoreElements()
                .toObservable()
    }

    private fun getErrorState(error: Throwable): EmptyState = when (error) {
        is NoSuchUserException -> EmptyState(
                message = HtmlCompat.fromHtml(resourceProvider.getString(R.string.common_message_user_not_found, TextUtils.htmlEncode(error.username))))
        else -> errorMessageProvider.getErrorState(error)
    }

    companion object {
        private const val LOG_TAG = "UserProfileViewModel"
    }
}

class UserProfileStateReducer @Inject constructor(
    private val resourceProvider: ResourceProvider
) : Reducer<UserProfileEvent, UserProfileViewState, UserProfileEffect> {
    override fun invoke(state: UserProfileViewState, event: UserProfileEvent): Next<UserProfileViewState, UserProfileEffect> = when (event) {
        is UserProfileChangedEvent -> {
            next(state.copy(
                    contentState = ContentState.Content,
                    userProfile = event.userProfile,
                    title = event.userProfile.user.name))
        }

        is LoadErrorEvent -> {
            next(state.copy(contentState = ContentState.Empty(event.emptyState, isError = true)))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading), LoadUserProfileEffect(state.username))
        }

        CopyProfileLinkClickEvent -> {
            next(state.copy(snackbarState = SnackbarState.Message(resourceProvider.getString(R.string.common_message_link_copied))),
                    CopyProfileLinkEffect(state.userProfile!!.profileUrl))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = SnackbarState.None))
        }
    }
}
