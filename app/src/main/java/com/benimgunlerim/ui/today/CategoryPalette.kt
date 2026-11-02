package com.benimgunlerim.ui.today

import androidx.compose.ui.graphics.Color
import com.benimgunlerim.ui.theme.CategoryColor
import com.benimgunlerim.ui.theme.CategoryTones
import java.util.Locale
import kotlin.math.abs

/**
 * Kategoriye kararlı renk: önce anahtar kelime, yoksa adın hash'iyle 6 doğal tondan biri.
 * Tonlar: 0 yaprak, 1 deniz, 2 erik, 3 kiremit, 4 hardal, 5 taş.
 */
object CategoryPalette {
    private const val Leaf = 0
    private const val Sea = 1
    private const val Plum = 2
    private const val Brick = 3
    private const val Mustard = 4
    private const val Stone = 5

    /** Sıra önemli: ilk eşleşen kazanır. */
    private val KEYWORD_TONES: List<Pair<String, Int>> = listOf(
        "spor" to Leaf, "sağlık" to Leaf, "egzersiz" to Leaf, "yürüyüş" to Leaf, "beslenme" to Leaf,
        "iş" to Sea, "işler" to Sea, "toplantı" to Sea, "sunum" to Sea, "proje" to Sea, "fatura" to Sea,
        "gelişim" to Plum, "kişisel" to Plum, "not" to Plum, "günlük" to Plum,
        "oku" to Plum, "ders" to Plum, "öğren" to Plum, "kitap" to Plum, "dil" to Plum,
        "sosyal" to Brick, "aile" to Brick, "çocuk" to Brick, "arkadaş" to Brick,
        "para" to Mustard, "finans" to Mustard, "yatırım" to Mustard,
        "ev" to Stone, "temizlik" to Stone, "market" to Stone, "alışveriş" to Stone,
    )

    fun toneFor(seed: String): CategoryColor {
        val v = seed.lowercase(Locale("tr", "TR")).trim().ifEmpty { return CategoryTones[Leaf] }
        val index = KEYWORD_TONES.firstOrNull { (kw, _) -> v.contains(kw) }?.second
            ?: (abs(v.hashCode()) % CategoryTones.size)
        return CategoryTones[index]
    }

    fun colorFor(seed: String): Color = toneFor(seed).fg
}
