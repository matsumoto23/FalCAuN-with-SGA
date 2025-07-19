#!/bin/bash

for index in 1 2 2_2 3 4 4_2 5 6 7 8 9
do
  for mode in original partial abstract
  do
    for iteration in {1..60}
    do
      echo "===================================="
      echo "$mode"_AT"$index"_iteration:"$iteration"
      echo "===================================="
      (time (timeout 1260 ./AT$index.kts $mode)) | tee ./log/AT"$index"/"$mode"_"$iteration".txt
    done
  done
done