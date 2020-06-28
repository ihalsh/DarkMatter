package com.github.ihalsh.darkmatter.screens

import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.ecs.component.*
import ktx.ashley.entity
import ktx.ashley.with
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<GameScreen>()

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {

    override fun show() {
        LOG.debug { "GameScreen is shown" }
        engine.entity {
            with<PlayerComponent>()
            with<FacingComponent>()
            with<TransformComponent> { position.set(4f, 8f, 0f) }
            with<GraphicComponent>()
            with<MoveComponent>()
        }
    }

    override fun render(delta: Float) {
        engine.update(delta)
    }
}
