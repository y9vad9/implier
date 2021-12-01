package com.y9vad9.implier

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration

class ImplierAnnotationProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private var invoked = false

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotations: Sequence<KSDeclaration> =
            resolver.getAllFiles().flatMap { it.declarations }.filter {
                it.isAnnotationPresent(MutableImpl::class) || it.isAnnotationPresent(ImmutableImpl::class)
            }

        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = AnnotationsVisitor(codeGenerator)

        annotations.forEach {
            it.accept(visitor, Unit)
        }

        return listOf()
    }
}