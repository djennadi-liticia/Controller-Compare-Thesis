# Controller-Compare-Thesis
Performance Evaluation of Controller for Low-power IoT Networks.
==

### Intro
This repo hosts the source code for academic research on comparing μSDN and SDN-WISE architecture. This repo also host source code for connecting ONOS controller with μSDN sink node.

###Prerequistes
- Linux Operating system
- Install bazel from [here](https://docs.bazel.build/versions/master/install-ubuntu.html)
- Contiki OS with Cooja simulator (comes in built with repository)
- Install ONOS image from [here](https://wiki.onosproject.org/display/ONOS/Developer+Quick+Start)
### Getting Started
To get you going you need to clone the repository on your local subsystems with prequistes need to be installed before we start simulation.
```
### uSDN setup:
You'll also need to install the 20-bit mspgcc compiler.

For instructions on how to compile this please click [here](https://github.com/contiki-os/contiki/wiki/MSP430X)

For a pre-compiled version for Ubuntu-64 please click [here](https://github.com/pksec/msp430-gcc-4.7.3)

- Use the precompiled msp430-gcc version above. You literally just need to extract it to a folder of your choice and then add it to your path `export PATH=$PATH:<uri-to-your-mspgcc>`. Once you have done this your path should look something like this:

```
echo $PATH
/home/mkulkarni/Compilers/mspgcc-.7.3/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin:/usr/lib/jvm/java-8-oracle/bin:/usr/lib/jvm/java-8-oracle/db/bin:/usr/lib/jvm/java-8-oracle/jre/bin
```

- NB There is only *ONE* msp430-gcc compiler in the path. If there are two you need to remove the old one.
- Check the mspgcc version (`msp430-gcc --version`) it should be 4.7.3.
This step is optinal, you can use pre-compiled sdn-sink and sdn-node
  cd usdn/examples/sdn/controller/
  make clean & make
  cd ..
  cd node/
  make clean & make
Move to usdn/examples/sdn and run this script so that your parameters are set before the simulation.
./compile.sh MULTIFLOW=1 NUM_APPS=1 FLOWIDS=1 TXNODES=8 RXNODES=10 DELAY=0 BRMIN=5 BRMAX=5 NSUFREQ=600 FTLIFETIME=300 FTREFRESH=1 FORCENSU=1 LOG_LEVEL_SDN=LOG_LEVEL_DBG LOG_LEVEL_ATOM=LOG_LEVEL_DBG
```
- Use command cd/tools/cooja && ant run to run Cooja simulator.
### Varying different Args:
- MULTIFLOW - Turn on multiflow (0/1)
- NUM_APPS - Number of flows (N)
- FLOWIDS - Id for each flow ([0,1,2...])
- TXNODES - Transmission nodes ([18,12...] 0 is ALL)
- RXNODES - Receiving nodes ([1,2...])
- DELAY   - Delay each transmission (seconds)
- BRMIN   - Minimum bitrate (seconds)
- BRMAX   - Maximum bitrate (seconds)


### Where is everything?
- Core: */core/net/sdn/*
- Stack: */core/net/sdn/usdn/*
- Atom: */apps/atom/*
- Multiflow: */apps/multiflow/*


### Changes in usdn

- sdn-stats.c (Modified to calculate energy, RTT and latency)
- sdn-sink.c (added to support border router)
- Removed all atom related libraries for second part comparison.

###SDN-WISE setup.

- Move to cd/sdnwise/node
- Run the ./compile.sh make sure TRICKLE_ON is set to zero.
- Else you can find precompiled sink.z1 and node.z1 in cooja_firmwares
- Use command cd/tools/cooja && ant run to run Cooja simulator.
- Select the sink.z1 and node.z1 from "Add mote type" and then run the simulation.

### Parameters Varying
All parameters related things can be varied using sdn-wise.c source code
- RF_RECEIVE_U_EVENT - data rate for receiving unicast (bps)
- RF_SEND_U_EVENT - data rate for sending unicast (bps)
- RF_SEND_B_EVENT - data rate for sending broadcast (bps)
- RF_SEND_B_EVENT - data rate for sending broadcast (bps)
- conf.hops_from_Sink - Hop distance towards destination sink node.

###Changes in SDN-WISE
- adapter.c (Modified and added new control modules for embedded control logic)
- project_conf.h (Modified to support contikimac and CSMA/cA)
- statistc.c (Modified for energy calculation)

### ONOS connection with usdn
- Open your Linux terminal.
- Clone the ONOS repository.
- Build the ONOS repository using "bazel build onos".
- After building the ONOS, we will navigate to the folder where ONOS is installed ’cd onos’. Now we will activate ONOS controller
using command ’bazel run onos-local – clean -debug”. This will create a ONOS instance on your local machine.
- Now we need to invoke the command to open CLI (Command Line interface) to activate our ONOS application which will act as a adaptation between Cooja and ONOS controller. This is done by using creating new window of your current terminal and typing following command ”tools/test/bin/onos localhost’.
- Now in CLI type ’app activate org.onosproject.usdn’. This command activates our ONOS application.
- Now navigate to usdn folder which you cloned from git, open a terminal inside that folder and type command ’cd tools/cooja’ and then ’ant run’. This opens the Cooja simulator.
-  Now open new simulation and select the new motes. Add sink node and other nodes to your simulation as given in usdn git file. Right click on sink node and select SLIP client connection and keep port number as 9999 and click start.
- Now we need to create tunnel so as to connect ONOS and Cooja, this can be done by using tunslip6 utility of
cooja. Reopen your current terminal and navigate back using ’cd ..’. 
- Now open tunnel using ’sudo ./tunslip6 -v 2 -t tun0 -a localhost -p 9999 fd00::1/64’.
- Now click start from Cooja simulator and once the simulation starts, you can see logs getting updated in the log window.

### Where is eveything?
You will find details about usdn adapter application in two folders of onos.
- onos/protocols/usdn
- onos/providers/usdn




