#!/usr/bin/bash

threads=1
export COATJAVA="/group/clas12/packages/coatjava/6.3.1"

# Make directory for version
version=4 #"19_3"
export plots_dir="./v$version"
#mkdir -p $ver_dir
#cd $ver_dir

#javac -cp "$COATJAVA/lib/clas/*:$COATJAVA/lib/utils/*:." ana2p2.java

#IFS=' ' read -r -a files <<< "$@"

cat "$@" | xargs -I myfile -P $threads bash -c '
	file=myfile
	IFS="/" read -r -a run_split <<< $file
	run_num=${run_split[-1]}

	list=list_"$run_num".txt
	echo $file/*.hipo | tr " " "\n" > $list

	mkdir -p plots$run_num

	java -d64 -Xms256m -Xmx512M -DCLAS12DIR="$COATJAVA" -cp "$COATJAVA/lib/clas/*:$COATJAVA/lib/utils/*:." ana_2p2 $run_num $list 10000000 10.6

	rm -rf "$plots_dir/plots$run_num"
	rm $list
	cp -r "plots$run_num" "$plots_dir" #wouldnt this just rewrite over plots we want to keep???
' sh
