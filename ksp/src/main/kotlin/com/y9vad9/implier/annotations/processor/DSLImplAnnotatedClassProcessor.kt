package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.y9vad9.implier.DSLImpl
import com.y9vad9.implier.ImmutableImpl
import com.y9vad9.implier.MutableImpl
import java.io.OutputStreamWriter
import java.util.*

object DSLImplAnnotatedClassProcessor : AnnotatedClassProcessor<DSLImpl> {
    @OptIn(KspExperimental::class)
    override fun process(annotation: DSLImpl, codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration) {
        if (!(classDeclaration.isAnnotationPresent(ImmutableImpl::class)
                && classDeclaration.isAnnotationPresent(MutableImpl::class))
        )
            throw IllegalStateException("Unable to create builder implementation without Immutable / Mutable implementations.")
        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            classDeclaration.simpleName.asString().plus("BuilderScope")
        ).use { output ->
            OutputStreamWriter(output).use { writer ->
                generateDSLImplementation(
                    type = annotation.type,
                    initVariantCode = (if (!(classDeclaration.isAnnotationPresent(ImmutableImpl::class))) "Immutable" else "Mutable").plus(
                        classDeclaration.simpleName.asString()
                    ),
                    functionName = annotation.functionName,
                    declaration = classDeclaration
                ).writeTo(writer)
            }
        }
    }
}

private fun generateDSLImplementation(
    type: DSLImpl.Type,
    functionName: String,
    initVariantCode: String,
    declaration: KSClassDeclaration
): FileSpec {
    val name = declaration.simpleName.asString().plus("BuilderScope")
    val file = FileSpec.builder(
        declaration.packageName.asString(),
        name
    )
    val builderClassName = ClassName(declaration.packageName.asString(), name)
    val builderClass = TypeSpec.classBuilder(file.name)
    for (member in declaration.getAllProperties()) {
        val resolvedMember = member.type.resolve()
        val memberType = ClassName(
            resolvedMember.declaration.packageName.asString(),
            resolvedMember.declaration.simpleName.asString()
        )
        when (type) {
            DSLImpl.Type.PROPERTY_ACCESS -> builderClass.addProperty(
                PropertySpec.builder(member.simpleName.asString(), memberType)
                    .mutable(true)
                    .delegate("kotlin.properties.Delegates.notNull()")
                    .build()
            )
            else -> {
                builderClass.addProperty(
                    PropertySpec.builder(member.simpleName.asString(), memberType)
                        .mutable(true)
                        .delegate("kotlin.properties.Delegates.notNull()")
                        .addModifiers(KModifier.PUBLIC)
                        .build()
                )
                builderClass.addFunction(
                    FunSpec.builder(
                        if (type == DSLImpl.Type.WITHOUT_ACCESSORS) member.simpleName.asString() else "set${
                            member.simpleName.asString()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        }"
                    ).addParameter(
                        "value",
                        ClassName(
                            resolvedMember.declaration.packageName.asString(),
                            resolvedMember.declaration.simpleName.asString()
                        )
                    )
                        .returns(builderClassName)
                        .addCode("${member.simpleName.asString()} = value\n")
                        .addCode("return this")
                        .build()
                )
            }
        }
    }
    file.addFunction(
        FunSpec.builder(functionName)
            .addParameter(
                "block",
                LambdaTypeName.get(receiver = builderClassName, returnType = Unit::class.asTypeName())
            )
            .addCode(
                """
                val dslBuilder = $name()
                dslBuilder.apply(block)
                return $initVariantCode(${
                    declaration.getAllProperties().joinToString(",") { "dslBuilder." + it.simpleName.asString() }
                })
            """.trimIndent()
            )
            .returns(
                ClassName(
                    declaration.packageName.asString(),
                    declaration.simpleName.asString()
                )
            )
            .build()
    )
    file.addType(builderClass.build())
    return file.build()
}