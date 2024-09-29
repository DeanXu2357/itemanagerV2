package com.example.itemanagerv2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.example.itemanagerv2.R
import androidx.compose.ui.text.TextStyle

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val NotoSansFont = GoogleFont("Noto Sans")

val NotoSansFontFamily = FontFamily(
    Font(googleFont = NotoSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NotoSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NotoSansFont, fontProvider = provider, weight = FontWeight.Bold)
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = NotoSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    // 定義其他文本樣式...
)

@Composable
fun NotoSansText(text: String, weight: FontWeight = FontWeight.Normal) {
    Text(
        text = text,
        fontFamily = NotoSansFontFamily,
        fontWeight = weight
    )
}

@Composable
fun BaseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = AppTypography,
        content = content
    )
}