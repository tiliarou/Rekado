package com.pavelrekun.rekado

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.pavelrekun.rekado.services.utils.LoginUtils
import com.pavelrekun.penza.Penza
import com.pavelrekun.penza.data.Color
import com.pavelrekun.penza.data.Theme
import com.pavelrekun.penza.services.enums.Project
import com.pavelrekun.penza.services.theme.PenzaTheme
import com.pavelrekun.rekado.services.utils.MemoryUtils

class RekadoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

        MemoryUtils.parseBundledSchema()

        configureInternalSystems()
        configureThemeEngine()
    }

    private fun configureThemeEngine() {
        Penza.context = this.applicationContext
        val penzaTheme = PenzaTheme(Color.LIGHT_BLUE_500, Theme.BLACK)
        Penza.initialize(penzaTheme, Project.REKADO, true)
    }

    private fun configureInternalSystems() {
        LoginUtils.init(false)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

    }

}