package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.y9vad9.implier.Visibility

object FactoryFunctionCodeGenerator {
    fun generate(
        declaration: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>,
        visibility: Visibility,
        preferImmutable: Boolean,
    ): FileSpec {
        val packageName = declaration.packageName.asString()
        val simpleName = declaration.simpleName.asString()
        val fileName = "${simpleName}Factory"
        val visibilityModifier = if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL

        val targetRealizationName = if (preferImmutable) "Immutable$simpleName" else "Mutable$simpleName"
        val targetClass = ClassName(packageName, targetRealizationName)

        val factoryFunctionBuilder = FunSpec.builder(simpleName)
            .addModifiers(visibilityModifier)
            .returns(declaration.toClassName())

        for (property in properties) {
            factoryFunctionBuilder.addParameter(
                property.simpleName.asString(),
                property.type.resolve().toTypeName()
            )
        }

        factoryFunctionBuilder.addStatement(
            "return %T(%L)",
            targetClass,
            properties.joinToString(", ") { it.simpleName.asString() }
        )

        return FileSpec.builder(packageName, fileName)
            .addFunction(factoryFunctionBuilder.build())
            .build()
    }
}
