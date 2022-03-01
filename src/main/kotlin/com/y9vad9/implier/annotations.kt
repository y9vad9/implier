package com.y9vad9.implier


/**
 * Marks that object should be able to mutate.
 * Generates `toMutable()` function for creating mutable variant of object and
 * mutable variant of annotated interface.
 * @param visibility - Visibility of generated class & function.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class MutableImpl(val visibility: Visibility = Visibility.PUBLIC)

/**
 * Marks that object should be able to immutate.
 * Generates `toImmutable()` function for creating immutable variant of object and
 * mutable variant of annotated interface.
 * @param visibility - Visibility of generated class & function.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class ImmutableImpl(val visibility: Visibility = Visibility.PUBLIC)

/**
 * Marks that object should have factory-function with hidden realization.
 * To make it works, you should use at least one of mutable / immutable annotations.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class FactoryFunctionImpl(val visibility: Visibility = Visibility.PUBLIC)

/**
 * Marks that object should have Builder implementation.
 * To make it works, you should use at least one of mutable / immutable
 * annotations (type will be hidden behind interface / abstract class). Immutable preferred.
 *
 * @param type - methods generation type (with accessors set, or just `property(value)`)
 * @param visibility - Visibility of generated class.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class BuilderImpl(val type: Type = Type.WITHOUT_ACCESSORS, val visibility: Visibility = Visibility.PUBLIC) {
    enum class Type {
        WITH_ACCESSORS, WITHOUT_ACCESSORS
    }
}

/**
 * Marks that object should have DSL Builder implementation.
 * To make it works, you should use at least one of mutable / immutable
 * annotations (type will be hidden behind `interface` / `abstract class`). Immutable preferred.
 *
 * @param functionName - DSL function name (e.x: `myConfiguration {}`)
 * @param type - methods generation type (with accessors set / `property(value) or just property-access`)
 * @param visibility - Visibility of generated class & function.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class DSLBuilderImpl(
    val functionName: String,
    val type: Type = Type.PROPERTY_ACCESS,
    val visibility: Visibility = Visibility.PUBLIC
) {
    enum class Type {
        PROPERTY_ACCESS, WITH_ACCESSORS, WITHOUT_ACCESSORS
    }
}