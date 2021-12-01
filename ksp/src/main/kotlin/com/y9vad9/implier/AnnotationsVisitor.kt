package com.y9vad9.implier

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import java.io.OutputStreamWriter

class AnnotationsVisitor(private val codeGenerator: CodeGenerator) : KSVisitorVoid() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitAnnotated(classDeclaration, data)
        if (classDeclaration.isAnnotationPresent(MutableImpl::class)) {
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
        if (classDeclaration.isAnnotationPresent(ImmutableImpl::class)) {
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
}