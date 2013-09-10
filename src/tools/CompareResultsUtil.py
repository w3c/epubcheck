#!c:\python27\python
import os
import sys
import optparse
import json
import CompareResults

def parse_compare_args(argv):
    prog_dir = os.path.dirname(argv[0])
    usage = """
Usage: %s <originalFile> <newFile> [OPTION] 
Compare epubcheck json output
"""[1:-1] % os.path.basename(argv[0])

    parser = optparse.OptionParser(usage=usage)
    parser.add_option("-r", "--resultFile", dest="resultFile", type="str", default="",
                      help="The destination file for the comparison results")

    opts,args = parser.parse_args(argv[1:])
    return opts,args
                   
global gopts
gopts,args = parse_compare_args(sys.argv)
if (len(sys.argv) < 3):
    print "ERROR: Insufficent args were provided.  Specify both the original and new json files"
    sys.exit();
gopts.oldFile = os.path.join(".", sys.argv[1])
gopts.newFile = os.path.join(".", sys.argv[2])
if (not os.path.isfile(gopts.oldFile)):
    print "ERROR: Original file " + gopts.oldFile + " could not be found"
    sys.exit()
if (not os.path.isfile(gopts.newFile)):
    print "ERROR: New file " + gopts.newFile + " could not be found"
    sys.exit()
oldJsonData = open(gopts.oldFile, "r").read()
oldResults = json.loads(oldJsonData)
newJsonData = open(gopts.newFile, "r").read()
newResults = json.loads(newJsonData)
jsonChanges = CompareResults.compareResults(oldResults, newResults)
if (gopts.resultFile == ""):
    gopts.resultFile = os.path.basename(gopts.oldFile) + "_" + os.path.basename(gopts.newFile) + ".json"
changesFP = open(gopts.resultFile, "w")
json.dump(jsonChanges, changesFP, indent=2)
changesFP.close()
print "Finished! Results can be found in " + gopts.resultFile