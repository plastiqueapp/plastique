package ${escapeKotlinIdentifiers(packageName)}

import io.plastique.core.BaseViewModel
import io.plastique.core.content.ContentState
import io.plastique.core.flow.MainLoop
import io.plastique.core.flow.Next
import io.plastique.core.flow.Reducer
import io.plastique.core.flow.TimberLogger
import io.reactivex.Observable
import javax.inject.Inject

class ${viewModelName} @Inject constructor(
    stateReducer: ${reducerName}
): BaseViewModel() {

    lateinit var state: Observable<${viewStateName}>
    private val loop = MainLoop(
            reducer = stateReducer,
            effectHandler = ::effectHandler,
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

    private fun effectHandler(effects: Observable<${effectName}>): Observable<${eventName}> {
        return Observable.empty()
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

class ${reducerName} @Inject constructor() : Reducer<${eventName}, ${viewStateName}, ${effectName}> {
    override fun invoke(state: ${viewStateName}, event: ${eventName}): Next<${viewStateName}, ${effectName}> {
        TODO()
    }
}
