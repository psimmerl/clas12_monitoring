#!/usr/bin/bash
script_dir=. #/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/farm

inputDir=/volatile/clas12/rg-a/production/recon/pass0/calib/v2.2.15/mon/recon/

mkdir -p jobs/
mkdir -p fileLists/

python $script_dir/run-genMonList.py --inputDir $inputDir

#run_list=

for f in ./fileLists/*.txt
do
	./run-clas12Farm.sh $f
done

#./run-clas12Farm.sh $run_list

for f in ./fileListsfl_r*.txt
do
	python run-submit.py $f
done




