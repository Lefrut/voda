package com.vodovoz.app.util.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.properties.ReadOnlyProperty


fun Context.isVpnActive(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        connectivityManager.activeNetwork ?: return false
    } else {
        return false
    }
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
}

fun Context.getBitmap(drawableId: Int): Bitmap {
    return when (val drawable = drawable(drawableId)) {
        is BitmapDrawable -> {
            BitmapFactory.decodeResource(resources, drawableId);
        }

        is VectorDrawable -> {
            drawable.toBitmap()
        }

        else -> {
            throw IllegalArgumentException("unsupported drawable type");
        }
    }
}


@SuppressLint("UseKtx")
private fun VectorDrawable.toBitmap(): Bitmap {
    val bitmap = createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}


inline fun Fragment.addOnBackPressedCallback(crossinline callback: () -> Unit) {
    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            callback.invoke()
        }
    }
    this.requireActivity().onBackPressedDispatcher.addCallback(
        this.viewLifecycleOwner,
        onBackPressedCallback
    )
}

//fun Context.sp(value: Float) = (value * resources.displayMetrics.scaledDensity)

fun Context.dipF(value: Int): Float = (value * resources.displayMetrics.density)
fun Context.dipF(value: Float): Float = (value * resources.displayMetrics.density)
fun Context.dip(value: Int): Int = dipF(value).toInt()
fun Context.dip(value: Float): Int = dipF(value).toInt()

//fun Context.sp(value: Int): Int = (value * resources.displayMetrics.scaledDensity).toInt()
fun Context.dimen(@DimenRes resource: Int): Int = resources.getDimensionPixelSize(resource)

fun Context.color(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.string(@StringRes resId: Int, vararg formatArgs: Any): String {
    return if (formatArgs.isEmpty()) {
        getString(resId)
    } else {
        getString(resId, *formatArgs)
    }
}

fun Context.drawable(@DrawableRes drawableRes: Int) = ContextCompat.getDrawable(this, drawableRes)


fun Activity.snack(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT,
) {
    Snackbar.make(findViewById(android.R.id.content), message, length).show()
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}



fun View.isKeyboardOpen(): Boolean {
    return ViewCompat.getRootWindowInsets(this)
        ?.isVisible(WindowInsetsCompat.Type.ime())
        ?: false
}

fun Context.unwrap(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextThemeWrapper -> this.unwrap()
        else -> null
    }
}

fun NestedScrollView.scrollViewToTop() {
    this.post {
        this.fling(0)
        this.smoothScrollTo(0, 0)
    }
}

fun longArgs(key: String): ReadOnlyProperty<Fragment, Long> {
    return ReadOnlyProperty { thisRef, _ ->
        val args = thisRef.requireArguments()
        require(args.containsKey(key)) { "Arguments don't contain key $key" }
        requireNotNull(args.getLong(key))
    }
}


fun String.fromHtml(): Spanned {
    val result: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
    return result
}

fun Context.openAppNotificationSettings() {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }

            else -> {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
        }
    }

    kotlin.runCatching { startActivity(intent) }
}

fun Long.millisToItemDate(): String {
    val targetFormat = SimpleDateFormat("MMM_dd_yyyy_hh_mm_a", Locale.ROOT)
    return targetFormat.format(this)
}


fun fetchCurrentDayInTimeMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun Activity.enableFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    insetsController.hide(WindowInsetsCompat.Type.statusBars())
    insetsController.hide(WindowInsetsCompat.Type.navigationBars())
}

fun Activity.disableFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    insetsController.show(WindowInsetsCompat.Type.statusBars())
    insetsController.show(WindowInsetsCompat.Type.navigationBars())
}

inline fun <T : View> T.preDraw(crossinline callBack: (isReady: Boolean) -> Unit) {

    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {

        override fun onPreDraw(): Boolean {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnPreDrawListener(this)
                callBack(true)
            } else {
                callBack(false)
            }
            return true
        }
    })
}


fun Context.openUrl(url: String) {
    kotlin.runCatching {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}

fun Context.window(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.window()
        else -> null
    }