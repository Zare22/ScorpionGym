-keep class org.sqlite.** { *; }

# kotlinx-datetime is pulled in transitively (via Compose) and carries optional
# kotlinx-serialization support. We don't use serialization, so those classes
# aren't on the classpath; ProGuard would otherwise abort on the dangling refs.
-dontwarn kotlinx.datetime.**
-dontwarn kotlinx.serialization.**