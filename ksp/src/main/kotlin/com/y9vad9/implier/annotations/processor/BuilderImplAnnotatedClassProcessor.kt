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
import com.y9vad9.implier.codegen.BuilderFileCodeGeneration
import com.y9vad9.implier.codegen.BuilderFileCodeGeneration.generate
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
                BuilderFileCodeGeneration.Data(
                    type = annotation.type,
                    initVariantCode = (if (!(classDeclaration.isAnnotationPresent(ImmutableImpl::class))) "Immutable" else "Mutable").plus(
                        classDeclaration.simpleName.asString()
                    ),
                    classDeclaration
                ).generate().writeTo(writer)
            }
        }
    }
}