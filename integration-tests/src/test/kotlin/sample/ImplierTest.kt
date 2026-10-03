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
        // GIVEN
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

        // WHEN
        val modified = sample1.copy(number = 99, nullableField = "not-null")
        val mutable = sample1.toMutable()

        // THEN
        assertEquals("hello", sample1.sample)
        assertEquals(42, sample1.number)
        assertNull(sample1.nullableField)
        assertEquals(listOf("a", "b"), sample1.list)

        assertEquals(sample1, sample2)
        assertEquals(sample1.hashCode(), sample2.hashCode())

        assertTrue(sample1.toString().startsWith("ImmutableSample("))
        assertTrue(sample1.toString().contains("sample=hello"))

        assertEquals(99, modified.number)
        assertEquals("not-null", modified.nullableField)
        assertEquals("hello", modified.sample)
        assertNotEquals(sample1, modified)

        assertEquals(sample1.sample, mutable.sample)
        assertEquals(sample1.number, mutable.number)
        assertEquals(sample1.nullableField, mutable.nullableField)
        assertEquals(sample1.list, mutable.list)
    }

    @Test
    fun testMutableRealization() {
        // GIVEN
        val mutable = MutableSample(
            sample = "initial",
            number = 10,
            nullableField = "value",
            list = listOf("item")
        )

        // WHEN
        mutable.sample = "mutated"
        mutable.number = 20
        mutable.nullableField = null
        mutable.list = listOf("item1", "item2")
        val immutable = mutable.toImmutable()

        // THEN
        assertEquals("mutated", mutable.sample)
        assertEquals(20, mutable.number)
        assertNull(mutable.nullableField)
        assertEquals(listOf("item1", "item2"), mutable.list)

        assertEquals("mutated", immutable.sample)
        assertEquals(20, immutable.number)
        assertNull(immutable.nullableField)
        assertEquals(listOf("item1", "item2"), immutable.list)
    }

    @Test
    fun testFactoryFunction() {
        // GIVEN / WHEN
        val created = Sample(
            sample = "from-factory",
            number = 7,
            nullableField = "test",
            list = listOf("one")
        )

        // THEN
        assertEquals("from-factory", created.sample)
        assertEquals(7, created.number)
        assertEquals("test", created.nullableField)
        assertEquals(listOf("one"), created.list)
        assertTrue(created is ImmutableSample)
    }

    @Test
    fun testBuilderWithAllPropertiesSet() {
        // GIVEN
        val builder = SampleBuilder()
            .sample("builder-sample")
            .number(123)
            .nullableField("custom")
            .list(listOf("x", "y"))

        // WHEN
        val built = builder.build()

        // THEN
        assertEquals("builder-sample", built.sample)
        assertEquals(123, built.number)
        assertEquals("custom", built.nullableField)
        assertEquals(listOf("x", "y"), built.list)
        assertTrue(built is ImmutableSample)
    }

    @Test
    fun testBuilderWithNullablePropertyOmitted() {
        // GIVEN
        val builder = SampleBuilder()
            .sample("omitted-nullable")
            .number(456)
            .list(emptyList())

        // WHEN
        val built = builder.build()

        // THEN
        assertEquals("omitted-nullable", built.sample)
        assertEquals(456, built.number)
        assertNull(built.nullableField)
    }

    @Test
    fun testBuilderThrowsWhenRequiredPropertyOmitted() {
        // GIVEN
        val builder = SampleBuilder()
            .number(456)
            .list(emptyList())

        // WHEN / THEN
        assertThrows<IllegalStateException> {
            builder.build()
        }
    }

    @Test
    fun testDSLBuilderWithPropertyAccess() {
        // GIVEN / WHEN
        val result = sampleDSL {
            sample = "dsl-sample"
            number = 999
            nullableField = "set-in-dsl"
            list = listOf("dsl1")
        }

        // THEN
        assertEquals("dsl-sample", result.sample)
        assertEquals(999, result.number)
        assertEquals("set-in-dsl", result.nullableField)
        assertEquals(listOf("dsl1"), result.list)
        assertTrue(result is ImmutableSample)
    }

    @Test
    fun testDSLBuilderWithAccessorsAndNullableOmitted() {
        // GIVEN / WHEN
        val result = sampleDSL {
            setSample("accessor-sample")
            setNumber(888)
            setList(listOf("dsl2"))
        }

        // THEN
        assertEquals("accessor-sample", result.sample)
        assertEquals(888, result.number)
        assertNull(result.nullableField)
    }

    @Test
    fun testDSLBuilderThrowsWhenRequiredPropertyOmitted() {
        // GIVEN / WHEN / THEN
        assertThrows<IllegalStateException> {
            sampleDSL {
                sample = "only-sample"
            }
        }
    }

    @Test
    fun testDtoAndPatching() {
        // GIVEN
        val original = Sample(
            sample = "orig-title",
            number = 1,
            nullableField = "orig-note",
            list = listOf("initial")
        )
        val patch1 = DtoSample(sample = "updated-title")
        val patch2 = DtoSample(number = 100, nullableField = "new-note")

        // WHEN
        val dto = original.toDto()
        val patched1 = original.toPatched(patch1)
        val patched2 = patched1.toPatched(patch2)

        // THEN
        assertEquals("orig-title", dto.sample)
        assertEquals(1, dto.number)
        assertEquals("orig-note", dto.nullableField)
        assertEquals(listOf("initial"), dto.list)

        assertEquals("updated-title", patched1.sample)
        assertEquals(1, patched1.number)
        assertEquals("orig-note", patched1.nullableField)
        assertEquals(listOf("initial"), patched1.list)

        assertEquals("updated-title", patched2.sample)
        assertEquals(100, patched2.number)
        assertEquals("new-note", patched2.nullableField)
        assertEquals(listOf("initial"), patched2.list)
    }

    @Test
    fun testAbstractClassSupport() {
        // GIVEN
        val mutable = MutableAbstractSample(title = "AbstractTitle", count = 5)

        // WHEN
        val immutable = mutable.toImmutable()
        val copy = immutable.copy(count = 10)
        val fromFactory = AbstractSample(title = "FactoryTitle", count = 42)
        val built = AbstractSampleBuilder()
            .setTitle("BuilderAbstract")
            .setCount(15)
            .build()
        val fromDsl = abstractSampleDSL {
            title = "DSLAbstract"
            count = 25
        }
        val dto = fromDsl.toDto()
        val patched = fromDsl.toPatched(DtoAbstractSample(title = "PatchedAbstract"))

        // THEN
        assertEquals("AbstractTitle", mutable.title)
        assertEquals(5, mutable.count)
        assertEquals("AbstractTitle: 5", mutable.display)
        assertEquals(10, mutable.doubleCount())

        assertEquals("AbstractTitle", immutable.title)
        assertEquals(5, immutable.count)
        assertEquals("AbstractTitle: 5", immutable.display)
        assertEquals(10, immutable.doubleCount())

        assertEquals("AbstractTitle", copy.title)
        assertEquals(10, copy.count)
        assertEquals(immutable, ImmutableAbstractSample(title = "AbstractTitle", count = 5))

        assertEquals("FactoryTitle", fromFactory.title)
        assertEquals(42, fromFactory.count)
        assertTrue(fromFactory is ImmutableAbstractSample)

        assertEquals("BuilderAbstract", built.title)
        assertEquals(15, built.count)

        assertEquals("DSLAbstract", fromDsl.title)
        assertEquals(25, fromDsl.count)

        assertEquals("DSLAbstract", dto.title)
        assertEquals(25, dto.count)

        assertEquals("PatchedAbstract", patched.title)
        assertEquals(25, patched.count)
    }

    @Test
    fun testImmutableOnlySubset() {
        // GIVEN
        val builder = ImmutableOnlySampleBuilder()
            .id(1001L)
            .name("Subset")

        // WHEN
        val built: ImmutableOnlySample = builder.build()

        // THEN
        assertEquals(1001L, built.id)
        assertEquals("Subset", built.name)
        assertTrue(built is ImmutableImmutableOnlySample)

        val copy = built.copy(name = "Updated")
        assertEquals(1001L, copy.id)
        assertEquals("Updated", copy.name)
    }

    @Test
    fun testMutableOnlySubset() {
        // GIVEN
        val builder = MutableOnlySampleBuilder()
            .score(75.0)
            .flag(false)

        // WHEN
        val created = MutableOnlySample(score = 98.5, flag = true)
        val built = builder.build()
        val fromDsl = mutableOnlyDSL {
            score = 88.0
            flag = true
        }
        val dto = fromDsl.toDto()
        val patched = fromDsl.toPatched(DtoMutableOnlySample(score = 92.0))

        // THEN
        assertEquals(98.5, created.score)
        assertEquals(true, created.flag)
        assertTrue(created is MutableMutableOnlySample)

        assertEquals(75.0, built.score)
        assertEquals(false, built.flag)
        assertTrue(built is MutableMutableOnlySample)

        assertEquals(88.0, fromDsl.score)
        assertEquals(true, fromDsl.flag)
        assertTrue(fromDsl is MutableMutableOnlySample)

        assertEquals(88.0, dto.score)
        assertEquals(true, dto.flag)

        assertEquals(92.0, patched.score)
        assertEquals(true, patched.flag)
        assertTrue(patched is MutableMutableOnlySample)
    }

    @Test
    fun testNestedDeclaration() {
        // GIVEN
        val created = NestedSample(key = "k1", value = "v1")

        // WHEN
        val mutable = created.toMutable()
        mutable.value = "v2"
        val backToImmutable = mutable.toImmutable()

        // THEN
        assertEquals("k1", created.key)
        assertEquals("v1", created.value)
        assertTrue(created is ImmutableNestedSample)
        assertEquals("v2", mutable.value)
        assertEquals("v2", backToImmutable.value)
    }
}
