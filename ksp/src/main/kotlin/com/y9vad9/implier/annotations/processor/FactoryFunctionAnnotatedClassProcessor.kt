package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.y9vad9.implier.FactoryFunctionImpl
import com.y9vad9.implier.ImmutableImpl
import com.y9vad9.implier.MutableImpl
import com.y9vad9.implier.codegen.FunctionFactoryFileCodeGeneration
import com.y9vad9.implier.codegen.FunctionFactoryFileCodeGeneration.generate
import java.io.OutputStreamWriter

object FactoryFunctionAnnotatedClassProcessor : AnnotatedClassProcessor<FactoryFunctionImpl> {
    @OptIn(KspExperimental::class)
    override fun process(
        annotation: FactoryFunctionImpl,
        codeGenerator: CodeGenerator,
        classDeclaration: KSClassDeclaration
    ) {
        val variantName = if (classDeclaration.isAnnotationPresent(ImmutableImpl::class))
            "Immutable"
        else if (classDeclaration.isAnnotationPresent(MutableImpl::class))
            "Mutable"
        else throw IllegalStateException(
            "Unable to create factory function for interface / class that does not have Mutable or Immutable realization"
        )
        if (classDeclaration.isAnnotationPresent(ImmutableImpl::class)) {
            codeGenerator.createNewFile(
                Dependencies(false),
                classDeclaration.packageName.asString(),
                classDeclaration.simpleName.asString().plus("Factory")
            ).use { output ->
                OutputStreamWriter(output).use { writer ->
                    FunctionFactoryFileCodeGeneration.Data(variantName, classDeclaration)
                        .generate()
                        .writeTo(writer)
                }
            }
        }
    }
}