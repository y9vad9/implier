package com.y9vad9.implier


/**
 * Marks that object should be able to mutate.
 * Generates `toMutable()` function for creating mutable variant of object and
 * mutable variant of annotated interface.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class MutableImpl

/**
 * Marks that object should be able to immutate.
 * Generates `toImmutable()` function for creating immutable variant of object and
 * mutable variant of annotated interface.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class ImmutableImpl

/**
 * Marks that object should have factory-function with hidden realization.
 * To make it works, you should use at least one of mutable / immutable annotations.
 */
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class FactoryFunctionImpl