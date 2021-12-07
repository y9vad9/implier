package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName

object ImplementationFileCodeGeneration :
    FileCodeGeneration<ImplementationFileCodeGeneration.Data> {
    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.classBuilder(name)
                    .applyParent()
                    .applyConstructor()
                    .build()
            ).addFunction(
                FunSpec.builder("to".plus(if (mutable) "Immutable" else "Mutable"))
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