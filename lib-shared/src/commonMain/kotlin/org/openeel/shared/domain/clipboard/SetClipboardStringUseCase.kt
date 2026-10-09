package world.respect.shared.domain.clipboard


interface SetClipboardStringUseCase {

    operator fun invoke(content: String)

}