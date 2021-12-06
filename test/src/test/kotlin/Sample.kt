package sample

import com.y9vad9.implier.*

@FactoryFunctionImpl
@ImmutableImpl
@MutableImpl
@BuilderImpl
@DSLImpl("sampleDSL", type = DSLImpl.Type.WITH_ACCESSORS)
interface Sample {
    val sample: String
    val number: Int
}