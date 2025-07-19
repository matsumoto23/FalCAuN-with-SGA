mkdir ./FalCAuN/example/kotlin/AT/log/
mkdir ./FalCAuN/example/kotlin/CC/log/
mkdir ./FalCAuN/example/kotlin/WhiteboxMealy/log/

for bench_index in 1 2 2_2 3 4 4_2 5 6 7 8 9
do
  mkdir ./FalCAuN/example/kotlin/AT/log/AT$bench_index
done

for bench_index in {1..6}
do
  mkdir ./FalCAuN/example/kotlin/CC/log/CC$bench_index
done