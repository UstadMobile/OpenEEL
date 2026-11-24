# lib-xapi-p2p-sync

xAPI peer-to-peer sync allows a cluster of devices (eg tablets in a classroom to sync directly with 
each other even when connectivity to the primary xAPI server itself is not available (e.g. when 
devices are not connected the Internet). This allows for: 

* Students to use any available device: students can pick up from where they left off on any device
  in the cluster.
* Teachers to see progress without waiting

How it works:

* **Push**: Each node will send any new statements that were written locally to all other available 
  nodes in the cluster.  
* **Polling**: Each node in the cluster will periodically poll all other available nodes to discover
  if they have statements available that are not on that device. This is a kind of "belt and 
  suspenders" approach e.g. if node A writes a statement, which is then copied to node B, after 
  which node A runs out of battery, then node C can discover this statement by polling node B.
  
The sync mechanism can work using any implementation of 
[XapiResource](../lib-xapi-core/src/commonMain/kotlin/org/openeel/lib/xapi/resources/XapiResource.kt).
