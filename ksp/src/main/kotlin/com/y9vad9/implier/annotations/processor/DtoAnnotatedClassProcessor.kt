package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.y9vad9.implier.Dto
import com.y9vad9.implier.DtoImpl
import com.y9vad9.implier.codegen.DtoFileCodeGeneration
import com.y9vad9.implier.codegen.DtoFileCodeGeneration.generate
import java.io.OutputStreamWriter

object DtoAnnotatedClassProcessor : AnnotatedClassProcessor<DtoImpl> {
    override fun process(annotation: DtoImpl, codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration) {
        validateClassDeclaration(classDeclaration)

        codeGenerator.createNewFile(
            Dependencies(false),
            classDeclaration.packageName.asString(),
            "Dto${classDeclaration.simpleName.asString()}"
        ).use { output ->
            OutputStreamWriter(output).use { writer ->
                DtoFileCodeGeneration.Data(Dto::class, annotation.visibility, classDeclaration).generate()
                    .writeTo(writer)
            }
        }
    }

    private fun validateClassDeclaration(classDeclaration: KSClassDeclaration) {
        if (classDeclaration.classKind == ClassKind.CLASS && Modifier.ABSTRACT !in classDeclaration.modifiers)
            throw IllegalStateException("Unable to create realization from non-abstract class")
        else if (classDeclaration.classKind != ClassKind.INTERFACE)
            throw IllegalStateException("Unable to create realization from ${classDeclaration.classKind}.")
    }
}