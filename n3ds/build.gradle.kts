// 3DS games: Azahar (third_party/azahar) and our front end to it (src/main/cpp), packed as libpk_n3ds.so into the app.
// No Kotlin here: the JNI functions belong to the app's com.pokedaisy.app.n3ds.N3dsNative, which loads this library only
// when BuildConfig.FEATURE_N3DS says it was built in. Included by settings.gradle.kts only with the n3ds flag.
plugins {
    id("com.android.library")
}

android {
    namespace = "com.pokedaisy.n3ds"
    compileSdk = 34
    ndkVersion = "28.2.13676358"

    // Optional x86_64 build for an Android emulator on an x86_64 host. Production and device builds stay ARM64-only.
    val n3dsX86_64 = providers.gradleProperty("pokedaisy.n3dsX86_64")
        .map(String::toBoolean).getOrElse(false)

    defaultConfig {
        // Azahar needs Android 10; the app (minSdk 26) overrides this and only loads the library on 29+.
        minSdk = 29
        ndk {
            // Azahar's JIT (dynarmic) and our devices: arm64. An optional x86_64 emulator build can be enabled below.
            abiFilters += listOf("arm64-v8a") + if (n3dsX86_64) listOf("x86_64") else emptyList()
        }
        externalNativeBuild {
            cmake {
                arguments += listOf("-DANDROID_STL=c++_shared", "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
                if (!n3dsX86_64) {
                    // cryptopp wants NEON on the supported ARM64 device build.
                    arguments += "-DANDROID_ARM_NEON=true"
                }
                // Only ours: AGP otherwise builds and packs every shared library the project defines (Azahar's own
                // Android front end, SPIR-V tools), ~40 MB nobody loads.
                targets += "pk_n3ds"
            }
        }
    }

    buildTypes {
        // Unoptimised, a 3DS game can't run at all: debug builds compile the core as a release would.
        debug { externalNativeBuild { cmake { arguments += "-DCMAKE_BUILD_TYPE=Release" } } }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.31.6"
        }
    }
}
