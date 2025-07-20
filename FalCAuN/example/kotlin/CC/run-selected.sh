for index in 1 2 6
do
  for mode in original partial abstract
  do 
    for iteration in {1..3}
    do
      echo "===================================="
      echo "$mode"_CC"$index"_iteration:"$iteration"
      echo "===================================="
      { time timeout 1260 ./CC$index.main.kts $mode 1; } |& tee ./log/CC"$index"/"$mode"_"$iteration".txt
    done
  done
done