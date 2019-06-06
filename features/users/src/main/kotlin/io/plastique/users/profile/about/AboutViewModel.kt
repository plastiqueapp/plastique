package io.plastique.users.profile.about

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
import io.plastique.core.mvvm.BaseViewModel
import io.plastique.core.text.RichTextFormatter
import io.plastique.core.text.SpannedWrapper
import io.plastique.users.profile.UserProfileRepository
import io.plastique.users.profile.about.AboutEffect.LoadEffect
import io.plastique.users.profile.about.AboutEvent.LoadErrorEvent
import io.plastique.users.profile.about.AboutEvent.RetryClickEvent
import io.plastique.users.profile.about.AboutEvent.UserProfileChangedEvent
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import timber.log.Timber
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    stateReducer: AboutStateReducer,
    effectHandlerFactory: AboutEffectHandlerFactory
) : BaseViewModel() {

    lateinit var state: Observable<AboutViewState>
    private val loop = MainLoop(
        reducer = stateReducer,
        effectHandler = effectHandlerFactory.create(screenVisible),
        listener = TimberLogger(LOG_TAG))

    fun init(username: String) {
        if (::state.isInitialized) return

        val initialState = AboutViewState.Loading(username = username)
        state = loop.loop(initialState, LoadEffect(username)).disposeOnDestroy()
    }

    fun dispatch(event: AboutEvent) {
        loop.dispatch(event)
    }

    companion object {
        private const val LOG_TAG = "AboutViewModel"
    }
}

@AutoFactory
class AboutEffectHandler(
    @Provided private val userProfileRepository: UserProfileRepository,
    private val screenVisible: Observable<Boolean>
) : EffectHandler<AboutEffect, AboutEvent> {

    override fun handle(effects: Observable<AboutEffect>): Observable<AboutEvent> {
        return effects.ofType<LoadEffect>()
            .switchMap { effect ->
                userProfileRepository.getUserProfileByName(effect.username)
                    .valveLatest(screenVisible)
                    .map<AboutEvent> { userProfile -> UserProfileChangedEvent(userProfile) }
                    .doOnError(Timber::e)
                    .onErrorReturn { error -> LoadErrorEvent(error) }
            }
    }
}

class AboutStateReducer @Inject constructor(
    private val errorMessageProvider: ErrorMessageProvider,
    private val richTextFormatter: RichTextFormatter
) : StateReducer<AboutEvent, AboutViewState, AboutEffect> {

    override fun reduce(state: AboutViewState, event: AboutEvent): StateWithEffects<AboutViewState, AboutEffect> = when (event) {
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
