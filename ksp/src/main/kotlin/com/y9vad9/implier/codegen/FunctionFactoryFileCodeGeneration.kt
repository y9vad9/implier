package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.y9vad9.implier.Visibility
import java.util.*

object FunctionFactoryFileCodeGeneration : FileCodeGeneration<FunctionFactoryFileCodeGeneration.Data> {
    class Data(val realizationName: String, val visibility: Visibility, declaration: KSClassDeclaration) :
        FileCodeGeneration.Data(declaration) {
        val name: String get() = simpleName + "Factory"

        fun formatConstructorArguments(): String {
            return declaration.getAllProperties().joinToString(", ") { it.simpleName.asString() }
        }

        fun generateParameters(): List<ParameterSpec> = declaration.getAllProperties().map {
            ParameterSpec.builder(it.simpleName.asString(), it.type.resolve().toClassName()).build()
        }.toList()
    }

    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, name)
            .addFunction(
                FunSpec.builder(simpleName)
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .returns(declaration.toClassName())
                    .addParameters(generateParameters())
                    .addCode(
                        "return $realizationName${
                            simpleName.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            }
                        }(${formatConstructorArguments()})"
                    )
                    .build()
            )
            .build()
    }
}