package com.ixeken.nierika

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de la aplicación Nierika.
 * Inicializa Hilt para la inyección de dependencias a nivel global.
 */
@HiltAndroidApp
class NierikaApplication : Application()
