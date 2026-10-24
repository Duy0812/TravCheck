package com.example.travcheck.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.travcheck.R

// Khởi tạo FontFamily
val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk_bold, FontWeight.Bold))
val PlusJakartaSans = FontFamily(Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal))
val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono_medium, FontWeight.Medium))

// Định nghĩa Typography
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle( // Dùng cho các thẻ Tag, thông số kỹ thuật
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)