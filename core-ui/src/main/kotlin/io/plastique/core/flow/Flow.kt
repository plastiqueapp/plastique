package io.plastique.core.flow

import com.sch.rxjava2.extensions.DisposableObservable
import com.sch.rxjava2.extensions.autoConnectDisposable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.UnicastSubject
import timber.log.Timber

data class Next<out State : Any, out Effect : Any>(
    val state: State,
    val effects: List<Effect>
)

fun <State : Any, Effect : Any> next(state: State, vararg effects: Effect) = Next(state, effects.toList())

typealias Reducer<Event, State, Effect> = (state: State, event: Event) -> Next<State, Effect>
typealias EffectHandler<Effect, Event> = (effects: Observable<Effect>) -> Observable<Event>

class MainLoop<in Event : Any, State : Any, in Effect : Any>(
    private val reducer: Reducer<Event, State, Effect>,
    private val effectHandler: EffectHandler<Effect, Event> = { Observable.empty<Event>() },
    private val externalEvents: Observable<out Event> = Observable.empty(),
    private val listener: Listener<Event, State, Effect>? = null
) {
    private val events = UnicastSubject.create<Event>()
    private val effects = UnicastSubject.create<Effect>()

    fun dispatch(event: Event) {
        events.onNext(event)
    }

    fun loop(initialState: State, vararg initialEffects: Effect): DisposableObservable<State> {
        return loop(next(initialState, *initialEffects))
    }

    fun loop(initialStateAndEffects: Next<State, Effect>): DisposableObservable<State> {
        val effectHandlerEvents = effects
                .observeOn(Schedulers.io())
                .doOnNext { effect -> listener?.onEffect(effect) }
                .publish { effects -> effectHandler(effects) }

        return Observable.merge(events, externalEvents, effectHandlerEvents)
                .observeOn(Schedulers.computation())
                .doOnNext { event -> listener?.onEvent(event) }
                .scan(initialStateAndEffects) { next, event -> reducer(next.state, event) }
                .doOnNext { next -> next.effects.forEach { effect -> effects.onNext(effect) } }
                .map { next -> next.state }
                .distinctUntilChanged()
                .doOnNext { state -> listener?.onState(state) }
                .replay(1)
                .autoConnectDisposable()
    }

    interface Listener<in Event : Any, in State : Any, in Effect : Any> {
        fun onEvent(event: Event)

        fun onState(state: State)

        fun onEffect(effect: Effect)
    }
}

fun <Event : Any, State : Any, Effect : Any> compose(vararg listeners: MainLoop.Listener<Event, State, Effect>): MainLoop.Listener<Event, State, Effect> {
    return CompositeListener(listeners)
}

private class CompositeListener<in Event : Any, in State : Any, in Effect : Any>(
    private val listeners: Array<out MainLoop.Listener<Event, State, Effect>>
) : MainLoop.Listener<Event, State, Effect> {
    override fun onEvent(event: Event) {
        listeners.forEach { it.onEvent(event) }
    }

    override fun onState(state: State) {
        listeners.forEach { it.onState(state) }
    }

    override fun onEffect(effect: Effect) {
        listeners.forEach { it.onEffect(effect) }
    }
}

class TimberLogger(private val tag: String) : MainLoop.Listener<Any, Any, Any> {
    override fun onEvent(event: Any) {
        Timber.tag(tag).d("%s", event)
    }

    override fun onState(state: Any) {
        Timber.tag(tag).d("%s", state)
    }

    override fun onEffect(effect: Any) {
        Timber.tag(tag).d("%s", effect)
    }
}
