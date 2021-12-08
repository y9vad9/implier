package sample

import com.y9vad9.implier.*

@FactoryFunctionImpl
@ImmutableImpl
@MutableImpl
@BuilderImpl
@DSLBuilderImpl("sampleDSL", type = DSLBuilderImpl.Type.WITH_ACCESSORS)
interface Sample {
    val sample: String
    val number: Int
}