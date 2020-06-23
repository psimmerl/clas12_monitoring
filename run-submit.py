import os, sys
import glob

#add input commands later
# for example run number, run ranges, or run list

rootdir="./jobs/" #"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/farm/jobs/"
input_files_to_process=sys.argv[1]


runs = open(input_files_to_process,"r")
run_list =[];
for rr in runs:
    run_list.append(rr[:-1])

for subdir, dirs, files in os.walk(rootdir):
    for file in files:
        #print os.path.join(subdir, file)
        filepath = subdir + os.sep + file


        for r in run_list:
            if file.endswith(".jsub") and ("clas12Mon"+str(r) in file):
                print 'Submitting file %s :'  % (file)
                os.system("jsub %s" % filepath)
            
