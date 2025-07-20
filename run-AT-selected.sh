#!/bin/bash

cd ./FalCAuN/example/kotlin/AT/
./run-selected.sh
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
if [ -d data/AT ]; then
  rm -rf data/AT
fi

# write results to data/AT
mkdir -p data/AT

set -o pipefail

for log in FalCAuN/example/kotlin/AT/log/AT*/*.txt
do
  if [[ ${log} =~ ^FalCAuN/example/kotlin/AT/log/AT([0-9 | \_]+)/([a-z]+)_([0-9]+)\.txt$ ]]; then
    if [ ! -f "data/AT/AT${BASH_REMATCH[1]}.txt" ]; then
      echo "method, num Fals., Total time (sec), # of executions, Init. Time (ms), # of EQ" >> data/AT/AT${BASH_REMATCH[1]}.txt
    fi
    num_eq=$(cat $log | grep "Starting round" | tr " " "\n" | tail -n 1) && \
    num_ex=$(cat $log | grep "Number of simulations:" | tr " " "\n" | tail -n 1) && \
    num_false=$(cat $log | grep "cex output:" | wc -l) && \
    init_time=$(cat $log | grep "Initialization Time:" | tr " " "\n" | tail -n 1) && \
    time_str=$(cat $log | grep "real" | tr "	" "\n" | tail -n 1 ) && \
    total_time=$(convert_to_seconds "$time_str") && \
    arr=(${BASH_REMATCH[2]} $num_false $total_time $num_ex $init_time $(($num_eq-1)) ) && \
    echo "$(IFS=","; echo "${arr[*]}")" >> data/AT/AT${BASH_REMATCH[1]}.txt
  fi
done

echo "Written the results in data/AT"

./show-result.sh AT

set +o pipefail