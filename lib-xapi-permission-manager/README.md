# lib-xapi-permission-manager

This module provides an xAPI resource that can wrap any other xAPI resource to enforce permissions.

Intended use cases:

* On the http server: wrap an xAPI database resource to enforce permissions on incoming requests
* In a reverse proxy: wrap an xAPI http client resource to enforce permissions on requests that
  go to any third party LRS that does not otherwise support granular permission management.
* On the mobile app: wrap an xAPI offline-first repository to enforce permission management when
  the user can be logged into multiple accounts simultaneously.

