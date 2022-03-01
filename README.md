![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=%24version&metadataUrl=https%3A%2F%2Fmaven.y9vad9.com%2Fcom%2Fy9vad9%2Fimplier%2Fimplier%2Fmaven-metadata.xml)

# implier

Kotlin Symbol Processor library for creating **[
Mutable](https://github.com/y9vad9/implier/blob/fb5cba3c62defe23ce5773287fc9f37367d800fd/src/main/kotlin/com/y9vad9/implier/annotations.kt#L10)**
,
**[Immutable](https://github.com/y9vad9/implier/blob/fb5cba3c62defe23ce5773287fc9f37367d800fd/src/main/kotlin/com/y9vad9/implier/annotations.kt#L18)**
,
**[Builders](https://github.com/y9vad9/implier/blob/fb5cba3c62defe23ce5773287fc9f37367d800fd/src/main/kotlin/com/y9vad9/implier/annotations.kt#L35)**
, **[DSL Builders](https://github.com/y9vad9/implier/blob/1.0.1/src/main/kotlin/com/y9vad9/implier/annotations.kt#L50)**
from interfaces & abstract classes with properties.

## Examples

### Immutable & Mutable

```kotlin
@ImmutableImpl
@MutableImpl
interface Sample {
    val sample: String
}
```

Will generate next classes and functions:

```kotlin
fun Sample.toImmutable(): ImmutableSample = ImmutableSample(sample)

class ImmutableSample(
    override val sample: String
) : Sample

fun Sample.toMutable(): MutableSample = MutableSample(sample)

class MutableSample(
    override var sample: String
) : Sample
```

### Builders

```kotlin
// required for annotations above. They can be used with either ImmutableImpl or MutableImpl
@ImmutableImpl(visibility = Visibility.INTERNAL)
@DSLBuilderImpl(functionName = "foo")
@BuilderImpl
@FactoryFunctionImpl
interface Foo {
    val bar: Bar
}
```

Will generate:

```kotlin
 // generated file: FooFactory
fun Foo(bar: Bar): Foo = ImmutableFoo(bar)

// generatedfile: FooBuilder
class FooBuilder {
    fun bar(bar: Bar): FooBuilder { /* code */ }
    fun build(): Foo { /* code */ }
}

// generated file: FooBuilderScope
class FooDSLBuilderScope {
    var bar: Bar by Delegates.notNull()
}

fun foo(builder: FooDSLBuilderScope.() -> Unit): Foo { /* code */ }
```

## Implementation

For first, we need to add repository:

```kotlin
repositories {
    maven("https://maven.y9vad9.com")
}
```

And then we need to add dependency:

```kotlin
dependencies {
    implementation("com.y9vad9.implier:implier:$version") // annotations
    ksp("com.y9vad9.implier:ksp:$version") // ksp implementation of annotations
}
```
