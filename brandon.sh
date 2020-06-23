#!/usr/bin/bash

echo "Starting"

script_dir=. #/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/farm

inputDir=/volatile/clas12/rg-a/production/recon/pass0/calib/v2.2.15/mon/recon/

mkdir -p jobs/
mkdir -p fileLists/

python $script_dir/run-genMonList.py --inputDir $inputDir

echo "Finished run-genMonList.py"

#run_list=

for f in ./fileLists/*.txt
do
	./run-clas12Farm.sh $f
done

echo "Finished run-clas12Farm.sh"

#./run-clas12Farm.sh $run_list

python run-submit.py run_list.txt

echo "Finished run-submit"


#watch squeue -u psimmerl



