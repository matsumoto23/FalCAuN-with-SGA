#!/bin/bash

get_avg(){
awk -F',' '
NR == 1 {
    for (i = 1; i <= NF; i++) {
        header[i] = $i
    }
    next
}
{
    for (i = 2; i <= NF; i++) { 
        sum[i] += $i
    }
    row_count++
}
END {
    if (row_count < 1) {
      printf "Always failed to falsify\n"
    } else {
      for (i = 2; i <= NF; i++) {
          if (header[i] == " num Fals.") {
            tmp_row_count = 3
          } else {
            tmp_row_count = row_count
          }
          printf "%s average: %.2f\n", header[i], sum[i] / tmp_row_count
      }
    }
}' $1
}

convert_to_seconds() {
    local time_str=$1
    local minutes=$(echo "$time_str" | grep -o '[0-9]\+m' | tr -d 'm')
    local seconds=$(echo "$time_str" | grep -o '[0-9.]\+s' | tr -d 's')
    minutes=${minutes:-0}
    seconds=${seconds:-0}
    echo "$minutes * 60 + $seconds" | bc
}

if [ $# -lt 1 ] || [ $1 == AT ]; then
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
  set +o pipefail
  echo "Written the results in data/AT"
  # print statistics
  for bench in data/AT/*
  do
    if [[ ${bench} =~ ^data/AT/AT([0-9 | \_]+)\.txt$ ]]; then
      echo "======AT${BASH_REMATCH[1]}======"
      echo "NOABS"
      cat $bench | grep -e method -e original | get_avg | cat
      echo "DISJSENSE"
      cat $bench | grep -e method -e partial | get_avg | cat
      echo "COARSEST"
      cat $bench | grep -e method -e abstract | get_avg | cat
    fi
  done
fi

if [ $# -lt 1 ] || [ $1 == CC ]; then
  # delete previous results
  if [ -d data/CC ]; then
    rm -rf data/CC
  fi
  # write results to data/CC
  mkdir -p data/CC
  set -o pipefail
  for log in FalCAuN/example/kotlin/CC/log/CC*/*.txt
  do
    if [[ ${log} =~ ^FalCAuN/example/kotlin/CC/log/CC([0-9 | \_]+)/([a-z]+)_([0-9]+)\.txt$ ]]; then
      if [ ! -f "data/CC/CC${BASH_REMATCH[1]}.txt" ]; then
        echo "method, num Fals., Total time, # of executions, Init. Time (ms), # of EQ" >> data/CC/CC${BASH_REMATCH[1]}.txt
      fi
      num_eq=$(cat $log | grep "Starting round" | tr " " "\n" | tail -n 1) && \
      num_ex=$(cat $log | grep "Number of simulations:" | tr " " "\n" | tail -n 1) && \
      num_false=$(cat $log | grep "cex output:" | wc -l) && \
      init_time=$(cat $log | grep "Initialization Time:" | tr " " "\n" | tail -n 1) && \
      time_str=$(cat $log | grep "real" | tr "	" "\n" | tail -n 1) && \
      total_time=$(convert_to_seconds "$time_str") && \
      arr=(${BASH_REMATCH[2]} $num_false $total_time $num_ex $init_time $(($num_eq-1)) ) && \
      echo "$(IFS=","; echo "${arr[*]}")" >> data/CC/CC${BASH_REMATCH[1]}.txt
    fi
  done
  echo "Written the results in data/CC"
  set +o pipefail
  # print statistics
  for bench in data/CC/*
  do
    if [[ ${bench} =~ ^data/CC/CC([0-9 | \_]+)\.txt$ ]]; then
      echo "======CC${BASH_REMATCH[1]}======"
      echo "NOABS"
      cat $bench | grep -e method -e original | get_avg | cat
      echo "DISJSENSE"
      cat $bench | grep -e method -e partial | get_avg | cat
      echo "COARSEST"
      cat $bench | grep -e method -e abstract | get_avg | cat
    fi
  done
fi

if [ $# -lt 1 ] || [ $1 == RERS ]; then
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
        echo "method, # of EQ , # of OQ, Init. Time (ms), Fals. Time (ms), Total time (sec)" >> data/RERS/m${BASH_REMATCH[1]}.txt
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
  echo "Written the results in data/RERS"
  set +o pipefail
  # print statistics
  for bench in data/RERS/*
  do
    if [[ ${bench} =~ ^data/RERS/m([0-9 | \_]+)\.txt$ ]]; then
      echo "======RERS/m${BASH_REMATCH[1]}======"
      echo "NOABS"
      cat $bench | grep -e method -e original | get_avg | cat
      echo "COARSEST"
      cat $bench | grep -e method -e abstract | get_avg | cat
    fi
  done
fi