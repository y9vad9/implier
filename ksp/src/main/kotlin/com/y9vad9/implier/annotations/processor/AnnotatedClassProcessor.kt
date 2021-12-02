package com.y9vad9.implier.annotations.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration

interface AnnotatedClassProcessor<T : Annotation> {
    fun process(codeGenerator: CodeGenerator, classDeclaration: KSClassDeclaration)
}