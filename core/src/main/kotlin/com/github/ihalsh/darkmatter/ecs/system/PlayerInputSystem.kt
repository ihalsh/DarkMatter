package com.github.ihalsh.darkmatter.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.github.ihalsh.darkmatter.di.providers.ViewportProvider
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import com.github.ihalsh.darkmatter.utils.facingComponent
import com.github.ihalsh.darkmatter.utils.transformComponent
import ktx.ashley.allOf
import ktx.inject.Context
import ktx.log.logger

class PlayerInputSystem(context: Context) : IteratingSystem(
    allOf(PlayerComponent::class, TransformComponent::class, FacingComponent::class).get()
) {
    private val gameViewport = context.inject<ViewportProvider>().gameViewport
    private val tmpVec2 = Vector2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing = entity.facingComponent
        val transform = entity.transformComponent

        // Convert screen coordinates of mouse pointer to world coordinates
        gameViewport.unproject(tmpVec2.apply { x = getMouseXInWorld() })

        val diffX = tmpVec2.x - transform.position.x - transform.size.x / 2f
        facing.direction = when {
            diffX < -TOUCH_TOLERANCE -> FacingComponent.Direction.LEFT
            diffX > TOUCH_TOLERANCE -> FacingComponent.Direction.RIGHT
            else -> FacingComponent.Direction.DEFAULT
        }
    }

    private fun getMouseXInWorld(): Float = Gdx.input.x.toFloat()

    companion object {
        private val LOG = logger<PlayerInputSystem>()

        private const val TOUCH_TOLERANCE = 0.2f
    }
}
