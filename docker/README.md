Docker
======

This directory contains `Dockerfile` to run FalCAuN-with-SGA on Docker. We test this environment on Windows 11/x64.

How to build
------------

You can build the docker image as follows.

```sh
docker build -t falcaun-with-sga .
```

How to use this image
---------------------

### Start a docker container

First, start a docker container as follows.

```sh
docker run --rm --shm-size=512M -p 5901:5901 -p 6080:6080 -it falcaun-with-sga -vnc
```

### Connect to the VNC environment

Then, connect to the container via VNC. We have the following two methods.

- If you use a VNC client, connect to display 1 (or port 5901).
- If you do not use a VNC client, open http://localhost:6080 with a browser.

If you are asked to enter a password, please type `matlab`.

### Open MATLAB and activate

Click the MATLAB icon on the desktop and activate the MATLAB license.

### Enable MATLAB engine

Then, execute `matlab.engine.shareEngine` to share this MATLAB session with FalCAuN-with-SGA.

### Set up the environment for the benchmarks

To set up the environment for the benchmarks, run the following commands in MATLAB.
A window of the AT model will open.

- `addpath('/home/matlab/FalCAuN-with-SGA/FalCAuN/example/kotlin/AT')`
- `addpath('/home/matlab/FalCAuN-with-SGA/FalCAuN/example/kotlin/CC')`
- `openExample('simulink_automotive/ModelingAnAutomaticTransmissionControllerExample')`

### Run the scripts

Finally, you can run the Kotlin scripts. Open a terminal at the bottom of the desktop. Move to the directory containing examples of FalCAuN-with-SGA with `cd /home/matlab/FalCAuN-with-SGA/example/kotlin` and run a script, e.g., `./mealy-nox.main.kts` or `./ATS1.main.kts`. Most of the example scripts assumes that the current directory of MATLAB session is the directory of the script. Therefore, you also have to run `cd /home/matlab/FalCAuN-with-SGA/FalCAuN/example/kotlin/AT` or `cd /home/matlab/FalCAuN-with-SGA/FalCAuN/example/kotlin/CC` in MATLAB.

### Run on macOS
Although this `Dockerfile` can be built and run on macOS with Apple Silicon, some trouble may occur.
Below, we have listed possible problems to help you solve them.

- When the container exits immediately after startup, try a different type of VMM.
  - The setting on VMM can be found in `Settings > General > Virtual Machine Options` in Docker Desktop.
- When you can not start MATLAB GUI, please try to
  - Run MATLAB with the command `matlab -nodesktop -nosplash` in the shell where Docker is running.
  - Then, activate the MATLAB license.
  - Then, run `addpath('/home/matlab/FalCAuN-with-SGA/FalCAuN/example/kotlin/AT')`, `addpath('/home/matlab/FalCAuN-with-SGA/FalCAuN/example/kotlin/CC')`, and `matlab.engine.shareEngine` in MATLAB.
  - Finally, follow the instructions to run the scripts.
