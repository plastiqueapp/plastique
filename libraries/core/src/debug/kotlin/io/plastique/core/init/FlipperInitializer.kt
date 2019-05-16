package io.plastique.core.init

import android.content.Context
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.soloader.SoLoader
import javax.inject.Inject
import javax.inject.Provider

class FlipperInitializer @Inject constructor(
    private val context: Context,
    private val flipperClient: Provider<FlipperClient>
) : Initializer() {

    override fun initialize() {
        SoLoader.init(context, false)
        if (FlipperUtils.shouldEnableFlipper(context)) {
            flipperClient.get().start()
        }
    }
}
