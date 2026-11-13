# lib-appui-compose guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module contains the Jetpack Compose multiplatform code for the UI layer of the app.

## Module guidance

Each screen should have a Composable function in the form of DestinationName**Screen** that takes
a ViewModel argument, and then an overloaded function with the same name that takes the UiState and
event handler arguments (can be used to generate previews as required).

e.g. 

```kotlin
@Composable
fun FooDetailScreen(
    viewModel: FooDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    FooDetailScreen(
        uiState = uiState,
        onClickSomething = viewModel::onClickSomething,
    )
}

@Composable
fun FooDetailScreen(
    uiState: FooDetailUiState,
    onClickSomething: () -> Unit
) {
    //Actual UI here
}

```

## Module Anti-Patterns : NEVER generate these

NEVER: Add unnecessary themeing/style info when the defaults will work fine.
```kotlin
ListItem(
    headlineContent = {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
)
```

ALWAYS: Use default applied styles and layout with built-in components wherever possible.
```kotlin
ListItem(
    headlineContent = {
        Text(text= title)
    },
    supportingContent = {
        Text(text=description)
    },
)
```

NEVER: Attempt to re-invent components that are already provided by Jetpack compose
```kotlin
Row {
    Icon(Icons.Default.Add, contentDescription = null)
    Spacer()
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

```

ALWAYS: Use built-in components already provided:
```kotlin
ListItem(
    headlineContent = {
        Text(text= title)
    },
    supportingContent = {
        Text(text=description)
    },
)
```

