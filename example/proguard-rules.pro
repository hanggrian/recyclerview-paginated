# retain data classes for gson
-keep class com.example.recyclerview_paginated.Post

# jdk8 and kotlin
-dontwarn javax.annotation.**
-dontwarn java.lang.invoke.*
-dontwarn kotlin.internal.*

# design
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView { boolean mShiftingMode; }

# square
-dontwarn okio.**
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions