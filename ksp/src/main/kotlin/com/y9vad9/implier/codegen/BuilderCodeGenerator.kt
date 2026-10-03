package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.y9vad9.implier.BuilderImpl
import com.y9vad9.implier.Visibility
import com.y9vad9.implier.capitalizeFirst

object BuilderCodeGenerator {
    fun generate(
        declaration: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>,
        visibility: Visibility,
        builderType: BuilderImpl.Type,
        preferImmutable: Boolean,
    ): FileSpec {
        val packageName = declaration.packageName.asString()
        val simpleName = declaration.simpleName.asString()
        val className = "${simpleName}Builder"
        val visibilityModifier = if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL
        val builderClass = ClassName(packageName, className)

        val targetRealizationName = if (preferImmutable) "Immutable$simpleName" else "Mutable$simpleName"
        val targetClass = ClassName(packageName, targetRealizationName)

        val typeSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(visibilityModifier)

        for (property in properties) {
            val propName = property.simpleName.asString()
            val resolvedType = property.type.resolve()
            val propType = resolvedType.toTypeName()
            val isNullable = resolvedType.isMarkedNullable

            val propBuilder = PropertySpec.builder(propName, propType)
                .mutable(true)
                .addModifiers(KModifier.PRIVATE)

            if (isNullable) {
                propBuilder.initializer("null")
            } else {
                propBuilder.delegate("kotlin.properties.Delegates.notNull()")
            }
            typeSpecBuilder.addProperty(propBuilder.build())

            val methodName = if (builderType == BuilderImpl.Type.WITHOUT_ACCESSORS) {
                propName
            } else {
                "set" + propName.capitalizeFirst()
            }

            typeSpecBuilder.addFunction(
                FunSpec.builder(methodName)
                    .addModifiers(visibilityModifier)
                    .addParameter("value", propType)
                    .returns(builderClass)
                    .addStatement("this.%N = value", propName)
                    .addStatement("return this")
                    .build()
            )
        }

        typeSpecBuilder.addFunction(
            FunSpec.builder("build")
                .addModifiers(visibilityModifier)
                .returns(declaration.toClassName())
                .addStatement(
                    "return %T(%L)",
                    targetClass,
                    properties.joinToString(", ") { it.simpleName.asString() }
                )
                .build()
        )

        return FileSpec.builder(packageName, className)
            .addType(typeSpecBuilder.build())
            .build()
    }
}
