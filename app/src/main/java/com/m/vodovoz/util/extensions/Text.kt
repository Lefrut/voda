package com.m.vodovoz.util.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.style.URLSpan
import androidx.core.text.HtmlCompat
import java.text.DecimalFormat


fun String.extractLinksFromHtml(): List<String> {
    val spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
    return spanned.getSpans(0, spanned.length, URLSpan::class.java).map { it.url }
}


fun Context.copyText(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip: ClipData = ClipData.newPlainText("Copied text", text)
    clipboard.setPrimaryClip(clip)
}

fun Context.shareText(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}


fun String.prepareServiceHtml(): String {
    return "<style>\n" +
            "    img {\n" +
            "        display: inline;\n" +
            "        height: auto;\n" +
            "        max-width: 100%;\n" +
            "    }\n" +
            "    .ordered-block,\n" +
            "    .order_sale {\n" +
            "        display: none;\n" +
            "    }\n" +
            "    .tables-responsive {\n" +
            "        overflow-x: auto;\n" +
            "        width: 100%;\n" +
            "    }\n" +
            "    table {\n" +
            "        width: 100%;\n" +
            "        border-collapse: collapse;\n" +
            "    }\n" +
            "    .colored_table {\n" +
            "        font-size: 2.5em;\n" +
            "        line-height: inherit;\n" +
            "    }\n" +
            "    .colored_table th,\n" +
            "    .colored_table td {\n" +
            "        padding: 8px;\n" +
            "        border: 1px solid #ccc;\n" +
            "        text-align: left;\n" +
            "    }" +
            "</style>$this"
}


fun String.decodeUnicodeEscapes(): String {
    val replacements = mapOf(
        "\\u2028" to "\n",
        "\\u2029" to "\n",
        "\\u200B" to "",
    )

    val regex = Regex("""\\u([0-9a-fA-F]{4})""")
    return regex.replace(this) { matchResult ->
        replacements.forEach { (unicode, new) ->
            if(unicode == matchResult.value) return@replace new
        }
        return@replace matchResult.value
    }
}

fun formatRating(rating: Float): String {
    val df = DecimalFormat("#.#")
    df.decimalFormatSymbols = df.decimalFormatSymbols.apply {
        decimalSeparator = '.'
    }
    return df.format(rating)
}


