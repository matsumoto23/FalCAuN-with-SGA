
 
for index in 4_2 7
do
  for mode in original partial abstract
  do
    for iteration in {1..3}
    do
      echo "===================================="
      echo "$mode"_stl"$index"_iteration:"$iteration"
      echo "===================================="
      (time (timeout 1260 ./stl$index.kts $mode)) | tee ./log/AT"$index"/"$mode"_"$iteration".txt
    done
  done
done