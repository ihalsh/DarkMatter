package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import ktx.ashley.allOf
import ktx.ashley.get

class RemoveSystem : IteratingSystem(allOf(RemoveComponent::class).get()) {

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val remove = entity[RemoveComponent.mapper]
        require(remove != null) {"$entity should have RemoveComponent to be removed."}

        remove.delay -= deltaTime
        if (remove.delay <= 0f) engine.removeEntity(entity)
    }
}