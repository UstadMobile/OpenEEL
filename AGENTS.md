# Respect project guide

This file provides guidance for AI agents working with code in this repository.

## Repository overview

This is the Kotlin programming language repository containing:
* The Respect Android app: [app-android module](app-android/)
* Compose Multiplatform UI: [lib-appui-compose module](lib-appui-compose/)
* ViewModels and domain logic architecture: [lib-shared module](lib-shared/)
* The datalayer is an offline-first datalayer in the modules [lib-datalayer (interfaces/abstract classes)](lib-datalayer/),
  [lib-datalayer-db (local database layer)](lib-datalayer-db),
  [lib-datalayer-http (http datasource implementation)](lib-datalayer-http),
  [lib-datalayer-repository (offline-first repository implementation)](lib-datalayer-repository),
* Server built using KTOR: [app-server module](app-server/)
* The main Android Activity is `app-android/src/main/kotlin/world/respect/MainActivity.kt`
* The NavHost with composable routes in ```lib-appui-compose/src/commonMain/kotlin/world/respect/app/app/AppNavHost.kt```

BEFORE running tests, modifying, or investigating code - identify the module and READ the
AGENTS.md file for that module.

## Repository wide patterns to follow
* Always use idiomatic Kotlin, not Java-style code written in Kotlin.
* Favor the use of Kotlin collections functions such `.map`, `.filter` etc instead of manual use of 
  loops.
* Use Koin for dependency injection. Dependencies can be:
  * Singletons: one instance for the whole application
  * School scope: one instance that is tied to a particular school/http API enpdoint (e.g. the SQLite 
    Room database for a given school, an AcceptInviteUseCase, etc).
  * Account scope: one instance tied to a particular account. This is a child scope of the school scope. It can 
    access dependencies from the related school scope (but not vice versa). Used for datasources
    (where the account determines permissions rules), authentication use cases, etc.

## Kotlin Programming Language Anti-Patterns: - Never Generate these

## 1. Coroutines:
NEVER: GlobalScope.launch { } 

ALWAYS: viewModelScope.launch { } or lifecycleScope.launch { } 

## 2. Null Safety:
NEVER: value!! (non-null assertion)

ALWAYS: value?.let { } or value ?: defaultValue

## 3. Data classes
NEVER: data class Foo(var x: Int)

ALWAYS: data class Foo(val x: Int)

## 4. Sealed Classes

ALWAYS: use when{} as expression, NEVER as statement.

## 5. Idiomatic Kotlin

NEVER: 
```
val callParams = call.request.params
val resourceParams = ResourceClass.Params.fromParameters(call.request.params)
val queryResult = dataSource.resource.query(params = resourceParams)
call.respond(queryResult)
```
ALWAYS: Use idiomatic Kotlin e.g.
```
call.respond(
    dataSource.resource.query(
        params = ResourceClass.Params.fromParameters(call.request.params)
    )
)
```

## Repository Anti-Patterns: - Never Generate these:

## 1. Json processing

NEVER: class MyClass() {  
  fun doSomething(str: String) {
      Json.decodeFromString(Foo.serializer(), str)
  }
}

ALWAYS: class MyClass(private val json: Json) {
  fun doSomething(str: String) {
      json.decodeFromString(Foo.serializer(), str)
  }
}

Exception: ONLY in AppRoutes.kt (which does not have access to dependency injection) it is OK to
use the global default Json.
