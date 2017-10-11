# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
#忽略警告
-ignorewarnings

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.support.v4.app.Fragment

-keep class org.apache.log4j{*;}
-keep class de.mindpipe.android.logging.log4j.**{*;}
-keep class com.j256.ormlite.**{*;}
-keep class com.newland.pay.tools.**{*;}
-keep class com.loopj.android.http.**{*;}
-keep class org.apache.http.**{*;}
-keep class com.google.code.gson.**{*;}
-keep class com.centerm.epos.bean.**{*;}

-keep class com.centerm.smartpos.aidl.**{*;}
-keep class com.barcodejni.**{*;}
-keep class com.centerm.smartzbar.**{*;}
-keep class com.centerm.smartzbarlib.**{*;}
-keep class com.zbar.**{*;}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepclassmembers class com.centerm.cpay.payment.bean.**{
    <fields>;
    void set*(***);
    *** get*();
}
