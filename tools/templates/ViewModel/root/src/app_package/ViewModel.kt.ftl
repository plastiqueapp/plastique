package ${escapeKotlinIdentifiers(packageName)}

import com.sch.neon.MainLoop
import com.sch.neon.StateReducer
import com.sch.neon.StateWithEffects
import com.sch.neon.timber.TimberLogger
import io.plastique.core.BaseViewModel
import io.plastique.core.content.ContentState
import io.reactivex.Observable
import javax.inject.Inject

class ${viewModelName} @Inject constructor(
    stateReducer: ${reducerName},
    effectHandler: ${effectHandlerName}
): BaseViewModel() {

    lateinit var state: Observable<${viewStateName}>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = effectHandler,
            <#if externalEvents>
            externalEvents = externalEvents(),
            </#if>
            listener = TimberLogger(LOG_TAG))

    fun init() {
        if (::state.isInitialized) return

        val initialState = ${viewStateName}(
                contentState = ContentState.Loading)

        state = loop.loop(initialState).disposeOnDestroy()
    }

    fun dispatch(event: ${eventName}) {
        loop.dispatch(event)
    }
    <#if externalEvents>

    private fun externalEvents() : Observable<${eventName}> {
        return Observable.empty()
    }
    </#if>

    companion object {
        private const val LOG_TAG = "${viewModelName}"
    }
}

class ${effectHandlerName} @Inject constructor() : EffectHandler<${effectName}, ${eventName}> {
    override fun handle(effects: Observable<${effectName}>): Observable<${eventName}> {
        TODO()
    }
}

class ${reducerName} @Inject constructor() : StateReducer<${eventName}, ${viewStateName}, ${effectName}> {
    override fun reduce(state: ${viewStateName}, event: ${eventName}): StateWithEffects<${viewStateName}, ${effectName}> {
        TODO()
    }
}
