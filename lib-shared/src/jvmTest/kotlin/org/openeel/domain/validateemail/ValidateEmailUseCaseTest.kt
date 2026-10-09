package world.respect.domain.validateemail


import world.respect.shared.domain.validateemail.ValidateEmailUseCase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateEmailUseCaseTest {

    private val validateEmailUseCase = ValidateEmailUseCase()

    @Test
    fun validEmailShouldReturnTrue() {
        assertTrue(validateEmailUseCase("test@example.com"))
        assertTrue(validateEmailUseCase("user.name@domain.co.in"))
        assertTrue(validateEmailUseCase("a@b.io"))
    }

    @Test
    fun emailWithoutAtSymbolShouldReturnFalse() {
        assertFalse(validateEmailUseCase("testexample.com"))
    }

    @Test
    fun emailWithoutDotAfterAtShouldReturnFalse() {
        assertFalse(validateEmailUseCase("test@examplecom"))
    }

    @Test
    fun emailWithSpacesShouldReturnFalse() {
        assertFalse(validateEmailUseCase("test @example.com"))
        assertFalse(validateEmailUseCase("test@ example.com"))
    }

    @Test
    fun emailWithInvalidCharactersShouldReturnFalse() {
        assertFalse(validateEmailUseCase("test@[example].com"))
        assertFalse(validateEmailUseCase("test\\example@example.com"))
    }

    @Test
    fun emailWithLeadingAndTrailingSpacesShouldBeValid() {
        assertTrue(validateEmailUseCase("   test@example.com  "))
    }
}
