package com.benimgunlerim.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Renk sistemi kuralı: ham renk (`Color(0x…)`) ve ham palet (`Palette.`) sadece `ui/theme` içinde.
 * Ekran ve bileşenler `MaterialTheme.colorScheme` veya `AppTheme.colors` rollerini kullanır.
 */
class ColorUsageGuardTest {
    private val sourceRoot = listOf(
        File("src/main/java/com/benimgunlerim"),
        File("app/src/main/java/com/benimgunlerim"),
    ).first { it.exists() }

    @Test
    fun rawColorsLiveOnlyInTheme() {
        val offenders = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { it.invariantSeparatorsPath.contains("/ui/theme/") }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    val raw = line.contains("Color(0x") || Regex("""\bPalette\.""").containsMatchIn(line)
                    if (raw) "${file.relativeTo(sourceRoot)}:${index + 1}: ${line.trim()}" else null
                }
            }
            .toList()
        assertTrue("Tema dışında ham renk:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }
}
