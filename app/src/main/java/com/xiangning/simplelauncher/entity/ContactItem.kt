package com.xiangning.simplelauncher.entity

import android.graphics.drawable.Drawable

data class ContactItem(
    val name: String?,
    val avatar: Drawable? = null,
    val phone: String? = null
)