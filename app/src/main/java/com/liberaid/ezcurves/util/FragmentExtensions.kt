package com.liberaid.ezcurves.util

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

fun FragmentManager.safeTransaction(action: FragmentTransaction.() -> Unit): Boolean {
    try {
        beginTransaction()
            .also { action(it) }
            .commit()

        return true
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return false
}