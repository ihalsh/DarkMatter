package com.github.ihalsh.darkmatter

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        initialize(DarkMatter(), AndroidApplicationConfiguration())
    }
}