import os
import argparse
import glob

runGroup="rg-a"
version="v2.2.9"
inputPath="/volatile/clas12/"
reconType="pass0"
monType="mon"
outputDir="fileLists/"#"~/clas12_monitoring/brandon/fileLists/"#"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/fileLists/"
outputMonDir="./"#"~/clas12_monitoring/brandon/"#"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/" 


parse = argparse.ArgumentParser(description="Script to create output directories for output of monitoring")
parse.add_argument('--inputDir', action = "store", help='directory location where hipo files are located', dest='inputDir', type=str)
results = parse.parse_args()
inputDir = results.inputDir

print "Generating list of files for each run to monitor"

runs=[]

#new way
# get the runs 
#directory_with_runs = "/volatile/clas12/rg-a/production/recon/pass0/calib/v2.2.15/mon/recon/"#"/volatile/clas12/rg-a/production/recon/pass0/calib/v2.2.1/mon/recon/"
runs = [x[0][-4:] for x in os.walk(inputDir)]  #directory_with_runs)]

run_list = open("run_list.txt", "w+")

for r in runs:

    #generate histogram folders for each run if they do not already exist
    print(' Generating necessary items for run {}'.format(r) )

    if not os.path.exists(outputMonDir+"plots"+r):
        print outputMonDir+"plots"+r + " does not exist --> Creating directory"
        os.mkdir(outputMonDir+"plots"+r)

    f_counter=0
    
    if os.path.exists(inputDir+'00'+r):
        file_list=open(outputDir+"file_list_r00"+r+".txt","w")

        n_files = os.listdir(inputDir+'00'+r)
        print " --> Number of files found in directory: %d " % (len(n_files))

        if len(n_files) >= 1 :#and len(n_files) < 15:

            temp_file_list = glob.glob(os.path.join(inputDir+'00'+r,"*.hipo"))
            temp_file_list.sort(key=lambda f: int(filter(str.isdigit, os.path.basename(f))))
            print(temp_file_list)
            run_list.write("%s\n" % r)
            for f in temp_file_list:
                if f_counter < 900:
                    file_list.write(f+'\n')
                f_counter=f_counter+1
        else:
            print '--> Error: run %s does not have more than 1 files ' % (r)

        file_list.close()
    

run_list.close()
print "complete"
