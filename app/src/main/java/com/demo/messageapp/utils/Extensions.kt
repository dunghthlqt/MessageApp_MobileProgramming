package com.demo.messageapp.utils

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.demo.messageapp.R

fun NavController.navigateToHomeAndClearBackStack() {
    this.navigate(R.id.homeFragment, null,
        NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build())
}