package io.plastique.core.extensions

import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.requireViewById

fun AppCompatActivity.setActionBar(@IdRes id: Int, init: ActionBar.() -> Unit = {}): Toolbar {
    val toolbar = requireViewById<Toolbar>(this, id)
    setSupportActionBar(toolbar)
    supportActionBar!!.init()
    return toolbar
}
