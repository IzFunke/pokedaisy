package com.pokedaisy.app.n3ds

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pokedaisy.app.FeatureFlags
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on an ARM64 Android device with `-Ppokedaisy.n3ds=true`; no commercial game data is needed. */
@RunWith(AndroidJUnit4::class)
class N3dsNativeInitTest {
    @Test fun azaharAndroidIntegrationInitializes() {
        org.junit.Assume.assumeTrue("Requires the optional Azahar build", FeatureFlags.n3ds())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val n3dsDir = File(context.filesDir, "n3ds-smoke")
        assertTrue("Could not create the private Azahar test directory", n3dsDir.isDirectory || n3dsDir.mkdirs())
        assertTrue("libpk_n3ds did not load", N3dsNative.available)
        assertTrue("Azahar Android/JNI initialization failed", N3dsNative.init(n3dsDir.absolutePath))
    }
}
