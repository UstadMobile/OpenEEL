package org.openeel.sharedse.domain.account.authenticatepassword

import org.openeel.credentials.passkey.OpenEelQRBadgeCredential
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.db.school.adapters.toPersonEntities
import org.openeel.lib.dataloadstate.throwable.ForbiddenException
import org.openeel.shared.domain.account.authenticatepassword.AuthenticateQrBadgeUseCase

class AuthenticateQrBadgeUseCaseDbImpl(
    private val schoolDb: SchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) : AuthenticateQrBadgeUseCase {

    override suspend fun invoke(
        credential: OpenEelQRBadgeCredential
    ): AuthenticateQrBadgeUseCase.Response {

        // First, find the QR badge by the URL from the credential
        val qrCodeEntity = schoolDb.getPersonQrBadgeEntityDao().findByQrCodeUrl(
            qrCodeUrl = credential.qrCodeUrl.toString()
        ) ?: throw ForbiddenException("QR badge not found")

        // Find the person using the GUID from the QR code entity
        val personEntity = schoolDb.getPersonEntityDao().findByGuidNum(
            uidNumberMapper(qrCodeEntity.pqrGuid)
        )?.toPersonEntities()?.toModel()
            ?: throw ForbiddenException("Person not found")

        return AuthenticateQrBadgeUseCase.Response(
            authenticatedPerson = personEntity
        )
    }
}