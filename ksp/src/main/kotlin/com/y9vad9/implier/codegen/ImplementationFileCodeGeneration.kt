package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.y9vad9.implier.Dto
import com.y9vad9.implier.Mutable
import com.y9vad9.implier.Visibility
import kotlin.reflect.KClass

object ImplementationFileCodeGeneration :
    FileCodeGeneration<ImplementationFileCodeGeneration.Data> {
    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.classBuilder(name)
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .applyParent()
                    .applyConstructor()
                    .build()
            ).addFunction(
                FunSpec.builder("to${marker.simpleName}")
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .receiver(ClassName(declaration.packageName.asString(), declaration.simpleName.asString()))
                    .returns(ClassName(declaration.packageName.asString(), name))
                    .addCode("return $name(${formatConstructorArguments()})")
                    .build()
            )
            .build()
    }

    class Data(
        val marker: KClass<*>,
        val visibility: Visibility,
        declaration: KSClassDeclaration
    ) : FileCodeGeneration.Data(declaration) {
        companion object {
            private val mutableMarkers = listOf(Mutable::class, Dto::class).map { it.qualifiedName }
            private val nullableMarkers = listOf(Dto::class).map { it.qualifiedName }
        }

        val name: String get() = marker.simpleName + simpleName

        fun TypeSpec.Builder.applyParent(): TypeSpec.Builder = when (classKind) {
            ClassKind.INTERFACE -> addSuperinterface(declaration.toClassName())
            ClassKind.CLASS -> superclass(declaration.toClassName())
            else -> error("Invalid type")
        }.addSuperinterface(marker.asClassName().parameterizedBy(declaration.toClassName()))

        fun TypeSpec.Builder.applyConstructor(): TypeSpec.Builder {
            return primaryConstructor(FunSpec.constructorBuilder().apply {
                for (property in declaration.getAllProperties()) {
                    addParameter(
                        ParameterSpec.builder(
                            property.simpleName.asString(), property.type.resolve().toClassName()
                        ).build()
                    )
                    addProperty(
                        PropertySpec.builder(property.simpleName.asString(), property.type.resolve().toClassName())
                            .initializer(property.simpleName.asString())
                            .mutable(mutableMarkers.contains(marker.qualifiedName))
                            .addModifiers(KModifier.OVERRIDE)
                            .build()
                    )
                }
            }.build())
        }

        fun formatConstructorArguments(): String {
            return declaration.getAllProperties().joinToString(", ") { it.simpleName.asString() }
        }
    }
}