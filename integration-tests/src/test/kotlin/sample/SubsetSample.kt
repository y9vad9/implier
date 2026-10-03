package sample

import com.y9vad9.implier.*

/**
 * Interface with only @ImmutableImpl and @BuilderImpl.
 */
@ImmutableImpl(Visibility.INTERNAL)
@BuilderImpl(visibility = Visibility.INTERNAL)
interface ImmutableOnlySample {
    val id: Long
    val name: String
}

/**
 * Interface with only @MutableImpl, @BuilderImpl, @DSLBuilderImpl, @FactoryFunctionImpl, and @DtoImpl.
 */
@MutableImpl(Visibility.INTERNAL)
@BuilderImpl(visibility = Visibility.INTERNAL)
@DSLBuilderImpl("mutableOnlyDSL", visibility = Visibility.INTERNAL)
@FactoryFunctionImpl(Visibility.INTERNAL)
@DtoImpl(Visibility.INTERNAL)
interface MutableOnlySample {
    val score: Double
    val flag: Boolean
}
