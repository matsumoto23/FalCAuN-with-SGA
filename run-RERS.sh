#!/bin/bash

# run all benchmarks
cd ./FalCAuN/example/kotlin/RERS/
./run.sh
cd -

convert_to_seconds() {
    local time_str=$1
    local minutes=$(echo "$time_str" | grep -o '[0-9]\+m' | tr -d 'm')
    local seconds=$(echo "$time_str" | grep -o '[0-9.]\+s' | tr -d 's')
    minutes=${minutes:-0}
    seconds=${seconds:-0}
    echo "$minutes * 60 + $seconds" | bc
}

# delete previous results
if [ -d data/RERS ]; then
  rm -rf data/RERS
fi

# write results to data/RERS
mkdir -p data/RERS

set -o pipefail

for log in FalCAuN/example/kotlin/RERS/log/*.txt
do
  if [[ ${log} =~ ^FalCAuN/example/kotlin/RERS/log/m([0-9]+)_([a-z]+)_([0-9]+)\.txt$ ]]; then
    if [ ! -f "data/RERS/m${BASH_REMATCH[1]}.txt" ]; then
      echo "method, # of OQ , # of EQ, Init. Time (ms), Fals. Time (ms), Total time (sec)" >> data/RERS/m${BASH_REMATCH[1]}.txt
    fi
    num_eq=$(cat $log | grep "Starting round" | tr " " "\n" | tail -n 1) && \
    num_oq=$(cat $log | grep "# of MQ:" | tr " " "\n" | tail -n 1) && \
    init_time=$(cat $log | grep "Time for initialize:" | tr " " "\n" | tail -n 1) && \
    falsif_time=$(cat $log | grep "Time for falsification:" | tr " " "\n" | tail -n 1) && \
    time_str=$(cat $log | grep "real" | tr "	" "\n" | tail -n 1) && \
    total_time=$(convert_to_seconds "$time_str") && \
    arr=(${BASH_REMATCH[2]} $num_eq $num_oq $init_time $falsif_time $total_time) && \
    echo "$(IFS=","; echo "${arr[*]}")" >> data/RERS/m${BASH_REMATCH[1]}.txt
  fi
done

./show-result.sh RERS

echo "Written the results in data/RERS"

set +o pipefail