![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=%24version&metadataUrl=https%3A%2F%2Fmaven.y9vad9.com%2Fcom%2Fy9vad9%2Fimplier%2Fimplier%2Fmaven-metadata.xml)

# implier

Kotlin Symbol Processor library for creating [**
Mutable**](https://github.com/y9vad9/implier/blob/fb5cba3c62defe23ce5773287fc9f37367d800fd/src/main/kotlin/com/y9vad9/implier/annotations.kt#L10)
, [**
Immutable**](https://github.com/y9vad9/implier/blob/fb5cba3c62defe23ce5773287fc9f37367d800fd/src/main/kotlin/com/y9vad9/implier/annotations.kt#L18)
, [**
Builders**](https://github.com/y9vad9/implier/blob/fb5cba3c62defe23ce5773287fc9f37367d800fd/src/main/kotlin/com/y9vad9/implier/annotations.kt#L35)
, [**DSL Builders**](https://github.com/y9vad9/implier/blob/1.0.1/src/main/kotlin/com/y9vad9/implier/annotations.kt#L50)
from interfaces & abstract classes with properties.

## Examples

```kotlin
@ImmutableImpl
@MutableImpl
public interface Sample {
    val sample: String
}
```

Will generate next classes and functions:

```kotlin
fun Sample.toImmutable(): ImmutableSample = ImmutableSample(sample)

class ImmutableSample(
    public override val sample: String
) : Sample

fun Sample.toMutable(): MutableSample = MutableSample(sample)

class MutableSample(
    public override var sample: String
) : Sample
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
