#!c:\python27\python

import os
import sys
import datetime
import time
import webbrowser
import urllib
import optparse
import subprocess
import zipfile
import tempfile
import shutil
import glob

def parse_args(argv):
    prog_dir = os.path.dirname(argv[0])
    usage = """
Usage: %s [OPTION] 
Collect ePubStats on the ePub files in the target directory
"""[1:-1] % os.path.basename(argv[0])

    parser = optparse.OptionParser(usage=usage)
    parser.add_option("-n", "--newerDir", dest="newerDir", type="str",
                      help="Directory holding the latest versions of the ePub versions to compare")
    parser.add_option("-o", "--olderDir", dest="olderDir", type="str",
                      help="Directory holding the older version of the ePubs being compared")
    parser.add_option("-p", "--preserveDiffs", action="store_true", dest="saveDiffs", default=False,
                       help=r"Use this flag to save the differences file in the --diffLogDir directory")
    parser.add_option("--diffLogDir", dest="diffsDir", type="str", default=r"diffLogs",
                      help=r"If --preserveDiffs is specified, the diffs are stored in the directory 'diffLogs' unless overriden by the value of this option.")
    parser.add_option("--logdir", dest="logdir", type="str",
                      default=r"$EPUBCHECK-LOGS",
                      help=r"Log file location used by this tool, default=%EPUBCHECK-LOGS%\CompareLog.TabDelimitedFile; if EPUBCHECK-LOGS is not defined, the file will be written to the current working directory")
    parser.add_option("--logfile", dest="logfile", type="str",
                      default=r"CompareEpubsLog.TabDelimitedFile",
                      help=r"Log file name used by this tool, default=CompareEpubsLog.TabDelimitedFile")

    opts,args = parser.parse_args(argv[1:])
    return opts,args


def logStats(log_file, elapsedTime, newerFile, olderFile, zipsDiffer, nDiffs):
    
    now = datetime.datetime.now()
    dateTime = str(now.date()) + "\t" + str(now.time())
    
    if not os.path.exists(log_file):
        print "File " + log_file + " doesn't exist, inititalizing file..."
        f = open(log_file, 'a')
        f.write("logDate\t")
        f.write("logTime\t")
        f.write("elapsedTime\t")
        f.write("newerFile\t")
        f.write("olderFile\t")
        f.write("zipsDiffer\t")
        f.write("nDiffs\n")
    else:
        f = open(log_file, 'a')

    f.write(str(now.date()) + "\t" + str(now.time()) + "\t")
    f.write(elapsedTime + "\t")
    f.write(newerFile.rstrip("\r") + "\t")
    f.write(olderFile.rstrip("\r") + "\t")
    f.write(zipsDiffer + "\t")
    f.write(nDiffs + "\n")
    f.close()

    return "ePubStats Logging complete..."

global gopts
gopts,args = parse_args(sys.argv)

    #
    # verify that the log dir and file are writable
    #
if not os.path.isdir(os.path.expandvars(gopts.logdir)):
   print ('Activity logging environment variable "' + gopts.logdir + '" is not a valid dir, logging to the current working directory')
   statsLog = os.path.join(".", gopts.logfile)
else:
   statsLog = os.path.join(os.path.expandvars(gopts.logdir), gopts.logfile)
print ("Activity logging is being performed to " + statsLog)

olderDir = gopts.olderDir
newerDir = gopts.newerDir

print("B&N CompareEpubVersions Tool")
print("  Compare two directory trees holding differing versions of the same ePubs")
print("--\n")
print('Comparing the newer ePub versions in "' + newerDir + '" to older versions in "' + olderDir +'"')
print("--\n")

newerFiles = os.listdir(newerDir)
olderFiles = os.listdir(olderDir)
newCount = len(newerFiles)

if newCount != len(olderFiles):
    print('  Warning: The number of files in "' + newerDir + '" (' + str(len(newerFiles)) + ') does not match the number in "' + olderDir + '" (' + str(len(olderFiles)) + ')')

diffCmd = "diff -q -w -r "
nCompared = 0
nChecked = 0

for file in newerFiles:
    startTime = time.time()
    nChecked += 1
    if os.path.splitext(file)[-1].lower() != ".epub":
        print ("  File #" + str(nChecked) + " (of " + str(newCount) + ") " + str(nChecked) + ": " + file + " is not an ePub, skipped...\n--\n")
        continue

    nCompared += 1

    ean = file[0:13]
    olderTarget = glob.glob(os.path.join(olderDir, ean + "*.epub"))
    if len(olderTarget) == 0:
        print("  Error: no older version of " + os.path.join(newerDir, file) + " was found in " + olderDir)
        logStats(statsLog, "NA", os.path.join(newerDir, file), olderDir, str(False), "Older File Not Found")
        nCompared -= 1
        continue

    if len(olderTarget) > 1:
        print("  Warning: " + str(len(olderTarget)) + " ePubs found matching the target ean: " + ean)
    # print("File: " + file + " ean: " + ean + " olderTarget: " + str(olderTarget))

    newTmpBookDir = tempfile.mkdtemp(prefix="CePubV-Newer")
    oldTmpBookDir = tempfile.mkdtemp(prefix="CePubV-Older")

    newZipFile = os.path.join(newerDir, file)
    oldZipFile = olderTarget[0]

    tmpOutputFile = os.path.join(newerDir, ean + ".compare.log")
    tmpOutput = open(tmpOutputFile, mode="w")
    cmdStr = diffCmd + '"' + newZipFile + '" "' + oldZipFile + '"'
    print("  Comparing the zips themselves: " + cmdStr)
    subprocess.call(cmdStr, stdout=tmpOutput, stderr=tmpOutput, shell=True)
    tmpOutput.close()
    tmpOutput = open(tmpOutputFile, mode="r")
    theDiffs = tmpOutput.readlines()
    tmpOutput.close()
    if len(theDiffs) == 0:
        zipsDiffer = False
        nDiffs = 0
        print("  File #" + str(nChecked) + " (of " + str(newCount) + ") (" + str(nCompared) + " compared) - " + file + ": both ePub container files are identical")
    else:
        zipsDiffer = True
        print("  File #" + str(nChecked) + " (of " + str(newCount) + ") (" + str(nCompared) + " compared) - " + file + ": ePub container files differ")
        for line in theDiffs:
            print("     Zip Diff: " + line.rstrip())
    #
    # if they are different, lets see how different
    #
    if zipsDiffer:
        newZip = zipfile.ZipFile(newZipFile, 'r')
        oldZip = zipfile.ZipFile(oldZipFile, 'r')

        newZip.extractall(newTmpBookDir)
        oldZip.extractall(oldTmpBookDir)

        #print("    New zip: " + file + " extracted to: " + str(newTmpBookDir))
        #print("    Old zip: " + olderTarget[0] + " extracted to: " + str(oldTmpBookDir))

        tmpOutputFile = os.path.join(newerDir, ean + ".compare.log")
        tmpOutput = open(tmpOutputFile, mode="w")
        cmdStr = diffCmd + '"' + newTmpBookDir + '" "' + oldTmpBookDir + '"'
        subprocess.call(cmdStr, stdout=tmpOutput, stderr=tmpOutput, shell=True)
        tmpOutput.close()
        tmpOutput = open(tmpOutputFile, mode="r")
        theDiffs = tmpOutput.readlines()
        tmpOutput.close()
        nDiffs = len(theDiffs)
        if nDiffs > 0:
            print("  File #" + str(nChecked) + " (of " + str(newCount) + ") (" + str(nCompared) + " compared) - " + file + " have " + str(len(theDiffs)) + " differences:")
            for line in theDiffs:
                print("     Asset Diff: " + line.rstrip())
        else:
            print("  File #" + str(nChecked) + " (of " + str(newCount) + ") (" + str(nCompared) + " compared) - " + file + ": ePub contents in differing containers are identical")
            
        shutil.rmtree(newTmpBookDir)
        shutil.rmtree(oldTmpBookDir)

    print("--")
    print("")

    if gopts.saveDiffs:
        diffsDir = os.path.join(newerDir, gopts.diffsDir)
        if not os.path.exists(diffsDir):
            print ("Dir: " + diffsDir + " does not exist; creating it...")
            os.mkdir(diffsDir)
        shutil.move(tmpOutputFile, diffsDir)
    else:
        os.remove(tmpOutputFile)

    elapsedTime = str(time.time() - startTime)
    logStats(statsLog, elapsedTime, os.path.join(newerDir, file), olderTarget[0], str(zipsDiffer), str(nDiffs))