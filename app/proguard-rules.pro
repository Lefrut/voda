# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-dontobfuscate


-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

-keep class com.m.vodovoz.common.water_app.** { *; }
-keepclassmembers class com.m.vodovoz.common.water_app.** {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keepnames class kotlin.Metadata { *; }

-keepclassmembers class com.rajat.pdfviewer.PdfRendererView {
    private android.widget.TextView pageNo;
}
