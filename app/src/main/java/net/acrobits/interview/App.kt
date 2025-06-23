package net.acrobits.interview

import android.app.Application
import net.acrobits.interview.test.core.MyPrefs
import net.acrobits.interview.test.core.di.AppModule
import net.acrobits.interview.test.core.utils.Constants
import cz.acrobits.ali.Xml
import cz.acrobits.libsoftphone.Instance

class AcrobitsApp : Application() {

    companion object {
        // Manual DI module
        lateinit var appModule: AppModule
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModule()

        Instance.loadLibrary(this)
        val provisioning = Xml.parse(Constants.PROVISIONING_XML)
        Instance.init(this, provisioning, MyPrefs::class.java)
        Instance.preferences.trafficLogging.set(true)
    }
}