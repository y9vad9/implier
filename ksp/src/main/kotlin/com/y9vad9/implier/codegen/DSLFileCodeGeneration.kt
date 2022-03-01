package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.y9vad9.implier.DSLBuilderImpl
import com.y9vad9.implier.Visibility
import java.util.*

object DSLFileCodeGeneration : FileCodeGeneration<DSLFileCodeGeneration.Data> {
    class Data(
        private val type: DSLBuilderImpl.Type,
        val initVariantCode: String,
        val visibility: Visibility,
        val functionName: String,
        declaration: KSClassDeclaration
    ) :
        FileCodeGeneration.Data(declaration) {
        val name: String get() = simpleName.plus("DSLBuilderScope")

        @OptIn(KotlinPoetKspPreview::class)
        fun TypeSpec.Builder.applyMembers(): TypeSpec.Builder {
            for (member in declaration.getAllProperties())
                when (type) {
                    DSLBuilderImpl.Type.PROPERTY_ACCESS -> addProperty(
                        PropertySpec.builder(member.simpleName.asString(), member.type.resolve().toClassName())
                            .mutable(true)
                            .delegate("kotlin.properties.Delegates.notNull()")
                            .build()
                    )
                    else -> {
                        val resolvedMember = member.type.resolve()
                        addProperty(
                            PropertySpec.builder(member.simpleName.asString(), resolvedMember.toClassName())
                                .mutable(true)
                                .delegate("kotlin.properties.Delegates.notNull()")
                                .addModifiers(KModifier.PUBLIC)
                                .build()
                        )
                        addFunction(
                            FunSpec.builder(
                                if (type == DSLBuilderImpl.Type.WITHOUT_ACCESSORS) member.simpleName.asString() else "set${
                                    member.simpleName.asString()
                                        .replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                            else it.toString()
                                        }
                                }"
                            ).addParameter(
                                "value",
                                resolvedMember.toClassName()
                            )
                                .returns(ClassName(packageName, name))
                                .addCode("${member.simpleName.asString()} = value\n")
                                .addCode("return this")
                                .build()
                        )
                    }
                }
            return this
        }

        @OptIn(KotlinPoetKspPreview::class)
        fun FileSpec.Builder.addDSLFunction(): FileSpec.Builder {
            addFunction(FunSpec.builder(functionName)
                .addModifiers(if(visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                .addParameter(
                    "block",
                    LambdaTypeName.get(
                        receiver = ClassName(packageName, name), returnType = Unit::class.asTypeName()
                    )
                ).addCode(
                    """
                val dslBuilder = $name()
                dslBuilder.apply(block)
                return $initVariantCode(${
                        declaration.getAllProperties()
                            .joinToString(",") { "dslBuilder." + it.simpleName.asString() }
                    })
            """.trimIndent()
                )
                .returns(
                    declaration.toClassName()
                )
                .build()
            ).build()
            return this
        }
    }

    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, name)
            .addType(
                TypeSpec.classBuilder(name)
                    .addModifiers(if (visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .applyMembers()
                    .build()
            )
            .addDSLFunction()
            .build()
    }
}