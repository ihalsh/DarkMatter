package com.github.ihalsh.darkmatter.utils

import com.badlogic.ashley.core.Entity
import com.github.ihalsh.darkmatter.ecs.component.AnimationComponent
import com.github.ihalsh.darkmatter.ecs.component.AttachComponent
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.MoveComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.PowerUpComponent
import com.github.ihalsh.darkmatter.ecs.component.RemoveComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.get

val Entity.playerComponent
    get() = this[PlayerComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a PlayerComponent. entity=${this}" }
            it
        }

val Entity.transformComponent
    get() = this[TransformComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a TransformComponent. entity=${this}" }
            it
        }

val Entity.animationComponent
    get() = this[AnimationComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a AnimationComponent. entity=${this}" }
            it
        }

val Entity.graphicsComponent
    get() = this[GraphicComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a GraphicComponent. entity=${this}" }
            it
        }

val Entity.attachComponent
    get() = this[AttachComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have an AttachComponent. entity=$this" }
            it
        }

val Entity.moveComponent
    get() = this[MoveComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a MoveComponent. entity=$this" }
            it
        }

val Entity.facingComponent
    get() = this[FacingComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a FacingComponent. entity=$this" }
            it
        }

val Entity.powerUpComponent
    get() = this[PowerUpComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a PowerUpComponent. entity=$this" }
            it
        }

val Entity.removeComponent
    get() = this[RemoveComponent.mapper]
        .let {
            require(it != null) { "Entity |entity| must have a RemoveComponent. entity=$this" }
            it
        }
