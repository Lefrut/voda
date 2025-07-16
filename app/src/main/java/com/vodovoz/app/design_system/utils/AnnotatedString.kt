package com.vodovoz.app.design_system.utils

import android.content.res.Resources
import android.graphics.Typeface
import android.text.Layout
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

fun Spanned.toAnnotatedString(
    linkStyles: TextLinkStyles? = null,
    linkInteractionListener: LinkInteractionListener? = null,
): AnnotatedString {
    val builder = AnnotatedString.Builder(toString())

    getSpans(0, length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        when (span) {
            is AlignmentSpan -> {
                builder.addStyle(span.toParagraphStyle(), start, end)
            }

            is SubscriptSpan -> {
                builder.addStyle(SpanStyle(baselineShift = BaselineShift.Subscript), start, end)
            }

            is SuperscriptSpan -> {
                builder.addStyle(SpanStyle(baselineShift = BaselineShift.Superscript), start, end)
            }

            is RelativeSizeSpan -> {
                builder.addStyle(SpanStyle(fontSize = span.sizeChange.em), start, end)
            }

            is StyleSpan -> {
                when (span.style) {
                    Typeface.BOLD -> builder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start,
                        end
                    )

                    Typeface.ITALIC -> builder.addStyle(
                        SpanStyle(fontStyle = FontStyle.Italic),
                        start,
                        end
                    )

                    Typeface.BOLD_ITALIC -> builder.addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        start,
                        end
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
                builder.addStyle(span.toSpanStyle(), start, end)
            }

            is AbsoluteSizeSpan -> {
                val sizeInSp =
                    if (span.dip) span.size.toFloat() else (span.size.toFloat() / Resources.getSystem().displayMetrics.scaledDensity)
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
                    builder.addStringAnnotation(
                        tag = "INTERACT",
                        annotation = url,
                        start = start,
                        end = end
                    )
                }
            }

            is ClickableSpan -> {
                val id = span.hashCode().toString()
                builder.addStringAnnotation(
                    tag = "CLICKABLE",
                    annotation = id,
                    start = start,
                    end = end
                )
                linkInteractionListener?.let {
                    builder.addStyle(linkStyles?.style ?: SpanStyle(color = Color.Blue), start, end)
                }
            }
        }
    }

    return builder.toAnnotatedString()
}

private fun TypefaceSpan.toSpanStyle(): SpanStyle {
    val fontFamily =
        when (family) {
            FontFamily.Cursive.name -> FontFamily.Cursive
            FontFamily.Monospace.name -> FontFamily.Monospace
            FontFamily.SansSerif.name -> FontFamily.SansSerif
            FontFamily.Serif.name -> FontFamily.Serif
            else -> FontFamily.Monospace
        }
    return SpanStyle(fontFamily = fontFamily)
}

private fun AlignmentSpan.toParagraphStyle(): ParagraphStyle {
    val alignment =
        when (this.alignment) {
            Layout.Alignment.ALIGN_NORMAL -> TextAlign.Start
            Layout.Alignment.ALIGN_CENTER -> TextAlign.Center
            Layout.Alignment.ALIGN_OPPOSITE -> TextAlign.End
            else -> TextAlign.Unspecified
        }
    return ParagraphStyle(textAlign = alignment)
}