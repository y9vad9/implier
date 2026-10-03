package sample

import com.y9vad9.implier.*

@ImmutableImpl(Visibility.INTERNAL)
@MutableImpl(Visibility.INTERNAL)
@FactoryFunctionImpl(Visibility.INTERNAL)
@BuilderImpl(type = BuilderImpl.Type.WITH_ACCESSORS, visibility = Visibility.INTERNAL)
@DSLBuilderImpl("abstractSampleDSL", type = DSLBuilderImpl.Type.PROPERTY_ACCESS, visibility = Visibility.INTERNAL)
@DtoImpl(Visibility.INTERNAL)
abstract class AbstractSample {
    abstract val title: String
    abstract val count: Int

    val display: String get() = "$title: $count"

    fun doubleCount(): Int = count * 2
}
