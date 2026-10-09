package world.respect.shared.domain.account.setpassword

import io.ktor.util.encodeBase64
import world.respect.datalayer.school.model.PersonPassword
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.time.Clock

class EncryptPersonPasswordUseCaseImpl: EncryptPersonPasswordUseCase {

    override fun invoke(request: EncryptPersonPasswordUseCase.Request): PersonPassword {

        val keySpec = PBEKeySpec(request.password.toCharArray(), request.salt.toByteArray(),
            DEFAULT_ITERATIONS,
            DEFAULT_KEY_LEN
        )
        val keyFactory = SecretKeyFactory.getInstance(KEY_ALGO)

        val now = Clock.System.now()

        return PersonPassword(
            personGuid = request.personGuid,
            authAlgorithm = KEY_ALGO,
            authEncoded = keyFactory.generateSecret(keySpec).encoded.encodeBase64(),
            authSalt = request.salt,
            authIterations = DEFAULT_ITERATIONS,
            authKeyLen = DEFAULT_KEY_LEN,
            lastModified = now,
            stored = now,
        )
    }



    companion object {

        const val DEFAULT_SALT_LEN = 16

        const val DEFAULT_ITERATIONS = 10_000

        const val DEFAULT_KEY_LEN = 512

        const val KEY_ALGO = "PBKDF2WithHmacSHA1"

    }
}