package com.siva.evoke.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.siva.evoke.R
import kotlin.math.roundToInt


fun View.show() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.hide() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun getPixelsFromDp(dp: Int,context: Context) : Int{
    val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.slideDown(context: Context){
    val slideDown = AnimationUtils.loadAnimation(context,
        R.anim.slide_down_anim
    )
    startAnimation(slideDown)
    visibility = View.GONE
}

fun View.slideUp(context: Context){
    val slideUp = AnimationUtils.loadAnimation(context,
        R.anim.slide_up_anim
    )
    startAnimation(slideUp)
    visibility = View.VISIBLE
}
