package com.y9vad9.implier

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.y9vad9.implier.annotations.processor.*

class AnnotationsVisitor(private val codeGenerator: CodeGenerator) : KSVisitorVoid() {
    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitAnnotated(classDeclaration, data)
        if (classDeclaration.isAnnotationPresent(MutableImpl::class)) {
            MutableAnnotatedClassProcessor.process(
                classDeclaration.getAnnotationsByType(MutableImpl::class).first(),
                codeGenerator,
                classDeclaration
            )
        }
        if (classDeclaration.isAnnotationPresent(ImmutableImpl::class)) {
            ImmutableAnnotatedClassProcessor.process(
                classDeclaration.getAnnotationsByType(ImmutableImpl::class).first(), codeGenerator, classDeclaration
            )
        }
        if (classDeclaration.isAnnotationPresent(FactoryFunctionImpl::class)) {
            FactoryFunctionAnnotatedClassProcessor.process(
                classDeclaration.getAnnotationsByType(FactoryFunctionImpl::class).first(),
                codeGenerator,
                classDeclaration
            )
        }
        if (classDeclaration.isAnnotationPresent(BuilderImpl::class)) {
            BuilderImplAnnotatedClassProcessor.process(
                classDeclaration.getAnnotationsByType(BuilderImpl::class).first(),
                codeGenerator,
                classDeclaration
            )
        }
        if (classDeclaration.isAnnotationPresent(DSLBuilderImpl::class)) {
            DSLImplAnnotatedClassProcessor.process(
                classDeclaration.getAnnotationsByType(DSLBuilderImpl::class).first(),
                codeGenerator,
                classDeclaration
            )
        }
    }
}