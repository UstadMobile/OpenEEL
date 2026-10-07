# lib-datalayer guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module contains interfaces for the data layer of repository (used by ViewModels in the mobile
app, Routes in KTOR http server, and domain layer use cases in the lib-shared module).

`SchoolDataSource` is used for school-level data including 
[Experience API (xAPI)](https://github.com/adlnet/xAPI-Spec) experience data (e.g. scores, what content has been experienced, etc),
user profile data, etc. 

`SchoolDirectoryResource` is used to provide a directory of schools for users to select when 
logging in.

## DataSources guidance

* DataSources are interfaces with functions to retrieve and update data, where applicable in
  accordance with external specifications.

```kotlin
interface FooDataSource {

    data class GetListParams(
        val uid: String? = null,
        val maxItems: Int? = null,
    ) {
        /** 
         * Convert the list parameters into KTOR StringResources that can be used in a URLBuilder
         * when making a request
         */
        fun toParameters(): StringValues {
            return ParametersBuilder().also { parameters ->
                parameters.appendIfNotNull("uid", uid)
                parameters.appendIfNotNull(maxItems, maxItems?.toString())
            }.build()
        }

        companion object {

          /**
           * Convert parameters from StringValues to the type safe GetListParams data class. Used by
           * the server resource when receiving a request.
           */
          fun fromParams(
                params: StringValues,
            ): GetListParams {
                return GetListParams(
                    uid = params["uid"],
                    maxItems = params["maxItems"]?.toInt()
                )
            }

        }
    }

    /** 
     * Get a list of items
     */
    suspend fun get(
        listParams: GetListParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): DataLoadState<List<Foo>>

    /** 
     * Get a list of items as a flow
     */
    suspend fun getAsFlow(
        listParams: GetListParams,
        dataLoadParams: DataLoadParams = DataLoadParams(),
    ): Flow<DataLoadState<List<Foo>>>

    /** 
     * Write a list of items 
     */
    suspend fun post(
        list: List<Foo>,
    ): DataLoadState<List<String>>
    
}
```

## Guidance

* The datalayer is split into two parts: SchoolDataSource for school-level data (users, student 
  progress, etc) and RespectAppDataSource for app-wide data.
* All models must be serializable using kotlinx Serialization.
* Always add a GetListParams data class to the DataSource interface. This is used as a parameter for
  list functions (e.g. list, listAsFlow, listAsPagingSource etc). The GetListParams function should
  always have a fromParams companion function that accepts a single ```StringValues``` parameter that will 
  return a GetListParams data class based on the parameters represented by the StringValues object.
  The Http client will add the GetListParams to the URL query parameters and the HTTP server will 
  use the fromParams function to convert the parameters back into a data class.
