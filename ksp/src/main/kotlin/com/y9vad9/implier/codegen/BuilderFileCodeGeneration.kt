package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.y9vad9.implier.BuilderImpl
import com.y9vad9.implier.Visibility
import java.util.*

object BuilderFileCodeGeneration : FileCodeGeneration<BuilderFileCodeGeneration.Data> {
    class Data(private val type: BuilderImpl.Type, val visibility: Visibility, val initVariantCode: String, declaration: KSClassDeclaration) :
        FileCodeGeneration.Data(declaration) {
        val name: String get() = simpleName.plus("Builder")

        fun TypeSpec.Builder.applyMembers(): TypeSpec.Builder {
            for (member in declaration.getAllProperties()) {
                val resolvedMember = member.type.resolve()
                addProperty(
                    PropertySpec.builder(member.simpleName.asString(), resolvedMember.toClassName())
                        .mutable(true)
                        .delegate("kotlin.properties.Delegates.notNull()")
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                )
                addFunction(
                    FunSpec.builder(
                        if (type == BuilderImpl.Type.WITHOUT_ACCESSORS) member.simpleName.asString() else "set${
                            member.simpleName.asString()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        }"
                    ).addParameter(
                        "value",
                        resolvedMember.toClassName()
                    )
                        .returns(ClassName(packageName, name))
                        .addCode("${member.simpleName.asString()} = value\n")
                        .addCode("return this")
                        .build())
            }
            return this
        }
    }

    override fun Data.generate(): FileSpec {
        return FileSpec.builder(packageName, simpleName)
            .addType(
                TypeSpec.classBuilder(name)
                    .addModifiers(if(visibility == Visibility.PUBLIC) KModifier.PUBLIC else KModifier.INTERNAL)
                    .applyMembers()
                    .addFunction(
                        FunSpec.builder("build")
                            .addCode(
                                "return $initVariantCode(${
                                    declaration.getAllProperties().joinToString(",") { it.simpleName.asString() }
                                })"
                            )
                            .returns(
                                ClassName(
                                    packageName,
                                    simpleName
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }
}