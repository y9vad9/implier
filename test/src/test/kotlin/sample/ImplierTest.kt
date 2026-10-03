package sample

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImplierTest {

    @Test
    fun testImmutableRealizationAndDataClassBehavior() {
        val sample1 = ImmutableSample(
            sample = "hello",
            number = 42,
            nullableField = null,
            list = listOf("a", "b")
        )
        val sample2 = ImmutableSample(
            sample = "hello",
            number = 42,
            nullableField = null,
            list = listOf("a", "b")
        )

        // Real property assertions
        assertEquals("hello", sample1.sample)
        assertEquals(42, sample1.number)
        assertNull(sample1.nullableField)
        assertEquals(listOf("a", "b"), sample1.list)

        // data class equals and hashCode
        assertEquals(sample1, sample2)
        assertEquals(sample1.hashCode(), sample2.hashCode())

        // data class toString
        assertTrue(sample1.toString().startsWith("ImmutableSample("))
        assertTrue(sample1.toString().contains("sample=hello"))

        // data class copy
        val modified = sample1.copy(number = 99, nullableField = "not-null")
        assertEquals(99, modified.number)
        assertEquals("not-null", modified.nullableField)
        assertEquals("hello", modified.sample)
        assertNotEquals(sample1, modified)

        // toMutable conversion
        val mutable = sample1.toMutable()
        assertEquals(sample1.sample, mutable.sample)
        assertEquals(sample1.number, mutable.number)
        assertEquals(sample1.nullableField, mutable.nullableField)
        assertEquals(sample1.list, mutable.list)
    }

    @Test
    fun testMutableRealization() {
        val mutable = MutableSample(
            sample = "initial",
            number = 10,
            nullableField = "value",
            list = listOf("item")
        )

        assertEquals("initial", mutable.sample)
        assertEquals(10, mutable.number)
        assertEquals("value", mutable.nullableField)
        assertEquals(listOf("item"), mutable.list)

        // Mutate fields
        mutable.sample = "mutated"
        mutable.number = 20
        mutable.nullableField = null
        mutable.list = listOf("item1", "item2")

        assertEquals("mutated", mutable.sample)
        assertEquals(20, mutable.number)
        assertNull(mutable.nullableField)
        assertEquals(listOf("item1", "item2"), mutable.list)

        // toImmutable conversion
        val immutable = mutable.toImmutable()
        assertEquals("mutated", immutable.sample)
        assertEquals(20, immutable.number)
        assertNull(immutable.nullableField)
        assertEquals(listOf("item1", "item2"), immutable.list)
    }

    @Test
    fun testFactoryFunction() {
        // Sample() factory prefers Immutable when both @ImmutableImpl and @MutableImpl are present
        val created = Sample(
            sample = "from-factory",
            number = 7,
            nullableField = "test",
            list = listOf("one")
        )

        assertEquals("from-factory", created.sample)
        assertEquals(7, created.number)
        assertEquals("test", created.nullableField)
        assertEquals(listOf("one"), created.list)
        assertTrue(created is ImmutableSample, "Factory should instantiate Immutable realization by default")
    }

    @Test
    fun testBuilderWithNonNullAndNullableProperties() {
        // Build with all properties set
        val built = SampleBuilder()
            .sample("builder-sample")
            .number(123)
            .nullableField("custom")
            .list(listOf("x", "y"))
            .build()

        assertEquals("builder-sample", built.sample)
        assertEquals(123, built.number)
        assertEquals("custom", built.nullableField)
        assertEquals(listOf("x", "y"), built.list)
        assertTrue(built is ImmutableSample, "Builder should instantiate ImmutableSample")

        // Build with nullableField omitted (should default to null without throwing)
        val withNullOmitted = SampleBuilder()
            .sample("omitted-nullable")
            .number(456)
            .list(emptyList())
            .build()

        assertEquals("omitted-nullable", withNullOmitted.sample)
        assertEquals(456, withNullOmitted.number)
        assertNull(withNullOmitted.nullableField, "Nullable property should default to null")

        // Non-null property omitted must throw IllegalStateException via Delegates.notNull()
        assertThrows<IllegalStateException> {
            SampleBuilder()
                .number(456)
                .list(emptyList())
                .build()
        }
    }

    @Test
    fun testDSLBuilderWithNonNullAndNullableProperties() {
        // DSL with WITH_ACCESSORS: supports property access and set*
        val result = sampleDSL {
            sample = "dsl-sample"
            number = 999
            nullableField = "set-in-dsl"
            list = listOf("dsl1")
        }

        assertEquals("dsl-sample", result.sample)
        assertEquals(999, result.number)
        assertEquals("set-in-dsl", result.nullableField)
        assertEquals(listOf("dsl1"), result.list)
        assertTrue(result is ImmutableSample, "DSL should instantiate ImmutableSample")

        // Using set* accessor functions
        val resultWithAccessors = sampleDSL {
            setSample("accessor-sample")
            setNumber(888)
            setList(listOf("dsl2"))
        }

        assertEquals("accessor-sample", resultWithAccessors.sample)
        assertEquals(888, resultWithAccessors.number)
        assertNull(resultWithAccessors.nullableField, "Nullable property should default to null in DSL")

        // Omitting non-null property throws IllegalStateException
        assertThrows<IllegalStateException> {
            sampleDSL {
                sample = "only-sample"
            }
        }
    }

    @Test
    fun testDtoAndPatching() {
        val original = Sample(
            sample = "orig-title",
            number = 1,
            nullableField = "orig-note",
            list = listOf("initial")
        )

        // toDto
        val dto = original.toDto()
        assertEquals("orig-title", dto.sample)
        assertEquals(1, dto.number)
        assertEquals("orig-note", dto.nullableField)
        assertEquals(listOf("initial"), dto.list)

        // Patch only single property
        val patch1 = DtoSample(sample = "updated-title")
        val patched1 = original.toPatched(patch1)
        assertEquals("updated-title", patched1.sample)
        assertEquals(1, patched1.number)
        assertEquals("orig-note", patched1.nullableField)
        assertEquals(listOf("initial"), patched1.list)

        // Patch multiple properties
        val patch2 = DtoSample(number = 100, nullableField = "new-note")
        val patched2 = patched1.toPatched(patch2)
        assertEquals("updated-title", patched2.sample)
        assertEquals(100, patched2.number)
        assertEquals("new-note", patched2.nullableField)
        assertEquals(listOf("initial"), patched2.list)
    }

    @Test
    fun testAbstractClassSupport() {
        val mutable = MutableAbstractSample(title = "AbstractTitle", count = 5)
        assertEquals("AbstractTitle", mutable.title)
        assertEquals(5, mutable.count)
        assertEquals("AbstractTitle: 5", mutable.display)
        assertEquals(10, mutable.doubleCount())

        // Abstract class converts to Immutable
        val immutable = mutable.toImmutable()
        assertEquals("AbstractTitle", immutable.title)
        assertEquals(5, immutable.count)
        assertEquals("AbstractTitle: 5", immutable.display)
        assertEquals(10, immutable.doubleCount())

        // Abstract class realization is a data class (has no-arg constructor)
        val copy = immutable.copy(count = 10)
        assertEquals("AbstractTitle", copy.title)
        assertEquals(10, copy.count)
        assertEquals(immutable, ImmutableAbstractSample(title = "AbstractTitle", count = 5))

        // Factory function for abstract class
        val fromFactory = AbstractSample(title = "FactoryTitle", count = 42)
        assertEquals("FactoryTitle", fromFactory.title)
        assertEquals(42, fromFactory.count)
        assertTrue(fromFactory is ImmutableAbstractSample)

        // Builder with WITH_ACCESSORS on abstract class
        val built = AbstractSampleBuilder()
            .setTitle("BuilderAbstract")
            .setCount(15)
            .build()
        assertEquals("BuilderAbstract", built.title)
        assertEquals(15, built.count)

        // DSL with PROPERTY_ACCESS on abstract class
        val fromDsl = abstractSampleDSL {
            title = "DSLAbstract"
            count = 25
        }
        assertEquals("DSLAbstract", fromDsl.title)
        assertEquals(25, fromDsl.count)

        // DTO and patching on abstract class
        val dto = fromDsl.toDto()
        assertEquals("DSLAbstract", dto.title)
        assertEquals(25, dto.count)

        val patched = fromDsl.toPatched(DtoAbstractSample(title = "PatchedAbstract"))
        assertEquals("PatchedAbstract", patched.title)
        assertEquals(25, patched.count)
    }

    @Test
    fun testImmutableOnlySubset() {
        val built = ImmutableOnlySampleBuilder()
            .id(1001L)
            .name("Subset")
            .build()

        assertEquals(1001L, built.id)
        assertEquals("Subset", built.name)
        assertTrue(built is ImmutableImmutableOnlySample)

        // ImmutableOnlySample is a data class
        val copy = built.copy(name = "Updated")
        assertEquals(1001L, copy.id)
        assertEquals("Updated", copy.name)
    }

    @Test
    fun testMutableOnlySubset() {
        // Factory with only MutableImpl
        val created = MutableOnlySample(score = 98.5, flag = true)
        assertEquals(98.5, created.score)
        assertEquals(true, created.flag)
        assertTrue(created is MutableMutableOnlySample)

        // Builder with only MutableImpl
        val built = MutableOnlySampleBuilder()
            .score(75.0)
            .flag(false)
            .build()
        assertEquals(75.0, built.score)
        assertEquals(false, built.flag)
        assertTrue(built is MutableMutableOnlySample)

        // DSL with only MutableImpl
        val fromDsl = mutableOnlyDSL {
            score = 88.0
            flag = true
        }
        assertEquals(88.0, fromDsl.score)
        assertEquals(true, fromDsl.flag)
        assertTrue(fromDsl is MutableMutableOnlySample)

        // DTO and patching with only MutableImpl
        val dto = fromDsl.toDto()
        assertEquals(88.0, dto.score)
        assertEquals(true, dto.flag)

        val patched = fromDsl.toPatched(DtoMutableOnlySample(score = 92.0))
        assertEquals(92.0, patched.score)
        assertEquals(true, patched.flag)
        assertTrue(patched is MutableMutableOnlySample)
    }

    @Test
    fun testNestedDeclaration() {
        val created = NestedSample(key = "k1", value = "v1")
        assertEquals("k1", created.key)
        assertEquals("v1", created.value)
        assertTrue(created is ImmutableNestedSample)

        val mutable = created.toMutable()
        mutable.value = "v2"
        assertEquals("v2", mutable.value)
        val backToImmutable = mutable.toImmutable()
        assertEquals("v2", backToImmutable.value)
    }
}
