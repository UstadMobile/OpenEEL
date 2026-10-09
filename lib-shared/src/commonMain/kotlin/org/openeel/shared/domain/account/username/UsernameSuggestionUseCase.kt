package world.respect.shared.domain.account.username



interface UsernameSuggestionUseCase {
    suspend operator fun invoke(name:String): String
}