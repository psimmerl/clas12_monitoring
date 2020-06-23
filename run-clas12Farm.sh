#!/bin/bash

inputRunList=$1

while read F  ;
do
    RUN=$F
		RUN=$( expr $(echo $F |  cut -d '/' -f12) + 0 )
    
		echo $RUN

    python run-genMonJobs.py --run ${RUN} --inputFileList ./fileLists/file_list_r00${RUN}.txt --outDir ./plots${RUN}/ --savePngs True
#/work/clas12/rg-a/software/clas12_monitoring/fileLists/file_list_r00${RUN}.txt --outDir /work/clas12/rg-a/software/clas12_monitoring/plots${RUN}/ --savePngs True

done <${inputRunList}
