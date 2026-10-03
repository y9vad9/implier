package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.y9vad9.implier.Dto
import com.y9vad9.implier.Visibility

object DtoCodeGenerator {
    fun generate(
        declaration: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>,
        visibility: Visibility,
        preferImmutable: Boolean,
    ): FileSpec {
        val packageName = declaration.packageName.asString()
        val simpleName = declaration.simpleName.asString()
        val className = "Dto$simpleName"
        val visibilityModifier = if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL
        val dtoClass = ClassName(packageName, className)

        val targetRealizationName = if (preferImmutable) "Immutable$simpleName" else "Mutable$simpleName"
        val targetClass = ClassName(packageName, targetRealizationName)

        val typeSpecBuilder = TypeSpec.classBuilder(className)
            .addModifiers(visibilityModifier)
            .addSuperinterface(Dto::class.asClassName().parameterizedBy(declaration.toClassName()))

        val constructorBuilder = FunSpec.constructorBuilder()
        for (property in properties) {
            val propName = property.simpleName.asString()
            val nullablePropType = property.type.resolve().toTypeName().copy(nullable = true)
            constructorBuilder.addParameter(
                ParameterSpec.builder(propName, nullablePropType)
                    .defaultValue("null")
                    .build()
            )
            typeSpecBuilder.addProperty(
                PropertySpec.builder(propName, nullablePropType)
                    .mutable(true)
                    .initializer(propName)
                    .build()
            )
        }
        typeSpecBuilder.primaryConstructor(constructorBuilder.build())

        val toDtoFunction = FunSpec.builder("toDto")
            .addModifiers(visibilityModifier)
            .receiver(declaration.toClassName())
            .returns(dtoClass)
            .addStatement(
                "return %T(%L)",
                dtoClass,
                properties.joinToString(", ") { "${it.simpleName.asString()} = this.${it.simpleName.asString()}" }
            )
            .build()

        val toPatchedFunction = FunSpec.builder("toPatched")
            .addModifiers(visibilityModifier)
            .receiver(declaration.toClassName())
            .addParameter("patch", dtoClass)
            .returns(declaration.toClassName())
            .addStatement(
                "return %T(%L)",
                targetClass,
                properties.joinToString(", ") {
                    val name = it.simpleName.asString()
                    "$name = patch.$name ?: this.$name"
                }
            )
            .build()

        return FileSpec.builder(packageName, className)
            .addType(typeSpecBuilder.build())
            .addFunction(toDtoFunction)
            .addFunction(toPatchedFunction)
            .build()
    }
}
