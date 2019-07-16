package io.plastique.inject.modules

import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import io.plastique.collections.deviations.CollectionDeviationFetcher
import io.plastique.collections.deviations.CollectionDeviationParams
import io.plastique.core.paging.Cursor
import io.plastique.deviations.DailyDeviationFetcher
import io.plastique.deviations.DailyParams
import io.plastique.deviations.DeviationFetcher
import io.plastique.deviations.FetchParams
import io.plastique.deviations.HotDeviationFetcher
import io.plastique.deviations.HotParams
import io.plastique.deviations.PopularDeviationFetcher
import io.plastique.deviations.PopularParams
import io.plastique.deviations.UndiscoveredDeviationFetcher
import io.plastique.deviations.UndiscoveredParams
import io.plastique.feed.BucketDeviationFetcher
import io.plastique.feed.BucketDeviationParams
import io.plastique.gallery.deviations.GalleryDeviationFetcher
import io.plastique.gallery.deviations.GalleryDeviationParams

@Module
interface DeviationsModule {
    @Binds
    @IntoMap
    @ClassKey(HotParams::class)
    fun bindHotDeviationFetcher(impl: HotDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>

    @Binds
    @IntoMap
    @ClassKey(PopularParams::class)
    fun bindPopularDeviationFetcher(impl: PopularDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>

    @Binds
    @IntoMap
    @ClassKey(UndiscoveredParams::class)
    fun bindUndiscoveredDeviationFetcher(impl: UndiscoveredDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>

    @Binds
    @IntoMap
    @ClassKey(DailyParams::class)
    fun bindDailyDeviationFetcher(impl: DailyDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>

    @Binds
    @IntoMap
    @ClassKey(CollectionDeviationParams::class)
    fun bindCollectionDeviationFetcher(impl: CollectionDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>

    @Binds
    @IntoMap
    @ClassKey(GalleryDeviationParams::class)
    fun bindGalleryDeviationFetcher(impl: GalleryDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>

    @Binds
    @IntoMap
    @ClassKey(BucketDeviationParams::class)
    fun bindBucketDeviationFetcher(impl: BucketDeviationFetcher): DeviationFetcher<out FetchParams, out Cursor>
}
