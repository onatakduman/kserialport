# Add project specific ProGuard rules here.

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Google Play Billing
-keep class com.android.vending.billing.** { *; }

# Keep R classes for AdMob
-keepclassmembers class **.R$* { public static <fields>; }

# Uncomment this to preserve the line number information for debugging stack traces.
#-keepattributes SourceFile,LineNumberTable
