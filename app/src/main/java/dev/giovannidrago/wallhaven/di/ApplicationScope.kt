package dev.giovannidrago.wallhaven.di

import javax.inject.Qualifier

/** Marks a process-wide [kotlinx.coroutines.CoroutineScope] for fire-and-forget background work. */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope
