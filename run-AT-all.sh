#!/bin/bash

# run all AT benchmarks 
cd ./FalCAuN/example/kotlin/AT/
./run-all.sh
cd -

./show-result.sh AT
