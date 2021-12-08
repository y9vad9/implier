package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.y9vad9.implier.MutableImpl
import com.y9vad9.implier.codegen.ImplementationFileCodeGeneration
import com.y9vad9.implier.codegen.ImplementationFileCodeGeneration.generate
import java.io.OutputStreamWriter

object MutableAnnotatedClassProcessor : AnnotatedClassProcessor<MutableImpl> {
    override fun process(annotation: MutableImpl, codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration) {
        if (classDeclaration.classKind == ClassKind.CLASS && Modifier.ABSTRACT !in classDeclaration.modifiers)
            throw IllegalStateException("Unable to create realization from non-abstract class")
        else if (classDeclaration.classKind != ClassKind.INTERFACE)
            throw IllegalStateException("Unable to create realization from ${classDeclaration.classKind}.")

        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            "Mutable${classDeclaration.simpleName.asString()}"
        ).use { output ->
            OutputStreamWriter(output).use { writer ->
                ImplementationFileCodeGeneration.Data(true, classDeclaration).generate()
                    .writeTo(writer)
            }
        }
    }
}