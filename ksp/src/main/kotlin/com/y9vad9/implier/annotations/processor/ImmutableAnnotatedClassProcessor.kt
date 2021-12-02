package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.y9vad9.implier.ImmutableImpl
import com.y9vad9.implier.generateVariant
import java.io.OutputStreamWriter

object ImmutableAnnotatedClassProcessor : AnnotatedClassProcessor<ImmutableImpl> {
    override fun process(codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration) {
        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            "Immutable${classDeclaration.simpleName.asString()}"
        ).use { output ->
            OutputStreamWriter(output).use { writer ->
                generateVariant("Immutable", false, classDeclaration).writeTo(writer)
            }
        }
    }
}