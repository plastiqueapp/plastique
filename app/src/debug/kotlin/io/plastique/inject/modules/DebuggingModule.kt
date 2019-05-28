package io.plastique.inject.modules

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DebuggingModule {
    @Provides
    @Singleton
    @JvmStatic
    fun provideFlipperClient(context: Context): FlipperClient {
        val flipperClient = AndroidFlipperClient.getInstance(context)
        flipperClient.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
        flipperClient.addPlugin(NetworkFlipperPlugin())
        flipperClient.addPlugin(SharedPreferencesFlipperPlugin(context))
        flipperClient.addPlugin(CrashReporterPlugin.getInstance())
        flipperClient.addPlugin(DatabasesFlipperPlugin(context))
        return flipperClient
    }
}
