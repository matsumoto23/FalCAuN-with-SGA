for index in 24 45 54 55 76 95 135 158 159 164 172 181 183 185 201
do
  for mode in original abstract
  do 
    for iteration in {1..60}
    do
      echo "===================================="
      echo "$mode"_m"$index":"$iteration"
      echo "===================================="
      time (./mealy_falsification.kts $mode $index 9) | tee ./log/m"$index"_"$mode"_"$iteration".txt
    done
  done
done