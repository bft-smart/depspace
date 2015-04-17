#DepSpace

DepSpace (Dependable Tuple Space) is fault and intrusion-tolerant secure tuple space implementation. The main objective of the system is to provide an extended tuple space abstraction that could be used to implement Byzantine fault tolerant applications. The system design is described in an [EuroSys'08 paper](http://www.di.fc.ul.pt/~bessani/publications/eurosys08-depspace.pdf).

This package contains the DepSpace source code (src/), libraries needed (lib/), a Makefile, and configuration files (config/).
DepSpace requires the Java Runtime Environment version 1.7 or later.

------------ Important notice for developers -------------

This version of DepSpace uses a tunned version of BFT-SMaRt (lib/SMaRt-eds.jar). In the future it will be fully integrated with the most up-to-date distribution of this library.

------------- How to run DepSpace -----------------------

First you need to configurate the BFT-SMaRt library. Please take a look at [BFT-SMaRt page](http://www.di.fc.ul.pt/~bessani/publications/eurosys08-depspace.pdf).

After that, you just need to run the DepSpace replicas using the given script /scripts/replica by typing the command:

./replica <replica-id> <config-dir>

The <replica-id> field is the replica's unique identifier, and the <config-dir> field is the full path to the configuration directory (/config). One execution example:

./replica 0 /home/ubuntu/EDS/config

Note that, by default you need to run 4 DepSpace replicas (from <replica-id> 0 to 3).

To run the client you need to run the script scripts/client by executing:
./client <client-id> <config-dir> <extension-code-dir>

The <client-id> refers to the client unique identifier. This id should be different from the ones used to lauch the DepSpace replicas.
The <config-dir> field can be the same used above.
The <extension-code-dir> field is related with depspace extentions source directory. It should be the full path of the directory used to store the DepSpace extensions. An example is:

./client 4 /home/ubuntu/EDS/config /home/ubuntu/EDS/src/

