# lib-directory

Lib-directory provides a set of common interfaces and models for the base directory (e.g. persons,
classes, enrollments) in an education environment (eg school). The interfaces implementations will
include:

* A local database for the local data source.
* Remote implementations for
  * OpenEel's own [app-server](../app-server/)
  * Keycloak
  * Google User Directory
* An offline-first repository implementation that mediates between the local and the remote.

