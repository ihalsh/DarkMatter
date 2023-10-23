package com.github.ihalsh.darkmatter.di.providers

import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ReflectionPool
import com.github.ihalsh.darkmatter.event.GameEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ktx.async.RenderingScope
import ktx.collections.getOrPut
import kotlin.reflect.KClass

interface EventDispatcher {
    val events: SharedFlow<GameEvent>

    fun <T : GameEvent> dispatchEvent(type: KClass<T>, action: T.() -> Unit = {})
}

class EventDispatcherImpl : EventDispatcher, Disposable, CoroutineScope by RenderingScope() {
    private val eventPools = ObjectMap<KClass<out GameEvent>, ReflectionPool<out GameEvent>>()

    private val _events = MutableSharedFlow<GameEvent>(replay = 1)
    override val events = _events.asSharedFlow()

    override fun <T : GameEvent> dispatchEvent(type: KClass<T>, action: T.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val pool = eventPools.getOrPut(type) { ReflectionPool(type.java) } as ReflectionPool<T>
        val event = pool.obtain() as T
        event.apply(action)

        launch {
            _events.emit(event)
            pool.free(event)
        }
    }

    override fun dispose() {
        eventPools.clear()
        cancel()
    }
}
