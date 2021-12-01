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