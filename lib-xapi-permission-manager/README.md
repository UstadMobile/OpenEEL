# lib-xapi-permission-manager

The Experience API spec defines [oAUTH scopes](https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#42-oauth-10-authorization-scope)
by endpoint (e.g. statements/write, read, etc).

Sometimes more granular control over who can do what is needed. An XapiPolicy implementation can 
* Block or modify write operations (e.g. put/post a statement, or put/post/delete to an XAPI 
document resource).
* Modify read parameters e.g. access to statements can be limited by modifying the get statements 
  parameters to set the agent parameter to match the authenticated user.

This module provides an xAPI resource that can wrap any other xAPI resource to enforce permissions.

Intended use cases:

* On the http server: wrap an xAPI database resource to enforce permissions on incoming requests
* In a reverse proxy: wrap an xAPI http client resource to enforce permissions on requests that
  go to any third party LRS that does not otherwise support granular permission management.
* On the mobile app: wrap an xAPI offline-first repository to enforce permission management when
  the user can be logged into multiple accounts simultaneously.

