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
unzip owl-linux-amd64-21.0.zip /owl
sudo cp -r owl /usr/lib/
```

We use kscript to run each benchmark.
So, you need to Install Kotlin and kscript.
For example, Kotlin and kscript can be installed with following command:
```sh
sdk install kotlin 1.9.24
sdk install kscript
```
To install kscript, see https://github.com/kscripting/kscript

## Install FalCAuN-with-SGA
If you successfully installed all the requirements, you can install FalCAuN-with-SGA with the commands:
```sh
cd FalCAuN
mvn clean install
```

## Preparation for running AT benchmarks


## Reproduce the results
You can reproduce the full results in the paper with the command:
```sh
./run-RERS.sh && ./run-AT-all.sh && ./run-CC-all.sh
```
- run-RERS.sh is a script to run all the benchmarks for RERS 60 times. It will take about 2 hours.
- run-AT-all.sh is a script to run all the benchmarks for AT 60 times. It will take about 300 hours.
- run-CC-all.shis a script to run all the benchmarks for CC 60 times. It will take about 60 hours.

Since runnning all the benchmarks for AT and CC takes long time, you can also reproduce the results of some selected benchmarks with the command:
```sh
./run-AT-selected.sh && ./run-CC-selected.sh
```
- run-AT-selected.sh is a script to run AT4' and AT7 3 times. It will take about 4 hours.
- run-CC-selected.sh is a script to run CC1, CC2 and CC6 3 times It will take about 1 hour.