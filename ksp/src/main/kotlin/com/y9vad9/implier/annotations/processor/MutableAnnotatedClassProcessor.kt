package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.y9vad9.implier.MutableImpl
import com.y9vad9.implier.generateVariant
import java.io.OutputStreamWriter

object MutableAnnotatedClassProcessor : AnnotatedClassProcessor<MutableImpl> {
    override fun process(codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration) {
        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            "Mutable${classDeclaration.simpleName.asString()}"
        ).use { output ->
            OutputStreamWriter(output).use { writer ->
                generateVariant("Mutable", true, classDeclaration).writeTo(writer)
            }
        }
    }
}