# FalCAuN-with-SGA
We implemented black-box checking with specification-guided abstraction(SGA) by extending [FalCAuN](https://github.com/MasWag/FalCAuN), a toolkit for testing black-box systems. See [this section](#Implementation-of-SGA) for the details of implementation.

## Installation
We tested on Debian 12 bookworm.

### Requirements
The requirements for FalCAuN-with-SGA are as follows.
- Java 11
- Maven
- LTSmin 3.1.0
- Owl 21.0
- MATLAB/Simulink R2024a
- Kotlin 1.9.24
- kscript

### 1. Install the requirements
You need to install all the requirements above.
For example, Java 11 and Maven can be installed with the command:
```sh
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install java 11.0.27-amzn
sdk install maven
```

You have to manually install LTSmin, Owl, and MATLAB/Simulink.
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
So, you need to install Kotlin 1.9.24 and kscript.
For example, Kotlin and kscript can be installed with the following commands:
```sh
sdk install kotlin 1.9.24
sdk install kscript
```

#### 2. Setup the environment variable

We assume that the environment variable `MATLAB_HOME` shows where MATLAB is installed. An example is as follows.

```sh
export MATLAB_HOME=<path/to/matlab/home>
## Example:
# export MATLAB_HOME=/usr/local/MATLAB/R2024a/
```

### 3. Build and Install FalCAuN-with-SGA
If you successfully installed all the requirements, you can build and install FalCAuN-with-SGA with the commands:
```sh
cd FalCAuN
mvn clean install
```

### Installation of LTSMin 3.1.0 on macOS with ARM Processors

FalCAuN works on macOS with ARM Processors, but the setup of LTSMin is a bit tricky because it only supports `x86_64`. One can still run LTSMin using Rosetta and libtool for `x86_64`.

1. Set up Rosetta on the macOS
2. Install Homebrew for intel processors with `arch -x86_64 /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)"`
3. Install `libtool` for `x86_64` with `/usr/local/bin/brew install libtool`

### Notes
Make sure that you have a compiler for Simulink.
- This can be checked by running `mex -setup` in MATLAB.
  - If no compiler was found, you need to install. For example, you can install gcc, C++, and FORTRAN with the command `sudo apt install build-essential`.

## Set up the environment for benchmarks
Before running the scripts below, add paths to the directories of AT and CC models to MATLAB and run `openExample('simulink_automotive/ModelingAnAutomaticTransmissionControllerExample')` in MATLAB. A window of the AT model will open.

You can add paths to models by running the following commands in MATLAB.
```matlab
addpath('<path/to/this/repository>/FalCAuN/example/kotlin/AT')
addpath('<path/to/this/repository>/FalCAuN/example/kotlin/CC')
savepath
```

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
First, you need to run the script to create directories for log files:
```sh
./install.sh
```

Then, you can reproduce the full results in the paper with the command:
```sh
./run-RERS.sh && ./run-AT-all.sh && ./run-CC-all.sh
```

Since running all the benchmarks for AT and CC takes a long time, you can also reproduce the results of some selected benchmarks with the command:
```sh
./run-AT-selected.sh && ./run-CC-selected.sh
```

## Run each benchmark
You can also run each benchmark in [FalCAuN/example/kotlin/AT](/FalCAuN/example/kotlin/AT) and [FalCAuN/example/kotlin/CC](/FalCAuN/example/kotlin/CC) with the command:
```sh
./bench_name.kts mode
```
The variable `mode` must be one of `'original'`, `'partial'`, `'abstract'`, corresponding to the method NOABS, DISJSENSE, COARSEST in the paper, respectively.
For example, you can run AT1 with COARSEST with the following command:
```sh
./AT1.kts abstract
```

## Run AT/CC with other specifications
To run falsification of AT/CC models with other specifications, please refer to [Tutorial for AT benchmarks](/FalCAuN/example/kotlin/AT/tutorial.md) and change the initialization of input/output mappers and STL properties.

## Run on Docker
We provide a `Dockerfile` to run FalCAuN-with-SGA in [/docker](/docker).
Follow [these instructions](/docker/README.md) to run benchmarks on Docker.

## Implementation of SGA
SGA is implemented as a mapper for a system.
[`OutputMapper`](/FalCAuN/core/src/main/java/net/maswag/falcaun/OutputMapper.java) is a class of a mapper for a Mealy machine provided as an instance of `MealySimulatorSUL`, and [`OutputEquivalence`](/FalCAuN/core/src/main/java/net/maswag/falcaun/OutputEquivalence.java) is a class of a mapper for a MATLAB/Simulink system.

You can enable SGA by setting a suitable mapper to a system.
- For a Mealy machine provided as an instance of `MealySimulatorSUL`, you can set a mapper by
  - constructing a mapper with `val outputMapper = OutputMapper(ltlList, sigma, gamma, false)`, and then
  - setting a mapper with `val mappedSUL = MappedSUL(outputMapper, sul)`
- For MATLAB/Simulink models, you can set a mapper by replacing `NumericSULMapper(inputMapper, outputMapperReader.largest, outputMapperReader.outputMapper, signalMapper)` with `OutputEquivalence(inputMapper, outputMapperReader.largest, outputMapperReader.outputMapper, signalMapper, stlList, flag)`

The COARSEST is used when the `flag` is `true`, and the DISJSENSE is used when the `flag` is `false`.
See [FalCAuN/example/kotlin/simple_mealy](/FalCAuN/example/kotlin/simple_mealy) as an example for a Mealy machine, and [FalCAuN/example/kotlin/AT](/FalCAuN/example/kotlin/AT) as an example for a MATLAB/Simulink system.