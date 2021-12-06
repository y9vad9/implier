import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.platform.commons.annotation.Testable
import sample.*

@Testable
object TestSample {
    @Test
    fun codegenTest() {
        assertDoesNotThrow {
            MutableSample("value", 1).toImmutable().toMutable()
        }
    }
    @Test
    fun testFactory() {
        assertDoesNotThrow {
            Sample("string", 1).toMutable().toImmutable()
        }
    }
    @Test
    fun testBuilder() {
        assertDoesNotThrow {
            SampleBuilder()
                .number(0)
                .sample("something")
                .build()
                .toMutable()
                .toImmutable()
        }
    }
}