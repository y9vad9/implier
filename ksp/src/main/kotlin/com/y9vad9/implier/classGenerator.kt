package com.y9vad9.implier

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*

internal fun generateVariant(prefix: String, mutable: Boolean, declaration: KSClassDeclaration): FileSpec {
    val name = "$prefix${declaration.simpleName.asString()}"
    val file = FileSpec.builder(declaration.packageName.asString(), name)
    val classDeclaration = TypeSpec.classBuilder(name)
    val className = ClassName(declaration.packageName.asString(), declaration.simpleName.asString())
    when (declaration.classKind) {
        ClassKind.CLASS -> {
            require(Modifier.ABSTRACT in declaration.modifiers) { "Annotated class should be abstract" }
            classDeclaration.superclass(className)
        }
        ClassKind.INTERFACE -> {
            classDeclaration.addSuperinterface(className)
        }
        else -> throw IllegalStateException("Unable to create $prefix variant for object. It should be abstract class / interface")
    }
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

internal fun generateFactory(variantName: String, declaration: KSClassDeclaration): FileSpec {
    val className = ClassName(declaration.packageName.asString(), declaration.simpleName.asString())
    val arguments = mutableListOf<String>()
    return FileSpec.builder(declaration.packageName.asString(), "${className}Factory")
        .addFunction(
            FunSpec.builder(declaration.simpleName.asString())
                .apply {
                    for (property in declaration.getAllProperties()) {
                        val resolved = property.type.resolve().declaration
                        addParameter(
                            property.simpleName.asString(),
                            ClassName(resolved.packageName.asString(), resolved.simpleName.asString())
                        )
                        arguments += property.simpleName.asString()
                    }
                }
                .returns(className)
                .addCode("return $variantName${className.simpleName}(${arguments.joinToString(", ")})")
                .build()
        ).build()
}