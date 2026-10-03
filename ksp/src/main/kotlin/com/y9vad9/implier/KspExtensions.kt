package com.y9vad9.implier

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.util.Locale

internal fun KSAnnotated.findAnnotation(qualifiedName: String): KSAnnotation? {
    return annotations.firstOrNull {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }
}

internal fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean {
    return findAnnotation(qualifiedName) != null
}

internal fun KSAnnotation.getArgument(name: String): Any? {
    return arguments.firstOrNull { it.name?.asString() == name }?.value
}

internal fun KSAnnotation.getVisibility(): Visibility {
    val value = getArgument("visibility")
    val name = when (value) {
        is KSClassDeclaration -> value.simpleName.asString()
        is Visibility -> value.name
        else -> value?.toString()?.substringAfterLast('.')
    }
    return if (name == "INTERNAL") Visibility.INTERNAL else Visibility.PUBLIC
}

internal fun KSAnnotation.getBuilderType(): BuilderImpl.Type {
    val value = getArgument("type")
    val name = when (value) {
        is KSClassDeclaration -> value.simpleName.asString()
        is BuilderImpl.Type -> value.name
        else -> value?.toString()?.substringAfterLast('.')
    }
    return if (name == "WITH_ACCESSORS") BuilderImpl.Type.WITH_ACCESSORS else BuilderImpl.Type.WITHOUT_ACCESSORS
}

internal fun KSAnnotation.getDSLBuilderType(): DSLBuilderImpl.Type {
    val value = getArgument("type")
    val name = when (value) {
        is KSClassDeclaration -> value.simpleName.asString()
        is DSLBuilderImpl.Type -> value.name
        else -> value?.toString()?.substringAfterLast('.')
    }
    return when (name) {
        "WITH_ACCESSORS" -> DSLBuilderImpl.Type.WITH_ACCESSORS
        "WITHOUT_ACCESSORS" -> DSLBuilderImpl.Type.WITHOUT_ACCESSORS
        else -> DSLBuilderImpl.Type.PROPERTY_ACCESS
    }
}

internal fun KSAnnotation.getDSLFunctionName(): String? {
    return getArgument("functionName") as? String
}

internal fun String.capitalizeFirst(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
}
