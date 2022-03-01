package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.y9vad9.implier.Visibility

object ImplementationFileCodeGeneration :
    FileCodeGeneration<ImplementationFileCodeGeneration.Data> {
    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.classBuilder(name)
                    .addModifiers(if(visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .applyParent()
                    .applyConstructor()
                    .build()
            ).addFunction(
                FunSpec.builder("to".plus(if (mutable) "Immutable" else "Mutable"))
                    .addModifiers(if(visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .receiver(ClassName(declaration.packageName.asString(), declaration.simpleName.asString()))
                    .returns(ClassName(declaration.packageName.asString(), name))
                    .addCode("return $name(${formatConstructorArguments()})")
                    .build()
            )
            .build()
    }

    @OptIn(KotlinPoetKspPreview::class)
    class Data(
        val mutable: Boolean,
        val visibility: Visibility,
        declaration: KSClassDeclaration
    ) : FileCodeGeneration.Data(declaration) {
        val name: String get() = (if (mutable) "Mutable" else "Immutable") + simpleName

        fun TypeSpec.Builder.applyParent(): TypeSpec.Builder = when (classKind) {
            ClassKind.INTERFACE -> addSuperinterface(declaration.toClassName())
            ClassKind.CLASS -> superclass(declaration.toClassName())
            else -> error("Invalid type")
        }

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
                            .mutable(mutable)
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