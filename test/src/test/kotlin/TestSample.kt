import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.platform.commons.annotation.Testable
import sample.MutableSample
import sample.toImmutable
import sample.toMutable

@Testable
object TestSample {
    @Test
    fun codegenTest() {
        assertDoesNotThrow {
            MutableSample("value").toImmutable().toMutable()
        }
    }
}