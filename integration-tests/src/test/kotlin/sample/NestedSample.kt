package sample

import com.y9vad9.implier.*

interface Container {
    @ImmutableImpl(Visibility.INTERNAL)
    @MutableImpl(Visibility.INTERNAL)
    @FactoryFunctionImpl(Visibility.INTERNAL)
    interface NestedSample {
        val key: String
        val value: String
    }
}
