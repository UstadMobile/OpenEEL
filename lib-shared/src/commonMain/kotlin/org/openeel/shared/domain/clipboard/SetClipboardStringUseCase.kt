package org.openeel.shared.domain.clipboard


interface SetClipboardStringUseCase {

    operator fun invoke(content: String)

}