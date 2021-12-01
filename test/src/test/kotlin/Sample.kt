package sample

import com.y9vad9.implier.ImmutableImpl
import com.y9vad9.implier.MutableImpl

@ImmutableImpl
@MutableImpl
interface Sample {
    val sample: String
}