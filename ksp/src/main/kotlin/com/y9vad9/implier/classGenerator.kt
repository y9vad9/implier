package com.y9vad9.implier

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*

internal fun generateVariant(prefix: String, mutable: Boolean, declaration: KSClassDeclaration): FileSpec {
    val name = "$prefix${declaration.simpleName.asString()}"
    val file = FileSpec.builder(declaration.packageName.asString(), name)
    val classDeclaration = TypeSpec.classBuilder(name)
        .addSuperinterface(ClassName(declaration.packageName.asString(), declaration.simpleName.asString()))
    val constructor = FunSpec.constructorBuilder()
    for (member in declaration.getAllProperties()) {
        val memberType = member.type.resolve()
        val type = ClassName(
            memberType.declaration.packageName.asString(),
            listOf(memberType.declaration.simpleName.asString())
        )
        constructor.addParameter(
            ParameterSpec.builder(member.simpleName.asString(), type)
                .build()
        )
        classDeclaration.addProperty(
            PropertySpec.builder(member.simpleName.asString(), type)
                .addModifiers(KModifier.OVERRIDE)
                .mutable(mutable)
                .initializer(member.simpleName.asString())
                .build()
        )
    }
    file.addFunction(
        FunSpec.builder("to$prefix")
            .receiver(ClassName(declaration.packageName.asString(), declaration.simpleName.asString()))
            .returns(ClassName(declaration.packageName.asString(), name))
            .addCode("return $name(${constructor.parameters.joinToString(",") { it.name }})")
            .build()
    )
    classDeclaration.primaryConstructor(constructor.build())
    file.addType(classDeclaration.build())
    return file.build()
}