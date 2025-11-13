package com.m.vodovoz.util.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.style.URLSpan
import androidx.annotation.Keep
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

@Keep
private val serviceHtml = """
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        p, h3, h2, h1, blockquote, ul, ol, table {
            margin-top: 16px;    
            margin-bottom: 16px;
        }

       @font-face {
            font-family: 'Roboto';
            src: url('file:///android_asset/fonts/roboto_regular.ttf');
        }
        
        body {
            font-size: 1em;
            font-family: 'Roboto';
            line-height: 1.5;
        }
    
        blockquote {
            position: relative;
            padding: 0px 20px 0px 41px;
            border: none;
            font-weight: normal;
            line-height: calc(1em + 10px);
            margin: 48px 0px;
            border-left: 5px solid #05A4FF;
        }
        
        
        img {
            display: inline;
            height: auto;
            max-width: 100%;
        }
        .ordered-block,
        .order_sale {
            display: none;
        }
        .tables-responsive {
            overflow-x: auto;
            width: 100%;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        .colored_table {
            font-size: 1em;
            line-height: inherit;
        }
        .colored_table th,
        .colored_table td {
            padding: 8px;
            border: 1px solid #ccc;
            text-align: left;
        }
    </style>
"""


@Keep
fun String.prepareServiceHtml(): String {
    return serviceHtml + this
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
            if (unicode == matchResult.value) return@replace new
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


