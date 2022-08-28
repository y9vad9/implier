package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.y9vad9.implier.Visibility
import kotlin.reflect.KClass

object DtoFileCodeGeneration :
    FileCodeGeneration<DtoFileCodeGeneration.Data> {
    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.classBuilder(name)
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .applyParent()
                    .applyConstructor()
                    .build()
            ).addFunction(
                FunSpec.builder("toDto")
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .receiver(ClassName(declaration.packageName.asString(), declaration.simpleName.asString()))
                    .returns(ClassName(declaration.packageName.asString(), name))
                    .addCode("return $name(${formatConstructorArguments()})")
                    .build()
            ).addFunction(
                FunSpec.builder("toPatched")
                    .addParameter("patch", className)
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .receiver(ClassName(declaration.packageName.asString(), declaration.simpleName.asString()))
                    .returns(className)
                    .addCode(CodeBlock.builder()
                        .addStatement("val isMutable = this is Mutable${declaration.simpleName.asString()}")
                        .addStatement("val base = if(isMutable) this as Mutable${declaration.simpleName.asString()} else this.toMutable()")
                        .also { builder ->
                            for (property in declaration.getAllProperties()) {
                                builder.addStatement("patch.${property.simpleName.asString()}?.let{ ")
                                    .indent().addStatement("base.${property.simpleName.asString()} = it").unindent()
                                    .addStatement("}")
                            }
                        }
                        .addStatement("return if(isMutable) this else base.toImmutable()")
                        .build())
                    .build()
            )
            .build()
    }

    class Data(
        val marker: KClass<*>,
        val visibility: Visibility,
        declaration: KSClassDeclaration
    ) : FileCodeGeneration.Data(declaration) {
        val name: String get() = marker.simpleName + simpleName

        fun TypeSpec.Builder.applyParent(): TypeSpec.Builder =
            addSuperinterface(marker.asClassName().parameterizedBy(declaration.toClassName()))

        fun TypeSpec.Builder.applyConstructor(): TypeSpec.Builder {
            return primaryConstructor(FunSpec.constructorBuilder().apply {
                for (property in declaration.getAllProperties()) {
                    val propertyName = property.simpleName.asString()
                    val propertyType = property.type.resolve().toClassName().copy(nullable = true)
                    addParameter(
                        ParameterSpec.builder(propertyName, propertyType).defaultValue("null").build()
                    )
                    addProperty(
                        PropertySpec.builder(propertyName, propertyType)
                            .mutable(true)
                            .initializer(propertyName)
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
