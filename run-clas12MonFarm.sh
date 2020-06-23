#!/bin/bash

#set coatjava env
coatjava_version=coatjava-6.5.3
#on feb 21 updates use of coatjava libraries to 6b51
#clara4p4-6bp2

#export COATJAVA="/work/clas12/rg-a/software/${coatjava_version}/plugins/clas12/"
#setenv COATJAVA /work/clas12/rg-a/software/clara-5bp7p8/plugins/clas12/

cp -r /w/hallb-scifs17exp/clas12/rg-a/software/${coatjava_version} .


#compile scripts
#javac -cp "${coatjava_version}/plugins/clas12/lib/clas/*:${coatjava_version}/plugins/clas12/lib/utils/*:." ana_2p2.java
javac -cp "${coatjava_version}/lib/clas/*:${coatjava_version}/lib/utils/*:." ana_2p2.java



# INPUTS
run=$1 
input_file_list=$2

echo "[shell] >> MAKING DIRECTORY FOR PLOTS "
mkdir plots${run}/

echo "[shell] >>  EXECUTING FOR SINGLE RUN: " $run 


max_num_events=100000000
n_files=`cat ${input_file_list} | wc -l`
if [ $n_files -gt 10 ]
then
    max_num_events=10000000000
fi
echo "[shell] >> MAX EVENTS TO PROCESS " ${max_num_events}

beam_energy=10.6
echo $run

if [ $run -gt 3861 -a $run -lt 5674 ]
then
    beam_energy=10.6
    echo " [shell] >> SETTING BEAM ENERGY TO  " $beam_energy
fi

if [ $run -gt 5673 -a $run -lt 5871 ]
then
    beam_energy=7.546
    echo " [shell] >> SETTING BEAM ENERGY TO " $beam_energy
fi

if [ $run -gt 5870 -a $run -lt 6001 ]
then
    beam_energy=6.5
    echo " [shell] >> SETTING BEAM ENERGY TO  " $beam_energy
fi

if [ $run -gt 6607 -a $run -lt 6784 ]
then
    beam_energy=10.2
    echo " [shell] >> SETTING BEAM ENERGY TO  " $beam_energy
fi

echo " [shell] >> BEAM ENERGY IS  " $beam_energy " FOR RUN " ${run}

echo " [shell] >> RUN ana_2p2 "
java -DCLAS12DIR="${coatjava_version}/" -cp "${coatjava_version}/lib/clas/*:${coatjava_version}/lib/utils/*:." ana_2p2 $run $input_file_list $max_num_events $beam_energy

