package sample

import com.y9vad9.implier.FactoryFunctionImpl
import com.y9vad9.implier.ImmutableImpl
import com.y9vad9.implier.MutableImpl

@FactoryFunctionImpl
@ImmutableImpl
@MutableImpl
interface Sample {
    val sample: String
    val number: Int
}