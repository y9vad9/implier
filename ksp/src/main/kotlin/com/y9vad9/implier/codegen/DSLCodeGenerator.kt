package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.y9vad9.implier.DSLBuilderImpl
import com.y9vad9.implier.Visibility
import com.y9vad9.implier.capitalizeFirst

object DSLCodeGenerator {
    fun generate(
        declaration: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>,
        visibility: Visibility,
        dslType: DSLBuilderImpl.Type,
        functionName: String,
        preferImmutable: Boolean,
    ): FileSpec {
        val packageName = declaration.packageName.asString()
        val simpleName = declaration.simpleName.asString()
        val className = "${simpleName}DSLBuilderScope"
        val fileName = "${simpleName}BuilderScope"
        val visibilityModifier = if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL
        val scopeClass = ClassName(packageName, className)

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

            if (isNullable) {
                propBuilder.initializer("null")
            } else {
                propBuilder.delegate("kotlin.properties.Delegates.notNull()")
            }
            typeSpecBuilder.addProperty(propBuilder.build())

            when (dslType) {
                DSLBuilderImpl.Type.PROPERTY_ACCESS -> {
                    // Property access only
                }
                DSLBuilderImpl.Type.WITHOUT_ACCESSORS -> {
                    typeSpecBuilder.addFunction(
                        FunSpec.builder(propName)
                            .addParameter("value", propType)
                            .returns(scopeClass)
                            .addStatement("this.%N = value", propName)
                            .addStatement("return this")
                            .build()
                    )
                }
                DSLBuilderImpl.Type.WITH_ACCESSORS -> {
                    val methodName = "set" + propName.capitalizeFirst()
                    typeSpecBuilder.addFunction(
                        FunSpec.builder(methodName)
                            .addParameter("value", propType)
                            .returns(scopeClass)
                            .addStatement("this.%N = value", propName)
                            .addStatement("return this")
                            .build()
                    )
                }
            }
        }

        val dslFunction = FunSpec.builder(functionName)
            .addModifiers(visibilityModifier)
            .addParameter(
                "block",
                LambdaTypeName.get(
                    receiver = scopeClass,
                    returnType = Unit::class.asTypeName()
                )
            )
            .returns(declaration.toClassName())
            .addStatement("val dslBuilder = %T()", scopeClass)
            .addStatement("dslBuilder.apply(block)")
            .addStatement(
                "return %T(%L)",
                targetClass,
                properties.joinToString(", ") { "dslBuilder.${it.simpleName.asString()}" }
            )
            .build()

        return FileSpec.builder(packageName, fileName)
            .addType(typeSpecBuilder.build())
            .addFunction(dslFunction)
            .build()
    }
}
