package com.github.ihalsh.darkmatter.screens

import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.ecs.component.FacingComponent
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.PlayerComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<GameScreen>()

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {

    override fun show() {
        LOG.debug { "GameScreen is shown" }
        engine.entity {
            with<TransformComponent> {
                position.set(4f, 8f, 0f)
            }
            with<GraphicComponent>()
            with<PlayerComponent>()
            with<FacingComponent>()
        }
    }

    override fun render(delta: Float) {
        engine.update(delta)
    }
}
