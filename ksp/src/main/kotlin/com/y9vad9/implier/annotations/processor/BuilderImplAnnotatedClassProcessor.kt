package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.y9vad9.implier.BuilderImpl
import com.y9vad9.implier.ImmutableImpl
import com.y9vad9.implier.MutableImpl
import java.io.OutputStreamWriter
import java.util.*

object BuilderImplAnnotatedClassProcessor : AnnotatedClassProcessor<BuilderImpl> {
    @OptIn(KspExperimental::class)
    override fun process(annotation: BuilderImpl, codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration) {
        if (!(classDeclaration.isAnnotationPresent(ImmutableImpl::class)
                && classDeclaration.isAnnotationPresent(MutableImpl::class))
        )
            throw IllegalStateException("Unable to create builder implementation without Immutable / Mutable implementations.")
        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            classDeclaration.simpleName.asString().plus("Builder")
        ).use { output ->
            OutputStreamWriter(output).use { writer ->
                generateBuilderImplementation(
                    type = annotation.type,
                    initVariantCode = (if (!(classDeclaration.isAnnotationPresent(ImmutableImpl::class))) "Immutable" else "Mutable").plus(
                        classDeclaration.simpleName.asString()
                    ),
                    classDeclaration
                ).writeTo(writer)
            }
        }
    }
}

private fun generateBuilderImplementation(
    type: BuilderImpl.Type,
    initVariantCode: String,
    declaration: KSClassDeclaration
): FileSpec {
    val file = FileSpec.builder(
        declaration.packageName.asString(),
        declaration.simpleName.asString().plus("Builder")
    )
    val builderClassName =
        ClassName(declaration.packageName.asString(), declaration.simpleName.asString().plus("Builder"))
    val builderClass = TypeSpec.classBuilder(file.name)
    for (member in declaration.getAllProperties()) {
        val resolvedMember = member.type.resolve()
        val memberType = ClassName(
            resolvedMember.declaration.packageName.asString(),
            resolvedMember.declaration.simpleName.asString()
        )
        builderClass.addProperty(
            PropertySpec.builder(member.simpleName.asString(), memberType)
                .mutable(true)
                .delegate("kotlin.properties.Delegates.notNull()")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        builderClass.addFunction(
            FunSpec.builder(
                if (type == BuilderImpl.Type.WITHOUT_ACCESSORS) member.simpleName.asString() else "set${
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
    builderClass.addFunction(
        FunSpec.builder("build")
            .addCode(
                "return $initVariantCode(${
                    declaration.getAllProperties().joinToString(",") { it.simpleName.asString() }
                })"
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