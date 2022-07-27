package sample

import com.y9vad9.implier.*

@FactoryFunctionImpl(Visibility.INTERNAL)
@ImmutableImpl(Visibility.INTERNAL)
@DtoImpl(Visibility.INTERNAL)
@MutableImpl(Visibility.INTERNAL)
@BuilderImpl(visibility = Visibility.INTERNAL)
@DSLBuilderImpl("sampleDSL", type = DSLBuilderImpl.Type.WITH_ACCESSORS, visibility = Visibility.INTERNAL)
interface Sample {
    val sample: String
    val number: Int
}