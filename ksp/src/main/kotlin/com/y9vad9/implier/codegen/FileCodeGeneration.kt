package com.y9vad9.implier.codegen

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec

interface FileCodeGeneration<TData : FileCodeGeneration.Data> {
    fun TData.generate(): FileSpec

    abstract class Data(
        val declaration: KSClassDeclaration
    ) {
        val simpleName: String get() = declaration.simpleName.asString()
        val packageName: String get() = declaration.packageName.asString()
        val classKind: ClassKind get() = declaration.classKind

        val className: ClassName = ClassName(packageName, simpleName)
    }
}