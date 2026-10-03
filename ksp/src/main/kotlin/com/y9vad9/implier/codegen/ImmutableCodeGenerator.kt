package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.y9vad9.implier.Immutable
import com.y9vad9.implier.Visibility

object ImmutableCodeGenerator {
    fun generate(
        declaration: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>,
        visibility: Visibility,
        isDataClass: Boolean,
    ): FileSpec {
        val packageName = declaration.packageName.asString()
        val simpleName = declaration.simpleName.asString()
        val className = "Immutable$simpleName"
        val visibilityModifier = if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL

        val typeSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(visibilityModifier)

        if (isDataClass) {
            typeSpecBuilder.addModifiers(KModifier.DATA)
        }

        when (declaration.classKind) {
            ClassKind.INTERFACE -> typeSpecBuilder.addSuperinterface(declaration.toClassName())
            ClassKind.CLASS -> typeSpecBuilder.superclass(declaration.toClassName())
            else -> {}
        }
        typeSpecBuilder.addSuperinterface(Immutable::class.asClassName().parameterizedBy(declaration.toClassName()))

        val constructorBuilder = FunSpec.constructorBuilder()
        for (property in properties) {
            val propName = property.simpleName.asString()
            val propType = property.type.resolve().toTypeName()
            constructorBuilder.addParameter(propName, propType)
            typeSpecBuilder.addProperty(
                PropertySpec.builder(propName, propType)
                    .initializer(propName)
                    .addModifiers(KModifier.OVERRIDE)
                    .build()
            )
        }
        typeSpecBuilder.primaryConstructor(constructorBuilder.build())

        val toImmutableFunction = FunSpec.builder("toImmutable")
            .addModifiers(visibilityModifier)
            .receiver(declaration.toClassName())
            .returns(ClassName(packageName, className))
            .addStatement(
                "return %T(%L)",
                ClassName(packageName, className),
                properties.joinToString(", ") { it.simpleName.asString() }
            )
            .build()

        return FileSpec.builder(packageName, className)
            .addType(typeSpecBuilder.build())
            .addFunction(toImmutableFunction)
            .build()
    }
}
