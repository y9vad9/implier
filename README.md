![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=%24version&metadataUrl=https%3A%2F%2Fmaven.y9vad9.com%2Fcom%2Fy9vad9%2Fimplier%2Fimplier%2Fmaven-metadata.xml)

# implier

Kotlin Symbol Processor plugin to create `Mutable` and `Immutable` variants of objects.

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
public fun Sample.toImmutable(): ImmutableSample = ImmutableSample(sample)

public class ImmutableSample(
    public override val sample: String
) : Sample

public fun Sample.toMutable(): MutableSample = MutableSample(sample)

public class MutableSample(
    public override var sample: String
) : Sample
```

## Implementation

For first, we need to add repository:

```kotlin
repositories {
    maven("https://maven.y9vad9.fun")
}
```

And then we need to add dependency:

```kotlin
dependencies {
    implementation("com.y9vad9.implier:implier:$version") // annotations
    ksp("com.y9vad9.implier:ksp-implementation:$version") // ksp implementation of annotations
}
```
