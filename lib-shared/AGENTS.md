# RESPECT lib-shared module guide

This file provides guidance for AI agents working with code in this 
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview
This module contains ViewModels and domain logic layer. When the same logic is required by more than
one ViewModel, create a UseCase. UseCases can use other UseCases.

### ViewModel guidance

Each ViewModel should have a UiState data class, and the ViewModel itself will have a flow
of it's UiState.

ViewModels should normally follow hte pattern as below:

```
data class ScreenNameUiState(
    val persons: DataLoadState<Object> = DataLoadingState(),
    val someButtonVisible: Boolean = false,
)

class ScreenNameViewModel(
    savedStateHandle: SavedStateHandle,
): RespectViewModel(
    savedStateHandle = savedStateHandle
){
    private val _uiState = MutableStateFlow(ScreenNameUiState())
    
    val uiState = _uiState.asStateFlow()
    
    init {
        //Load data/setup screen as required and update the uiState
    }
    
    //Event handlers here
    fun onClickSomeButton() {
    
    }
}
```

### Domain layer guidance

This is structured as UseCases as per Android Architecture Recommendations. Use cases always have
a single function `operator fun invoke`. They might be an interface where there are multiple 
different implementations (e.g. one for Android, and another for the http-server/JVM). 

Example single class use case:
```
class DoSomethingUseCase(schoolUrl: Url) {
    
    data class DoSomethingRequest(
        val param1: String,
        val param2: Boolean,
    )
    
    data class DoSomethingResult(
        val outcome: String,
        val time: Instant,
    )   
    
    suspend operator fun invoke(
        request: DoSomethingRequest
    ): DoSomethingResult {
        //Do something and return result
    }
    
    
}
```

Example UseCase with multiple implementations.

Interface:
```
interface DoSomethingUseCase {

    data class DoSomethingRequest(
        val param1: String,
        val param2: Boolean,
    )
    
    data class DoSomethingResult(
        val outcome: String,
        val time: Instant,
    )   
    
    suspend operator fun invoke(
        request: DoSomethingRequest
    ): DoSomethingResult
    
}
```

Implementation class for a given platform:
```
class DoSomethingUseCaseAndroid(
    val appContext: Context,
    val schoolUrl: Url,
): DoSomethingUseCase {
    
    suspend operator fun invoke(
        request: DoSomethingRequest
    ): DoSomethingResult {
        //Do something using Android-specific dependencies as needed, return result
    }
    
}
```

## Anti-Patterns: - Never Generate these

NEVER: In AppRoutes.kt: `data class SomeDetailScreen(val uid: String, val name: String)`

ALWAYS: `data class SomeDetailScreen(val uid: String)` The name should be looked up in one place,
        the detail screen itself

