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

# Hantera saknade klasser från Dagger/Hilt och Javapoet
-dontwarn dagger.internal.codegen.**
-dontwarn com.squareup.javapoet.**
-dontwarn com.squareup.kotlinpoet.**
-dontwarn javax.lang.model.**
-dontwarn javax.tools.**
-dontwarn com.google.auto.service.AutoService

# Hantera saknade klasser från betalnings-SDK och Gson
-dontwarn com.google.gson.**
-dontwarn com.paymentterminal.vendor.**

# Hantera saknade klasser för USB-seriell kommunikation
-dontwarn com.hoho.android.usbserial.**

# Om du använder Dagger/Hilt, behåll dessa
-keepattributes *Annotation*
-keepattributes Signature

-keep class com.externalprinter.vendor.** { *; }
-dontwarn com.externalprinter.vendor.**
