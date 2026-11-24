# lib-xapi-core guide

This file provides guidance for AI agents working with code in this
module. Always follow the repository guidelines in [../AGENTS.md](../AGENTS.md).

## Module overview

This module is used to run peer-to-peer synchronization of xAPI data between devices without
requiring connectivity to the primary xAPI server (e.g. when they do not have Internet access).

The P2P sync can support different underlying implementations (e.g. Network Service Discovery,
[Google Nearby](https://developers.google.com/nearby), etc).

This module contains:
* **Interfaces** to be implemented for each specific implementation module (e.g. a module for
  network service discovery, a module for Google Nearby, etc) : 
  [XapiP2pSyncAdapter](src/commonMain/kotlin/org/openeel/lib/xapi/p2psync/XapiP2pSyncAdapter.kt) and
  [NodeTracker](src/commonMain/kotlin/org/openeel/lib/xapi/p2psync/NodeTracker.kt) and
* **Push and poll sync UseCases**: 
  [PushStatementsToPeersUseCase](src/commonMain/kotlin/org/openeel/lib/xapi/p2psync/push/PushStatementsToPeersUseCase.kt)
  and [PollPeersUseCase](lib-xapi-p2p-sync/src/commonMain/kotlin/org/openeel/lib/xapi/p2psync/poll/PollPeersUseCase.kt)
  that push statements to and poll statements to/from peers respectively.

You should read [lib-xapi-core AGENTS.md](../lib-xapi-core/AGENTS.md) before working with
code in this module.

