package com.github.ihalsh.darkmatter.screens

import com.badlogic.gdx.Gdx.files
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.ihalsh.darkmatter.DarkMatter
import com.github.ihalsh.darkmatter.UNIT_SCALE
import com.github.ihalsh.darkmatter.ecs.component.GraphicComponent
import com.github.ihalsh.darkmatter.ecs.component.TransformComponent
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.graphics.use
import ktx.log.debug
import ktx.log.logger

private val LOG = logger<GameScreen>()

class GameScreen(game: DarkMatter) : DarkMatterScreen(game) {
    private val viewport = FitViewport(9f, 16f)
    private val playerTexture = Texture(files.internal("graphics/ship_base.png"))
    private val player = engine.entity {
        with<TransformComponent> {
            position.set(1f, 1f, 0f)
        }
        with<GraphicComponent> {
            sprite.run {
                setRegion(playerTexture)
                setSize(texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        }
    }

    override fun show() {
        LOG.debug { "GameScreen is shown" }
    }

    override fun render(delta: Float) {
        engine.update(delta)
        viewport.apply()
        batch.use(viewport.camera.combined) { batch ->
            player[GraphicComponent.mapper]?.let { graphics ->
                player[TransformComponent.mapper]?.let { transform ->
                    graphics.sprite.run {
                        rotation = transform.rotationDeg
                        setBounds(transform.position.x, transform.position.y, transform.size.x, transform.size.y)
                        draw(batch)
                    }
                }
            }
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun dispose() {
        playerTexture.dispose()
    }
}
