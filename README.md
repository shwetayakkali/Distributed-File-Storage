# Distributed-File-Storage
File Storage and retrieval in a Distributed System Network of Servers.

A Java application, which uses a Distributed Hash Table implementation for on-demand file insertion in constant time and file look-up in logarithmic time. The application also has a caching feature, which reduces look-up time on multiple requests.

Network consists of 7 servers and a client trying to store or retrieval a file. The file is stored by computing a hash on the file name and the level at which the file is stored. Once done the file is stored in O(1) i.e. constant time.
If a file retrieval is requested, the same hash function is computed on the file name and the searching starts from the base level. O(log N) time is required for file retrieval and download where N represents the number of servers present in the distributed network.
The Distributed Network of Servers form a dynamic Tree structure for each file and the file if present is always at the root node. The searching starts at the any of the leaf nodes and proceeds to the root via a consistent hash function.
The application also has a cahing function for replicating files at levels just below storage to ease search and retrieval process.

Client.java - Client which stores the required file and requests for the file.
Server.java - Has the functions for Consistent Hashing and provides transparency to the Client. Does file insertion in O(1) and file                     retrieval in O(log N)
HashClass.java - Consistent Hashing Implementation.
