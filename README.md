We tested on Debian 12 bookworm

# Requirements
The requirements for FalCAuN-with-SGA are as follows.
- Java 11
- Maven
- LTSmin 3.1.0
- Owl 21.0
- MATLAB/Simulink R2024a
- Kotlin 1.9.24
- kscript

# Installation

## Install the requirements
You need to install all the requirements above.
For example, Java 11 and Maven can be installed with the command:
```sh
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install java 11.0.27-amzn
sdk install maven
```

You have to manually install LTSmin, Owl and MATLAB/Simulink.
For example, LTSmin and Owl can be installed with the commands:
```sh
wget https://github.com/Meijuh/ltsmin/releases/download/v3.1.0/ltsmin-v3.1.0-linux.tgz
tar xvf ltsmin-v3.1.0-linux.tgz
sudo install v3.1.0/bin/* /usr/local/bin/

wget https://github.com/owl-toolkit/owl/releases/download/release-21.0/owl-linux-amd64-21.0.zip
unzip owl-linux-amd64-21.0.zip -d owl
sudo cp -r owl /usr/lib/
```

We use kscript to run each benchmark.
So, you need to Install Kotlin 1.9.24 and kscript.
For example, Kotlin and kscript can be installed with following commands:
```sh
sdk install kotlin 1.9.24
sdk install kscript
```

## Install FalCAuN-with-SGA
If you successfully installed all the requirements, you can build and install FalCAuN-with-SGA with the commands:
```sh
cd FalCAuN
mvn clean install
```

## Setup AT environment
Before running the scripts below, run openExample('simulink_automotive/ModelingAnAutomaticTransmissionControllerExample') in MATLAB. A window of the AT model will open.

## List of scripts
|Name|Short description|Estimated execution time|
|:---|:---|---:|
|install.sh|create directory for log|1 sec|
|run-RERS.sh|run all the benchmarks for RERS 60 times|2 hours|
|run-AT-all.sh|run all the benchmarks for AT 60 times|300 hours|
|run-CC-all.sh|run all the benchmarks for CC 60 times|60 hours|
|run-AT-selected.sh|run AT4' and AT7 3 times|4 hours|
|run-CC-selected.sh|run CC1, CC2 and CC6 3 times|1 hour|

## Reproduce the results
First, you need to run install script to create directories for log files:
```sh
./install.sh
```

Then, you can reproduce the full results in the paper with the command:
```sh
./run-RERS.sh && ./run-AT-all.sh && ./run-CC-all.sh
```

Since runnning all the benchmarks for AT and CC takes long time, you can also reproduce the results of some selected benchmarks with the command:
```sh
./run-AT-selected.sh && ./run-CC-selected.sh
```

## Run each benchmark
Each benchmark in [FalCAuN/example/kotlin/AT](/FalCAuN/example/kotlin/AT) and [FalCAuN/example/kotlin/CC](/FalCAuN/example/kotlin/CC) can be run with the command:
```sh
./bench_name.kts mode
```
The variable mode must be one of 'original', 'partial', 'abstract' corresponding to the method NOABS, DISJSENSE, COARSEST in the paper, respectively.
For example, you can run AT1 with COARSEST with the following command:
```sh
./AT1.kts abstract
```

## Run AT/CC with other specifications
To run falsification of AT/CC models with other specifications, please refer to [Tutorial for AT benchmarks](/FalCAuN/example/kotlin/AT/tutorial.md) and change the initiallization of input/output mappers and STL properties.

## Description of implementaion of specification-guided abstraction
