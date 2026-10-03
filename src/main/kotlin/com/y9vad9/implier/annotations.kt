package com.y9vad9.implier

/**
 * Marks that an object should have a mutable implementation generated.
 * Generates `Mutable*` class and `toMutable()` extension function.
 * Can be applied to interfaces or abstract classes.
 *
 * @param visibility Visibility of generated class & extension function.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class MutableImpl(val visibility: Visibility = Visibility.PUBLIC)

/**
 * Marks that an object should have an immutable implementation generated.
 * Generates `Immutable*` class (data class for interfaces and no-arg abstract classes)
 * and `toImmutable()` extension function.
 * Can be applied to interfaces or abstract classes.
 *
 * @param visibility Visibility of generated class & extension function.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class ImmutableImpl(val visibility: Visibility = Visibility.PUBLIC)

/**
 * Marks that an object should have a factory function with hidden realization.
 * Requires at least one of [ImmutableImpl] or [MutableImpl] on the same declaration
 * (prefers [ImmutableImpl]).
 *
 * @param visibility Visibility of generated factory function.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class FactoryFunctionImpl(val visibility: Visibility = Visibility.PUBLIC)

/**
 * Marks that an object should have a Builder implementation generated.
 * Requires at least one of [ImmutableImpl] or [MutableImpl] on the same declaration
 * (prefers [ImmutableImpl]).
 *
 * @param type Methods generation type (with accessors `set*`, or just `property(value)`).
 * @param visibility Visibility of generated builder class and methods.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class BuilderImpl(
    val type: Type = Type.WITHOUT_ACCESSORS,
    val visibility: Visibility = Visibility.PUBLIC
) {
    enum class Type {
        WITH_ACCESSORS, WITHOUT_ACCESSORS
    }
}

/**
 * Marks that an object should have a DSL Builder implementation generated.
 * Requires at least one of [ImmutableImpl] or [MutableImpl] on the same declaration
 * (prefers [ImmutableImpl]).
 *
 * @param functionName DSL function name (e.g.: `myConfiguration {}`).
 * @param type Methods generation type (property-access, accessors `set*`, or `property(value)`).
 * @param visibility Visibility of generated builder class and DSL function.
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

/**
 * Marks that an object should be able to (partially) mutate using DTOs as patch objects.
 * Generates `toDto()` function for creating DTO variant of object with nullable members,
 * and `toPatched(patch: Dto*)` function for applying patches.
 * Requires at least one of [ImmutableImpl] or [MutableImpl] on the same declaration
 * (prefers [ImmutableImpl]).
 *
 * @param visibility Visibility of generated DTO class & functions.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class DtoImpl(val visibility: Visibility = Visibility.PUBLIC)
