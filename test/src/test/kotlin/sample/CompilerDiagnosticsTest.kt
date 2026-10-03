@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class)

package sample

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.y9vad9.implier.ImplierProcessorProvider
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompilerDiagnosticsTest {

    @Test
    fun failsWhenAnnotationAppliedToNonAbstractClass() {
        val source = SourceFile.kotlin(
            "InvalidTarget.kt",
            """
            package test
            import com.y9vad9.implier.ImmutableImpl

            @ImmutableImpl
            class RegularClass(val name: String)
            """.trimIndent()
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Unable to create realization from non-abstract class: RegularClass"))
    }

    @Test
    fun failsWhenMissingRequiredRealizationAnnotation() {
        val source = SourceFile.kotlin(
            "MissingRealization.kt",
            """
            package test
            import com.y9vad9.implier.BuilderImpl

            @BuilderImpl
            interface LonelyInterface {
                val value: String
            }
            """.trimIndent()
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("requires at least one of @ImmutableImpl or @MutableImpl"))
    }

    @Test
    fun failsWhenDslFunctionNameIsBlank() {
        val source = SourceFile.kotlin(
            "BlankDslName.kt",
            """
            package test
            import com.y9vad9.implier.ImmutableImpl
            import com.y9vad9.implier.DSLBuilderImpl

            @ImmutableImpl
            @DSLBuilderImpl("")
            interface BlankDsl {
                val value: String
            }
            """.trimIndent()
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("must specify a non-empty functionName"))
    }

    @Test
    fun succeedsAndGeneratesCodeForValidInterface() {
        val source = SourceFile.kotlin(
            "ValidSample.kt",
            """
            package test
            import com.y9vad9.implier.ImmutableImpl
            import com.y9vad9.implier.MutableImpl

            @ImmutableImpl
            @MutableImpl
            interface ValidSample {
                val title: String
                val count: Int
            }
            """.trimIndent()
        )

        val result = compile(source)

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    private fun compile(vararg sources: SourceFile): JvmCompilationResult {
        return KotlinCompilation().apply {
            this.sources = sources.toList()
            configureKsp {
                symbolProcessorProviders += ImplierProcessorProvider()
            }
            inheritClassPath = true
        }.compile()
    }
}
