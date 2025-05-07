package com.vodovoz.app.design_system.utils

import android.content.res.Resources
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.xml.sax.XMLReader

fun Spanned.toAnnotatedString(
    linkStyles: TextLinkStyles? = null,
    linkInteractionListener: LinkInteractionListener? = null
): AnnotatedString {
    val builder = AnnotatedString.Builder(this.toString())

    getSpans(0, length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        when (span) {

            is RelativeSizeSpan -> {
                builder.addStyle(SpanStyle(fontSize = span.sizeChange.em), start, end)
            }

            is StyleSpan -> {
                when (span.style) {
                    Typeface.BOLD -> builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    Typeface.ITALIC -> builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    Typeface.BOLD_ITALIC -> builder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end
                    )
                }
            }

            is UnderlineSpan -> builder.addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline), start, end
            )

            is StrikethroughSpan -> builder.addStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough), start, end
            )

            is ForegroundColorSpan -> builder.addStyle(
                SpanStyle(color = Color(span.foregroundColor)), start, end
            )

            is BackgroundColorSpan -> builder.addStyle(
                SpanStyle(background = Color(span.backgroundColor)), start, end
            )

            is TypefaceSpan -> {
                if (span.family == "monospace") {
                    builder.addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, end)
                }
            }

            is AbsoluteSizeSpan -> {
                val sizeInSp = if (span.dip) span.size.toFloat() else (span.size.toFloat() / Resources.getSystem().displayMetrics.scaledDensity)
                builder.addStyle(SpanStyle(fontSize = sizeInSp.sp), start, end)
            }

            is URLSpan -> {
                val url = span.url
                builder.addStringAnnotation(tag = "URL", annotation = url, start = start, end = end)
                val urlStyle = linkStyles?.style
                if (urlStyle != null) {
                    builder.addStyle(urlStyle, start, end)
                }
                linkInteractionListener?.let {
                    builder.addStringAnnotation(tag = "INTERACT", annotation = url, start = start, end = end)
                }
            }

            is ClickableSpan -> {
                val id = span.hashCode().toString()
                builder.addStringAnnotation(tag = "CLICKABLE", annotation = id, start = start, end = end)
                linkInteractionListener?.let {
                    builder.addStyle(linkStyles?.style ?: SpanStyle(color = Color.Blue), start, end)
                }
            }
        }
    }

    return builder.toAnnotatedString()
}

object HeadingTagHandler : Html.TagHandler {

    private var startIndex = 0
    private var currentTag: String? = null

    override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
        val tagName = tag?.lowercase() ?: return

        if (tagName in listOf("h1", "h2", "h3", "h4", "h5", "h6")) {
            if (opening) {
                startIndex = output?.length ?: 0
                currentTag = tagName
            } else {
                val endIndex = output?.length ?: 0
                val sizeFactor = when (tagName) {
                    "h1" -> 2.0f
                    "h2" -> 1.5f
                    "h3" -> 1.3f
                    "h4" -> 1.1f
                    "h5" -> 1.0f
                    "h6" -> 0.9f
                    else -> 1.0f
                }

                output?.apply {
                    setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        RelativeSizeSpan(sizeFactor),
                        startIndex,
                        endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }
}
