package com.y9vad9.implier

import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ksp.writeTo
import com.y9vad9.implier.codegen.*

class ImplierProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    companion object {
        const val IMMUTABLE_IMPL = "com.y9vad9.implier.ImmutableImpl"
        const val MUTABLE_IMPL = "com.y9vad9.implier.MutableImpl"
        const val FACTORY_FUNCTION_IMPL = "com.y9vad9.implier.FactoryFunctionImpl"
        const val BUILDER_IMPL = "com.y9vad9.implier.BuilderImpl"
        const val DSL_BUILDER_IMPL = "com.y9vad9.implier.DSLBuilderImpl"
        const val DTO_IMPL = "com.y9vad9.implier.DtoImpl"

        val ALL_ANNOTATIONS = listOf(
            IMMUTABLE_IMPL,
            MUTABLE_IMPL,
            FACTORY_FUNCTION_IMPL,
            BUILDER_IMPL,
            DSL_BUILDER_IMPL,
            DTO_IMPL,
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = ALL_ANNOTATIONS
            .flatMap { resolver.getSymbolsWithAnnotation(it) }
            .distinct()
            .toList()

        val invalidSymbols = annotatedSymbols.filter { it !is KSClassDeclaration }
        for (symbol in invalidSymbols) {
            logger.error("Implier annotations can only be applied to interfaces or abstract classes", symbol)
        }

        val classDeclarations = annotatedSymbols.filterIsInstance<KSClassDeclaration>()
        val (validDeclarations, unresolvable) = classDeclarations.partition { it.validate(enableNewFeatures = true) }

        for (declaration in validDeclarations) {
            processDeclaration(declaration)
        }

        return unresolvable
    }

    private fun processDeclaration(declaration: KSClassDeclaration) {
        val isInterface = declaration.classKind == ClassKind.INTERFACE
        val isAbstractClass = declaration.classKind == ClassKind.CLASS && Modifier.ABSTRACT in declaration.modifiers

        if (!isInterface && !isAbstractClass) {
            logger.error(
                "Unable to create realization from non-abstract class: ${declaration.simpleName.asString()}",
                declaration
            )
            return
        }

        val immutableAnnotation = declaration.findAnnotation(IMMUTABLE_IMPL)
        val mutableAnnotation = declaration.findAnnotation(MUTABLE_IMPL)
        val factoryAnnotation = declaration.findAnnotation(FACTORY_FUNCTION_IMPL)
        val builderAnnotation = declaration.findAnnotation(BUILDER_IMPL)
        val dslAnnotation = declaration.findAnnotation(DSL_BUILDER_IMPL)
        val dtoAnnotation = declaration.findAnnotation(DTO_IMPL)

        val hasImmutable = immutableAnnotation != null
        val hasMutable = mutableAnnotation != null
        val hasFactory = factoryAnnotation != null
        val hasBuilder = builderAnnotation != null
        val hasDsl = dslAnnotation != null
        val hasDto = dtoAnnotation != null

        val requiresRealization = hasFactory || hasBuilder || hasDsl || hasDto
        if (requiresRealization && !hasImmutable && !hasMutable) {
            logger.error(
                "Declaration '${declaration.simpleName.asString()}' requires at least one of @ImmutableImpl or @MutableImpl to generate factory, builder, DSL, or DTO implementations",
                declaration
            )
            return
        }

        val properties = declaration.getAllProperties()
            .filter { it.isAbstract() }
            .distinctBy { it.simpleName.asString() }
            .toList()

        val isDataClassEligible = properties.isNotEmpty() && (
            isInterface || (
                declaration.primaryConstructor == null || declaration.primaryConstructor!!.parameters.isEmpty()
            )
        )

        val dependencies = Dependencies(
            aggregating = false,
            sources = listOfNotNull(declaration.containingFile).toTypedArray()
        )

        if (immutableAnnotation != null) {
            val visibility = immutableAnnotation.getVisibility()
            val fileSpec = ImmutableCodeGenerator.generate(
                declaration = declaration,
                properties = properties,
                visibility = visibility,
                isDataClass = isDataClassEligible,
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        }

        if (mutableAnnotation != null) {
            val visibility = mutableAnnotation.getVisibility()
            val fileSpec = MutableCodeGenerator.generate(
                declaration = declaration,
                properties = properties,
                visibility = visibility,
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        }

        if (factoryAnnotation != null) {
            val visibility = factoryAnnotation.getVisibility()
            val fileSpec = FactoryFunctionCodeGenerator.generate(
                declaration = declaration,
                properties = properties,
                visibility = visibility,
                preferImmutable = hasImmutable,
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        }

        if (builderAnnotation != null) {
            val visibility = builderAnnotation.getVisibility()
            val builderType = builderAnnotation.getBuilderType()
            val fileSpec = BuilderCodeGenerator.generate(
                declaration = declaration,
                properties = properties,
                visibility = visibility,
                builderType = builderType,
                preferImmutable = hasImmutable,
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        }

        if (dslAnnotation != null) {
            val visibility = dslAnnotation.getVisibility()
            val dslType = dslAnnotation.getDSLBuilderType()
            val functionName = dslAnnotation.getDSLFunctionName()
            if (functionName.isNullOrBlank()) {
                logger.error(
                    "DSLBuilderImpl on '${declaration.simpleName.asString()}' must specify a non-empty functionName",
                    declaration
                )
                return
            }
            val fileSpec = DSLCodeGenerator.generate(
                declaration = declaration,
                properties = properties,
                visibility = visibility,
                dslType = dslType,
                functionName = functionName,
                preferImmutable = hasImmutable,
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        }

        if (dtoAnnotation != null) {
            val visibility = dtoAnnotation.getVisibility()
            val fileSpec = DtoCodeGenerator.generate(
                declaration = declaration,
                properties = properties,
                visibility = visibility,
                preferImmutable = hasImmutable,
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        }
    }
}
