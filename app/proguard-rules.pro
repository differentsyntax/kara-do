# kotlinx.serialization: keep generated serializers for recipe and fixture classes.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class io.github.differentsyntax.karado.**$$serializer { *; }
-keepclassmembers class io.github.differentsyntax.karado.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.differentsyntax.karado.** {
    kotlinx.serialization.KSerializer serializer(...);
}
