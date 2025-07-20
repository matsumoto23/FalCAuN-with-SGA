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
            tmp_row_count = 60
          } else {
            tmp_row_count = row_count
          }
          printf "%s average: %.2f\n", header[i], sum[i] / tmp_row_count
      }
    }
}' $1
}

if [ $# -lt 1 ] || [ $1 == AT ]; then
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