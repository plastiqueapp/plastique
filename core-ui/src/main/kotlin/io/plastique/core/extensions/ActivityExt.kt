package io.plastique.core.extensions

import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.setActionBar(@IdRes id: Int, init: ActionBar.() -> Unit = {}): Toolbar {
    val toolbar = findViewById<Toolbar>(id)
    setSupportActionBar(toolbar)
    supportActionBar!!.init()
    return toolbar
}
