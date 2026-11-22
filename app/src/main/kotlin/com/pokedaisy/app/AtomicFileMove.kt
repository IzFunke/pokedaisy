package com.pokedaisy.app

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Replaces an existing file atomically where the filesystem supports it. */
internal fun moveReplacing(source: File, destination: File) {
    try {
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    } catch (_: Exception) {
        Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
