package com.benimgunlerim.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Katman 0 — ham renk paleti. Sadece `ui/theme` içindeki rol tanımları kullanır;
 * ekran ve bileşen dosyaları renklere `MaterialTheme.colorScheme` ya da `AppTheme.colors` üzerinden erişir.
 *
 * Aileler ve anlamları (DESIGN.md › Renk):
 * - Kâğıt/Mürekkep: zemin ve metin
 * - Yaprak: marka, "yapabileceğin şey" (aksiyon, seçim, gezinme)
 * - Kayısı: ilerleme, "yaptığın şey" (yıldız, ışın, zincir, XP, seri)
 * - Gece: kapanış ve dinlenme
 * - Kiremit: sadece hata ve geri alınamaz işlem
 * - Altın: sadece dükkan parası
 * - Deniz: bilgilendirme
 *
 * Kontrast: kâğıt zeminde metin en az ...600 tonu. Kayısı 500 metin için kullanılmaz (2.3:1) — Kayısı 700 kullan.
 */
internal object Palette {
    // Çayır / Mürekkep — zemin, tepelerin devamı olan açık adaçayı; kartlar neredeyse beyaz.
    // (Ad "Paper" kalıyor: rolü zemin/yüzey; tonu çayır.)
    val Paper50 = Color(0xFFFCFDFB)
    val Paper100 = Color(0xFFE1E9DC)
    val Paper200 = Color(0xFFD5DFCF)
    val Paper300 = Color(0xFFC9D5C3)
    val Paper400 = Color(0xFFAEBBA8)
    val Paper500 = Color(0xFF8E968B)
    val Paper600 = Color(0xFF6B6F68)
    val Paper700 = Color(0xFF545A55)
    val Ink = Color(0xFF1E2420)

    // Yaprak (marka yeşili)
    val Leaf50 = Color(0xFFE8F5EE)
    val Leaf100 = Color(0xFFCDEBDA)
    val Leaf300 = Color(0xFF7FBF98)
    val Leaf500 = Color(0xFF2F8A5C)
    val Leaf700 = Color(0xFF0B6C43)
    val Leaf900 = Color(0xFF004C2D)

    // Kayısı (ilerleme)
    val Apricot50 = Color(0xFFFBEFDD)
    val Apricot100 = Color(0xFFF7DDBB)
    val Apricot200 = Color(0xFFF2B887)
    val Apricot500 = Color(0xFFE9944A)
    val Apricot600 = Color(0xFFB9651F)
    val Apricot700 = Color(0xFF9A4E12)
    val Apricot900 = Color(0xFF5E3B10)

    // Gece
    val Night900 = Color(0xFF14213D)
    val Night700 = Color(0xFF1E2C4D)
    val Night400 = Color(0xFF3A4A70)
    val Night300 = Color(0xFF6B7699)
    val NightMuted = Color(0xFFC9CBDA)
    val MoonText = Color(0xFFF5EDE0)
    val Moonlight = Color(0xFFF5D9A8)

    // Kiremit (hata)
    val Brick50 = Color(0xFFFBE9E4)
    val Brick200 = Color(0xFFF0C2B5)
    val Brick500 = Color(0xFFB5452E)
    val Brick700 = Color(0xFF8E3322)

    // Altın (dükkan parası)
    val Gold50 = Color(0xFFF7EBC6)
    val Gold200 = Color(0xFFE8CC7A)
    val Gold500 = Color(0xFFC99A2E)
    val Gold700 = Color(0xFF8A6512)

    // Deniz (bilgi)
    val Sea50 = Color(0xFFE4EFF3)
    val Sea200 = Color(0xFFB9D3DD)
    val Sea700 = Color(0xFF3D6F86)

    // Kategori tonları (kâğıtla uyumlu, birbirinden ayırt edilebilir)
    val CatLeaf = Color(0xFF3F7A5A)
    val CatSea = Color(0xFF3D6F86)
    val CatPlum = Color(0xFF7A4E7E)
    val Plum50 = Color(0xFFF1E7F1)
    val CatBrick = Color(0xFFB5583B)
    val CatMustard = Color(0xFFA88422)
    val CatStone = Color(0xFF6B6F68)

    // Gökyüzü fazları
    val SkyMorning = Color(0xFFF6DCA8)
    val SkyNoon = Color(0xFFBFDCE3)
    val SkyEvening = Color(0xFFF2B887)
}
