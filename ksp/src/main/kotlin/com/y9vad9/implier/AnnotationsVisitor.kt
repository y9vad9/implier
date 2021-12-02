package com.y9vad9.implier

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.y9vad9.implier.annotations.processor.FactoryFunctionAnnotatedClassProcessor
import com.y9vad9.implier.annotations.processor.ImmutableAnnotatedClassProcessor
import com.y9vad9.implier.annotations.processor.MutableAnnotatedClassProcessor

class AnnotationsVisitor(private val codeGenerator: CodeGenerator) : KSVisitorVoid() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitAnnotated(classDeclaration, data)
        if (classDeclaration.classKind != ClassKind.INTERFACE)
            throw IllegalStateException("Annotated element should be interface")
        if (classDeclaration.isAnnotationPresent(MutableImpl::class)) {
            MutableAnnotatedClassProcessor.process(codeGenerator, classDeclaration)
        }
        if (classDeclaration.isAnnotationPresent(ImmutableImpl::class)) {
            ImmutableAnnotatedClassProcessor.process(codeGenerator, classDeclaration)
        }
        if(classDeclaration.isAnnotationPresent(FactoryFunctionImpl::class)) {
            FactoryFunctionAnnotatedClassProcessor.process(codeGenerator, classDeclaration)
        }
    }
}