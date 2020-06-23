import glob
import os
import sys, getopt
import argparse
import json
import subprocess


default_png_list="/work/clas12/rg-a/software/clas12_monitoring/farm/png_list.txt"
default_hipo_list="/work/clas12/rg-a/software/clas12_monitoring/farm/hipo_list.txt"

parse = argparse.ArgumentParser(description="Script to create jobs submission files for clas12Mon")
parse.add_argument('--run', action = "store", help='run number for generating plots', dest='run', type=int)
parse.add_argument('--inputFileList', action = "store", help='list of files for run to submit for processing', dest='inputFileList', type=str)
parse.add_argument('--outDir', action = "store", help='directory to place the output files from clas12Mon', dest='outDir', type=str)
parse.add_argument('--pngFileList', action = "store", help='file of png names', dest='pngFileList', type=str, default=default_png_list)
parse.add_argument('--hipoFileList', action = "store", help='file of hipo names', dest='hipoFileList', type=str,default=default_hipo_list)
parse.add_argument('--savePngs', action = "store", help='save png files?', dest='savePngs', type=bool, default=False)

print 'Arguments passed in are'
print parse.parse_args()

results=parse.parse_args()

run = results.run
inputFileList = open(results.inputFileList,"r")
outDir = results.outDir
pngFileList = results.pngFileList
hipoFileList = results.hipoFileList
savePngs = results.savePngs

file_list=[]
for ff in inputFileList:
    file_list.append(ff)

n_files = len(open(results.inputFileList).readlines(  ))
n_files_per_job=10
n_jobs=1

if n_files > 10 :
    n_jobs = n_files/10

# break list of files into groups for each job
group_file_list= [file_list[i * n_files_per_job:(i + 1) * n_files_per_job] for i in range((len(file_list) + n_files_per_job - 1) // n_files_per_job )]  

print ' Need to make %d jobs for %d ' % (n_jobs, n_files)
min_file=0
max_file=10

javaFileList="/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/farm/java_list.txt"
input_java_list = open(javaFileList,"r")
jc_list = []
for jc in input_java_list:
    jc_list.append(jc)

output_png_file = open(pngFileList)
output_png_list=[]
for png_name in output_png_file:
    print ' Adding ' + png_name + ' to output list'
    output_png_list.append(png_name)

output_hipo_file = open(hipoFileList)
output_hipo_list=[]
for hipo_name in output_hipo_file:
    print ' Adding ' + hipo_name + ' to output list'
    temp_hipo_name = hipo_name.replace("RUN",str(run))
    print temp_hipo_name
    output_hipo_list.append(temp_hipo_name)

print "CREATING " + str(n_jobs) + "JOBS"

for j in range(0,n_jobs):
    print ' creating jsub for job %d ' % (j)

    #other ways to do this but it works right now.
    temp_file_list_to_process_dir = "./fileLists"#"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/farm/fileLists/"
    temp_file_list_to_process_name = "fl_r"+str(run)+"_set"+str(j)+".txt"
            
    jsub_script = open("./jobs/jsub_script_clas12Mon"+str(run)+"_set"+str(j)+".jsub","w")#"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/farm/jobs/jsub_script_clas12Mon"+str(run)+"_set"+str(j)+".jsub","w")

    jsub_script.write("JOBNAME:  RGA_MON_" + str(run) + "j"+ str(j) + " \n")
    #jsub_script.write("OS:       centos7"+ " \n")
    jsub_script.write("TRACK:    analysis"+ " \n")
    jsub_script.write("MEMORY:   2 GB"+ " \n")
    jsub_script.write("PROJECT:  clas12"+ " \n")    
    jsub_script.write("COMMAND: ./run-clas12MonFarm.sh " + str(run) + " " + temp_file_list_to_process_name + " \n" )
    jsub_script.write("OTHER_FILES: "+ " \n")    
    jsub_script.write("run-clas12MonFarm.sh"+"\n")#"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/run-clas12MonFarm.sh"+ " \n")
    jsub_script.write(temp_file_list_to_process_dir+"fl_r"+str(run)+"_set"+str(j)+".txt" + " \n")

    for java_class in jc_list:
        jsub_script.write("./"+ java_class[:-1]+" \n")#"/w/hallb-scifs17exp/clas12/rg-a/software/clas12_monitoring/" + java_class[:-1] + " \n" )    
    
    for out_hipo_file in output_hipo_list:
        jsub_script.write("OUTPUT_DATA: plots" +str(run) +"/" + out_hipo_file[:-1] + " \n") # the [:-1] is because the txt has a new line character that I want to remove 
        jsub_script.write("OUTPUT_TEMPLATE: " + outDir + "set"+str(j)+"_"+out_hipo_file[:-1] + " \n")

    if( savePngs ):
        for out_png_file in output_png_list:
            jsub_script.write("OUTPUT_DATA: plots" +str(run) +"/" + out_png_file[:-1] + " \n")
            jsub_script.write("OUTPUT_TEMPLATE: " + outDir + "set"+str(j)+"_"+out_png_file[:-1] + " \n")


    jsub_script.write("INPUT_FILES: "+ " \n")

    temp_file_list_to_process = open(temp_file_list_to_process_dir+temp_file_list_to_process_name,"w")
    for f in group_file_list[j]:        
        temp_file_list_to_process.write(f[:-1] + " \n")
        jsub_script.write(f[:-1] + " \n")

    jsub_script.write("SINGLE_JOB: true \n")

    min_file=min_file+10;
    max_file=max_file+10
        
