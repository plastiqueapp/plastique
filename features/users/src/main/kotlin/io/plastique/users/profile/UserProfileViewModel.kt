package io.plastique.users.profile

import com.github.technoir42.rxjava2.extensions.valveLatest
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.sch.neon.EffectHandler
import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.next
import com.sch.neon.timber.TimberLogger
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.content.ContentState
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.session.SessionManager
import io.plastique.core.session.userId
import io.plastique.core.session.userIdChanges
import io.plastique.core.snackbar.SnackbarState
import io.plastique.users.R
import io.plastique.users.UsersNavigator
import io.plastique.users.profile.UserProfileEffect.CopyProfileLinkEffect
import io.plastique.users.profile.UserProfileEffect.LoadUserProfileEffect
import io.plastique.users.profile.UserProfileEffect.NavigationEffect
import io.plastique.users.profile.UserProfileEffect.NavigationEffect.OpenBrowserEffect
import io.plastique.users.profile.UserProfileEffect.NavigationEffect.OpenSignInEffect
import io.plastique.users.profile.UserProfileEffect.SetWatchingEffect
import io.plastique.users.profile.UserProfileEffect.SignOutEffect
import io.plastique.users.profile.UserProfileEvent.CopyProfileLinkClickEvent
import io.plastique.users.profile.UserProfileEvent.LoadErrorEvent
import io.plastique.users.profile.UserProfileEvent.OpenInBrowserEvent
import io.plastique.users.profile.UserProfileEvent.RetryClickEvent
import io.plastique.users.profile.UserProfileEvent.SetWatchingErrorEvent
import io.plastique.users.profile.UserProfileEvent.SetWatchingEvent
import io.plastique.users.profile.UserProfileEvent.SetWatchingFinishedEvent
import io.plastique.users.profile.UserProfileEvent.SignOutEvent
import io.plastique.users.profile.UserProfileEvent.SnackbarShownEvent
import io.plastique.users.profile.UserProfileEvent.UserChangedEvent
import io.plastique.users.profile.UserProfileEvent.UserProfileChangedEvent
import io.plastique.util.Clipboard
import io.plastique.watch.WatchManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    stateReducer: UserProfileStateReducer,
    effectHandlerFactory: UserProfileEffectHandlerFactory,
    val navigator: UsersNavigator,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    lateinit var state: Observable<UserProfileViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(navigator, screenVisible),
        externalEvents = externalEvents(),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String) {
        if (::state.isInitialized) return

        val initialState = UserProfileViewState(
            username = username,
            contentState = ContentState.Loading,
            currentUserId = sessionManager.session.userId,
            title = username)

        state = loop.loop(initialState, LoadUserProfileEffect(username)).disposeOnDestroy()
    }

    fun dispatch(event: UserProfileEvent) {
        loop.dispatch(event)
    }

    private fun externalEvents(): Observable<UserProfileEvent> {
        return sessionManager.userIdChanges
            .skip(1)
            .valveLatest(screenVisible)
            .map { userId -> UserChangedEvent(userId.toNullable()) }
    }

    companion object {
        private const val LOG_TAG = "UserProfileViewModel"
    }
}

@AutoFactory
class UserProfileEffectHandler(
    @Provided private val clipboard: Clipboard,
    @Provided private val sessionManager: SessionManager,
    @Provided private val userProfileRepository: UserProfileRepository,
    @Provided private val watchManager: WatchManager,
    private val navigator: UsersNavigator,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<UserProfileEffect, UserProfileEvent> {

    override fun handle(effects: Observable<UserProfileEffect>): Observable<UserProfileEvent> {
        val loadEvents = effects.ofType<LoadUserProfileEffect>()
            .switchMap { effect ->
                userProfileRepository.getUserProfileByName(effect.username)
                    .valveLatest(screenVisible)
                    .map<UserProfileEvent> { userProfile -> UserProfileChangedEvent(userProfile) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }

        val copyProfileLinkEvents = effects.ofType<CopyProfileLinkEffect>()
            .map { effect -> clipboard.setText(effect.profileUrl) }
            .ignoreElements()
            .toObservable<UserProfileEvent>()

        val watchEvents = effects.ofType<SetWatchingEffect>()
            .switchMapSingle { effect ->
                watchManager.setWatching(effect.username, effect.watching)
                    .toSingleDefault<UserProfileEvent>(SetWatchingFinishedEvent)
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> SetWatchingErrorEvent(error) }
            }

        val signOutEvents = effects.ofType<SignOutEffect>()
            .doOnNext { sessionManager.logout() }
            .ignoreElements()
            .toObservable<UserProfileEvent>()

        val navigationEvents = effects.ofType<NavigationEffect>()
            .doOnNext { effect ->
                when (effect) {
                    is OpenBrowserEffect -> navigator.openUrl(effect.url)
                    OpenSignInEffect -> navigator.openSignIn()
                }
            }
            .ignoreElements()
            .toObservable<UserProfileEvent>()

        return Observable.mergeArray(loadEvents, copyProfileLinkEvents, watchEvents, signOutEvents, navigationEvents)
    }
}

class UserProfileStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider
) : StateReducer<UserProfileEvent, UserProfileViewState, UserProfileEffect> {

    override fun reduce(state: UserProfileViewState, event: UserProfileEvent): StateWithEffects<UserProfileViewState, UserProfileEffect> = when (event) {
        is UserProfileChangedEvent -> {
            next(state.copy(
                contentState = ContentState.Content,
                userProfile = event.userProfile,
                title = event.userProfile.user.name,
                emptyState = null))
        }

        is LoadErrorEvent -> {
            next(state.copy(contentState = ContentState.Empty, emptyState = errorMessageProvider.getErrorState(event.error)))
        }

        RetryClickEvent -> {
            next(state.copy(contentState = ContentState.Loading, emptyState = null), LoadUserProfileEffect(state.username))
        }

        CopyProfileLinkClickEvent -> {
            next(state.copy(snackbarState = SnackbarState.Message(R.string.common_message_link_copied)),
                CopyProfileLinkEffect(state.userProfile!!.url))
        }

        OpenInBrowserEvent -> {
            next(state, OpenBrowserEffect(state.userProfile!!.url))
        }

        SnackbarShownEvent -> {
            next(state.copy(snackbarState = null))
        }

        is SetWatchingEvent -> {
            if (state.currentUserId != null) {
                next(state.copy(showProgressDialog = true), SetWatchingEffect(state.username, event.watching))
            } else {
                next(state, OpenSignInEffect)
            }
        }

        SetWatchingFinishedEvent -> {
            next(state.copy(showProgressDialog = false))
        }

        is SetWatchingErrorEvent -> {
            next(state.copy(showProgressDialog = false, snackbarState = SnackbarState.Message(errorMessageProvider.getErrorMessageId(event.error))))
        }

        is UserChangedEvent -> {
            if (state.currentUserId != event.userId) {
                next(state.copy(currentUserId = event.userId), LoadUserProfileEffect(state.username))
            } else {
                next(state)
            }
        }

        SignOutEvent -> {
            next(state.copy(snackbarState = SnackbarState.Message(R.string.users_profile_message_signed_out)), SignOutEffect)
        }
    }
}
