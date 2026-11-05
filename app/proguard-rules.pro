# Kotlinx Serialization keeps generated serializers for the DTOs in the app package.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.takasu.wallhaven.** {
	*** Companion;
}
-keepclasseswithmembers class com.takasu.wallhaven.** {
	kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.takasu.wallhaven.**$$serializer {
	*;
}

# Retrofit keeps the annotations on the service interface methods.
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
	@retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
