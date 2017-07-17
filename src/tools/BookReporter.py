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
import shutil
import json
import functools
import Dictionary
import CompareResults

defaultJarName = os.path.join(os.path.dirname(os.path.realpath(__file__)), r"epubcheck.jar")

def parse_args(argv):
    prog_dir = os.path.dirname(argv[0])
    usage = """
Usage: %s [OPTION]
BookReporter: ePubCheck all ePub files in the target directory, potentially preserving
                  generated .JSON output files, compare results to prior checks if old results are found.
                  Optional (use --ppDiffs) pretty-prints any jsondiffs.json files found in the json directory.
"""[1:-1] % os.path.basename(argv[0])

    parser = optparse.OptionParser(usage=usage)
    parser.add_option("-d", "--directory", dest="target", type="str", default=".",
                      help="Directory on which ePubCheck will be run, default is the current working directory")
    parser.add_option("-f", "--file", dest="targetFile", type="str", default="",
                      help=r'''File or comma separated list of files to run the check on; if -f is omitted, all files 
in the target directory will be checked. If you include a fully qualified path to a file, you can add additional comma 
separated file names in the same directory named on the first file (-f /path/a.epub,b.epub).
''')
    parser.add_option("--NoSaveJson", action="store_false", dest="saveJson", default=True,
                      help=r"Do NOT save ePubcheck .json output files")
    parser.add_option("--NoCompareJson", action="store_false", dest="compareJson", default=True,
                      help=r"Do NOT compare the json created during this check with the most recently saved .json result, if found.")
    parser.add_option("--EanOnlyJsonNames", action="store_true", dest="jsonNamedByEAN", default=False,
                      help=r"Use this flag to force .json file names to use EAN-only naming convention, <ean>.ePubCheck.json, not <file_name>.ePubCheck.json names. Files not conforming to EAN-first naming pattern will use the <file_name> convention")
    parser.add_option("-j", "--jsonDir", dest="jsonDir", type="str",
                      default=r"",
                      help=r"if the -s switch is used, ePubCheck .json output files will be preserved in either the location specified by the -e switch, or if -e is omitted, stored in <targetDir>\NOOKePubCheckJson")
    parser.add_option("--ppJson", dest="ppJson", action="store_true", default=False,
                      help=r"Skip checks and 'pretty print' any json files in the target directory's json output directory.")
    parser.add_option("--ppDiffs", dest="ppDiffs", action="store_true", default=False,
                      help=r"Skip checks and simply 'pretty-print' any jsondiffs.json files found in the target directory's json output directory.")
    parser.add_option("-v", "--verbose", dest="verbose", action="store_true", default=False,
                      help=r"Show all messages grouped by type")
    parser.add_option("-q", "--Hide_errors", dest="showErrors", action="store_false", default=True,
                      help=r"'Quiet' output mode; don't list FATAL and ERROR messages; by default these errors are always displayed on the console")
    parser.add_option("-w", "--warning", dest="showWarning", action="store_true", default=False,
                      help=r"Show WARNING messages; by default, these messages are not shown on the console")
    parser.add_option("-u", "--usage", dest="showUsage", action="store_true", default=False,
                      help=r"Show USAGE messages, by default, these messages are not shown on the console")

    parser.add_option("-l", "--logging", dest="loggingFlag", default=False, action="store_true",
                      help=r"Enable logging to a tab-delimited file")
    parser.add_option("--logdir", dest="logdir", type="str",
                      default=r"$EPUBCHECK-LOGS",
                      help=r'''Log file location used by this tool, defaults to the value of the environment variable "EPUBCHECK-LOGS";
if EPUBCHECK-LOGS is defined and a valid directory, logging is enabled to that directory automatically (-l is not required if EPUBCHECK-LOGS is defined)
if EPUBCHECK-LOGS is undefined and logging is enabled, logs are written in the current working directory.
if EPUBCHECK-LOGS is defined, automatic logging can be disabled by using the "--logdir none" switch.
''')
    parser.add_option("--logfile", dest="logfile", type="str",
                      default=r"NookReporter.TabDelimitedFile",
                      help=r"Log file name used by this tool, default=BookReporter.TabDelimitedFile")
    parser.add_option("--customCheckMessages", dest="overrideFile", type="str", default="$ePubCheckCustomMessageFile",
                      help=r'''Name of a custom ePubCheck message file for use in these checks. 
If not specified, the value of the environment variable $ePubCheckCustomMessages file will be used, if defined. 
To override the value of that environment variable, \use "--customCheckMessages=<filePath?" to use an alternate file, 
or use "--customChckMessages=none" to run ePubCheck with the default set of check message severities.
''')
    parser.add_option("--applicationJar", dest="appJar", type="str",
                      default=defaultJarName,
                      help=r"if specified,the named jar will be used; if not specified, " + defaultJarName + " in this script's directory will be used")
    parser.add_option("--jarArgs", dest="jarArgs", type="str",
                      default=r"",
                      help=r'''Any args specified with the --jarArgs switch will be passed to the applicationJar (ePubCheck); by default, no jarArgs are passed.
Note that the BookReporter adds several ePubCheck command line switches automatically, including -j, -mode exp, and possibly others.
Any parameters specified with --jarArgs will be appended to the command line, and could conflict with the switches added automatically. 
If the parameters include spaces, quote the --jarArgs parameter string.
''')
    parser.add_option("--timeout", dest="timeoutVal", type="int", default=30,
                      help=r"Abort a ePubCheck process that takes longer than the --timeout nnn in seconds. NOTE: This setting is ignored unless you are using Python 3.3 or later")
    opts,args = parser.parse_args(argv[1:])
    return opts,args

def fileMD5(fileName):
    import hashlib
    md5 = hashlib.md5()
    block_size = 128*md5.block_size
    with open(fileName,'rb') as f:
        for chunk in iter(functools.partial(f.read, block_size), b''):
            md5.update(chunk)
    return md5.hexdigest()

def checkForException(outputFile, targetString):
    with open(outputFile, 'r') as f:
        for line in f:
            #print(" debug: log file line: " + line.rstrip("\n"))
            if targetString in line:
                f.close()
                return True
    f.close()
    return False


def logMessages(theMessages, showType):
    nMessage=0
    for message in theMessages:
        if message["severity"] == showType:
            nMessage += 1
            if nMessage == 1:
                print ("    " + showType + " messages:")
            nTimes = len(message["locations"])
            if message["additionalLocations"] != 0: nTimes += message["additionalLocations"] - 1
            timesStr = " (" + str(nTimes) + " occurrence"
            if nTimes == 1:
                timesStr += ")"
            else:
                timesStr += "s)"
            try:
                print ("      " + str(nMessage) + ": " + message["ID"] + ": " + message["message"] + timesStr)
            except:
                print ("      " + str(nMessage) + ": " + message["ID"] + ": " + urllib.quote(message["message"].encode('utf-8')) + timesStr)
            if gopts.verbose:
                for loc in message["locations"]:
                    if loc["line"] != -1 or loc["column"] != -1:
                        locString = ": line: " + str(loc["line"]) + " col: " + str(loc["column"])
                    else:
                        locString = ""
                    try:
                        print("            " + loc["fileName"] + locString)
                    except:
                        print("            " + urllib.quote(loc["fileName"].encode('utf-8')) + urllib.quote(locString.encode('utf-8')))
                print("")
    if nMessage > 0:
        print ("    --")

def listServerFiles(theDir):
    theFiles = []
    os.chdir(theDir)
    files = os.listdir(".")
    for file in files:
        theFiles.append(string.strip(file))
    return(theFiles)

def logStats(log_file, checkerVersion, book_dir, book_file, book_path, elapsedTime, checkTime, ePubVersion, comparedTo, checkChanged, pubChanged, spineChanged, manifestChanged, messagesChanged, numFatal, numError, isScripted, hasFixedFormat):

    now = datetime.datetime.now()
    dateTime = str(now.date()) + "\t" + str(now.time())

    if not os.path.exists(log_file):
        print ("File " + log_file + " doesn't exist, inititalizing file...")
        f = open(log_file, 'a')
        f.write("logDate\t")
        f.write("logTime\t")
        f.write("logTool\t")
        f.write("checkerVersion\t")
        f.write("PubDir\t")
        f.write("ePubFile\t")
        f.write("ePubPath\t")
        f.write("elpasedTime\t")
        f.write("checkTime\t")
        f.write("ePubVersion\t")
        f.write("comparedTo\t")
        f.write("checkChanged\t")
        f.write("pubChanged\t")
        f.write("spineChanged\t")
        f.write("manifestChanged\t")
        f.write("messagesChanged\t")
        f.write("numFatal\t")
        f.write("numErrors\t")
        f.write("isScripted\t")
        f.write("hasFixedFormat\n")
    else:
        f = open(log_file, 'a')

    f.write(str(now.date()) + "\t" + str(now.time()) + "\t")
    f.write("BookReporter.py" + "\t")
    f.write(checkerVersion + "\t")
    f.write(book_dir + "\t")
    f.write(book_file.rstrip("\r") + "\t")
    f.write(book_path + "\t")
    f.write(elapsedTime + "\t")
    f.write(checkTime + "\t")
    f.write(ePubVersion + "\t")
    f.write(comparedTo + "\t")
    f.write(checkChanged + "\t")
    f.write(pubChanged + "\t")
    f.write(spineChanged + "\t")
    f.write(manifestChanged + "\t")
    f.write(messagesChanged + "\t")
    f.write(numFatal + "\t")
    f.write(numError + "\t")
    f.write(isScripted + "\t")
    f.write(hasFixedFormat + "\n")
    f.close()

    return "BookReporter Logging complete..."

def ppEpubCheckChanges(jsonDelta):
    #
    # print the check metadata plus the publication and manifest item metadata changes
    #
    oldCheck = jsonDelta["summary"]["oldCheck"]
    newCheck = jsonDelta["summary"]["newCheck"]
    print("    ePubCheck results comparison")
    print("    --\n\r")
    print("    New file: " + newCheck["path"] + " checked on: " + newCheck["checkDate"])
    #
    # json schema change: moved file name from publication to checker; if the name isn't found, use one in publication; this hack can be removed soon.
    #
    try:
       print("    Old file: " + oldCheck["path"] + " checked on: " + oldCheck["checkDate"])
    except:
       print("    Old file: path not found due to json output schema change; the old file was checked on: " + oldCheck["checkDate"])
    print("    --\n\r")
    print("    Summary: publication metadata changes: " + str(jsonDelta["summary"]["publicationChanges"]))
    print("             spine item changes: " + str(jsonDelta["summary"]["spineChanges"]))
    print("             manifest item changes: " + str(jsonDelta["summary"]["itemChanges"]))
    print("    --\n\r")
    if jsonDelta["summary"]["publicationChanges"] > 0:
        print("    Publication property changes:")
        pubChanges = jsonDelta["publication"]
        if len(pubChanges["adds"]) > 0:
            print("      New properties added: ")
            for item in pubChanges["adds"]:
                print("        '" + item + "' (value: " + str(pubChanges["adds"][item]) + ")")
        if len(pubChanges["cuts"]) > 0:
            print("      Properties removed: ")
            for item in pubChanges["cuts"]:
                print("        '" + item + "' (value was: " + str(pubChanges["cuts"][item]) + ")")
        if len(pubChanges["changes"]) > 0:
            print("      Properties changed: ")
            for item in pubChanges["changes"]:
                if item == "embeddedFonts":
                    print("        " + item + " changed --  new embedded font list:")
                    for fontString in pubChanges["changes"][item]["newValue"]:
                        print("          " + str(fontString))
                    print("        " + item + " changed --  old embedded font list:")
                    for fontString in pubChanges["changes"][item]["oldValue"]:
                        print("          " + str(fontString))
                else:
                    try:
                        print("        '" + item + "'changed --  new value: '" + str(pubChanges["changes"][item]["newValue"]) + "'; old value: '" + str(pubChanges["changes"][item]["oldValue"]) + "'")
                    except:
                        print("        '" + item + "'changed -- exception occurred trying to render old or new property value")
    else:
        print("    Publication property changes: NONE")

    print("    --\r\n")

    if jsonDelta["summary"]["spineChanges"] > 0:
        print("    Spine changes:")
        spineChanges = jsonDelta["spine"]
        print("      Unchanged spine items: " + str(spineChanges["unchanged"]))
        if len(spineChanges["adds"]) > 0:
            print("      New spine items added: ")
            for item in spineChanges["adds"]:
                print("        '" + item + "' order: " + str(spineChanges["adds"][item]))
        if len(spineChanges["cuts"]) > 0:
            print("      Spine items removed: ")
            for item in spineChanges["cuts"]:
                print("        '" + item + "' order was: " + str(spineChanges["cuts"][item]))
        if len(spineChanges["orderChanges"]) > 0:
            print("      Spine items reordered: ")
            for item in spineChanges["orderChanges"]:
                print("        '" + item + "' spine order changed --  new order: '" + str(spineChanges["orderChanges"][item]["newSpineIndex"]) + "'; old order: '" + str(spineChanges["orderChanges"][item]["oldSpineIndex"]) + "'")
        
        if len(spineChanges["contentChanges"]) > 0:
            print("      Spine item content changes: ")
            for item in spineChanges["contentChanges"]:
                print("        spine ID: '" + item + "' file: '" + str(spineChanges["contentChanges"][item]) + "'")
    else:
        print("    Publication spine changes: NONE")

    print("    --\r\n")

    if jsonDelta["summary"]["itemChanges"] > 0:
        print("    Publication manifest item changes:")
        maniChanges = jsonDelta["manifest"]

        if len(maniChanges["adds"]) > 0:
            print("      Manifest items added: ")
            for item in maniChanges["adds"]:
                print("        '" + item + "'")
                if gopts.verbose:
                    for property in maniChanges["adds"][item]:
                        if property == "referencedItems":
                            print("          referenced items: ")
                            for references in maniChanges["adds"][item][property]:
                                print("                            " + references)
                        else:
                            try:
                                print("          property: " + property + " -- value: " + str(maniChanges["adds"][item][property]))
                            except:
                                print("          property: " + property + " -- value caused exception during output")

        if len(maniChanges["cuts"]) > 0:
            print("\r\n      Manifest items removed: ")
            for item in maniChanges["cuts"]:
                print("        '" + item + "'")
                if gopts.verbose:
                    for property in maniChanges["cuts"][item]:
                        if property == "referencedItems":
                            print("          referenced items: ")
                            for references in maniChanges["cuts"][item][property]:
                                print("                            " + references)
                        else:
                            try:
                                print("          property: " + property + " -- value: " + str(maniChanges["cuts"][item][property]))
                            except:
                                print("          property: " + property + " -- value caused exception during output")

        if len(maniChanges["changes"]) > 0:
            theChanges = maniChanges["changes"]
            print("\r\n      Manifest item property changes: ")
            for itemId in theChanges:
                if "adds" in theChanges[itemId]:
                    for theAdd in theChanges[itemId]["adds"]:
                        print("        Manifest item ID: '" + itemId + "' -- property '" + theAdd + "' was added to the manifest item (value: " + str(theChanges[itemId]["adds"][theAdd]) + ")")
                if "cuts" in theChanges[itemId]:
                    for theCut in theChanges[itemId]["cuts"]:
                        print("        Manifest item ID: '" + itemId + "' -- property '" + theCut + "' was removed from the manifest item (value: " + str(theChanges[itemId]["cuts"][theCut]) +")")
                if "changes" in theChanges[itemId]:
                    for property in theChanges[itemId]["changes"]:
                        if property == "referencedItems":
                            if "adds" in theChanges[itemId]["changes"][property]:
                                for theAdd in theChanges[itemId]["changes"][property]["adds"]:
                                    try:
                                        print("        Manifest item ID: '" + itemId + "' -- added reference to: '" + theAdd +"'")
                                    except:
                                        print("        Manifest item ID: '" + itemId + "' -- added reference to: exception thrown during output of the added reference")
                            if "cuts" in theChanges[itemId]["changes"][property]:
                                for theCut in theChanges[itemId]["changes"][property]["cuts"]:
                                    try:
                                        print("        Manifest item ID: '" + itemId + "' -- removed reference to: '" + theCut +"'")
                                    except:
                                        print("        Manifest item ID: '" + itemId + "' -- added reference to: exception thrown during output of the cut reference")
                        elif property == "checkSum":
                            try:
                                print("        Manifest item ID: '" + itemId + "' -- the associated file '" + str(theChanges[itemId]["changes"][property]["newValue"]) +"' contents changed")
                            except:
                                print("        Manifest item ID: '" + itemId + "' -- the associated file (named caused exception on output) contents changed")
                        else:
                            try:
                                print("        Manifest item ID: '" + itemId + "' -- property '" + property + "' changed -- newValue: " + str(theChanges[itemId]["changes"][property]["newValue"]) + "; oldValue: " + str(theChanges[itemId]["changes"][property]["oldValue"]))
                            except:
                                print("        Manifest item ID: '" + itemId + "' -- property '" + property + "' changed -- old or new property value caused exception during output")
    else:
        print("    Publication manifest changes: NONE")
    print("    --\r\n")

def compareMessages(newMessages, oldMessages):
    if newMessages == oldMessages:
        print ("    Message collections are identical you idiot")
        messDelta = "Mess=!"
    else:
        print ("    Message collections comparisons:")
        messDelta = "Mess-"
    lenNew = len(newMessages)
    lenOld = len(oldMessages)
    oldItems = set()
    oldDict = {}
    newItems = set()
    newDict = {}
    blankIDinNew = 0
    blankIDinOld = 0
    for item in newMessages:
        key = item["ID"]
        if key == "":
            blankIDinNew += 1
            key = "BlankID_" + str(blankIDinNew)
        if not key in newItems:
            newItems.add(key)
            newDict[key] = item
        '''
        else:
            print ("      Message collections comparison: ID Collision in NEW Messages: " + key)
        '''
    for item in oldMessages:
        key = item["ID"]
        if key == "":
            blankIDinOld += 1
            key = "BlankID_" + str(blankIDinOld)
        if not key in oldItems:
            oldItems.add(key)
            oldDict[key] = item
        '''
        else:
            print ("      Message collections comparison: ID Collision in OLD Messages: " + key)
        '''

    delta = Dictionary.DictCompare(newDict, oldDict)
    adds = delta.added()
    cuts = delta.removed()
    changes = delta.changed()
    if len(adds) != 0:
        messDelta += "A" + str(len(adds)) + "-"
        for id in adds:
            print ("      Message ID: " + id + " added")
    else:
        messDelta += "xA-"
        print ("      No Message items added")
    if len(cuts) != 0:
        messDelta += "R" + str(len(cuts)) + "-"
        for id in cuts:
            print ("      Message ID: " + id + " removed")
    else:
        messDelta += "xR-"
        print ("      No Message items removed")
    if len(changes) != 0:
        messDelta += "C" + str(len(changes))
        for id in changes:
            newRecord = newDict[id]
            oldRecord = oldDict[id]
            recDelta = Dictionary.DictCompare(newRecord, oldRecord)
            for name in recDelta.added():
                print ('      Message ID: "' + id + '" -- property value "' + name + '" added; value: "' + str(newRecord[name]) + '"')
            for name in recDelta.removed():
                print ('      Message ID: "' + id + '" -- property value "' + name + '" removed; old value was: "' + str(oldRecord[name]) + '"')
            for name in recDelta.changed():
                if name == "locations":
                    print ('      Message ID: "' + id + '" -- property value "' + name + '" changed; new occurrence count: ' + str(len(newRecord[name])) + '; old occurrence count: ' + str(len(oldRecord[name])))
                    newLocs = set()
                    oldLocs = set()
                    oldLocsDict = {}
                    newLocsDict = {}
                    for locs in newRecord[name]:
                        if not locs["fileName"]:
                            locs["fileName"]= id + "HasNullFileNameLoc"
                        locID = locs["fileName"] + "-" + str(locs["line"]) + "-" + str(locs["column"])
                        if not locID in newLocs:
                            newLocs.add(locID)
                            newLocsDict[locID] = locs
                        else:
                            print ("        Messages: duplicate location in NEW message collection for message ID: " + id + " Location: " + locs["fileName"] +  "@ " + str(locs["line"]) + ":" + str(locs["column"]))
                    for locs in oldRecord[name]:
                        if not locs["fileName"]:
                            locs["fileName"]= id + "HasNullFileNameLoc"
                        locID = locs["fileName"] + "-" + str(locs["line"]) + "-" + str(locs["column"])
                        if not locID in oldLocs:
                            oldLocs.add(locID)
                            oldLocsDict[locID] = locs
                        else:
                            print ("        Messages: duplicate location in OLD message collection for message ID: " + id + " Location: " + locs["fileName"] + " @ " + str(locs["line"]) + ":" + str(locs["column"]))
                    locsDelta = Dictionary.DictCompare(newLocsDict, oldLocsDict)
                    for locs in locsDelta.added():
                        try:
                            print ('        Message ID: "' + id + '" -- location added; value: "' + newLocsDict[locs]['fileName'] + ' @ ' + str(newLocsDict[locs]['line']) + ':' + str(newLocsDict[locs]['column']) + '"')
                        except:
                            print ('        Message ID: "' + id + '" -- location added; value: caused exception on output')

                    for locs in locsDelta.removed():
                        try:
                            print ('        Message ID: "' + id + '" -- location removed; value: ' + oldLocsDict[locs]['fileName'] + ' @ ' + str(oldLocsDict[locs]['line']) + ':' + str(oldLocsDict[locs]['column']) +'"')
                        except:
                            print ('        Message ID: "' + id + '" -- location removed; value: caused exception on output')
                    '''
                    for locs in locsDelta.changed():
                        print ('      Manifest  ID: "' + id + '" -- property value "' + name + '" changed; new value: "' + str(newRecord[name]) + '"; old value: "' + str(oldRecord[name]) + '"')
                    '''
                else:
                    print ('      Message ID: "' + id + '" -- property value "' + name + '" changed; new value: "' + str(newRecord[name]) + '"; old value: "' + str(oldRecord[name]) + '"')

    else:
        messDelta += "xC"
        print ("      No Message item properties were changed")
    if blankIDinNew != 0:
        print ("    " + str(blankIDinNew) + " blank Message item IDs in new Messages found")
    if blankIDinOld != 0:
        print ("    " + str(blankIDinOld) + " blank Message item IDs in old Messages found")
    print ("    --")
    return(messDelta)

print ("BookReporter Tool: Check ePubs and optionally compare check results to a previous check")
global gopts
gopts,args = parse_args(sys.argv)

# print (str(gopts.target))
#
# set up logging of checking activity
#
logging = gopts.loggingFlag
if (logging or os.path.isdir(os.path.expandvars(gopts.logdir)) != "") and os.path.expandvars(gopts.logdir) != "none":
    logging = True
    #
    # verify that the log dir and file are writable
    #
    if not os.path.isdir(os.path.expandvars(gopts.logdir)):
        print ('Activity logging directory "' + os.path.expandvars(gopts.logdir) + '" is not a valid dir, logging to the current working directory')
        statsLog = os.path.join(".", gopts.logfile)
    else:
        statsLog = os.path.join(os.path.expandvars(gopts.logdir), gopts.logfile)
    print ("Activity logging is being performed to " + statsLog)

#
# find the epubcheck jar, or give up...
#
if os.path.exists(gopts.appJar):
        ePubCheckCmd = "java -jar " + gopts.appJar
else:
    print ("'" + gopts.appJar + "' was not found; BookReporter.py is aborting...")
    exit(1)
#print ("BookReporter is using the java command: " + ePubCheckCmd)

#
# decide if this is python 2.7 or 3.3 and whether to use the timeout= flag on subprocess.call
#

useTimeout = False
pyVersionString = sys.version
pyVersion = float(pyVersionString.split(' ')[0].split(".")[0] + "." + pyVersionString.split(' ')[0].split(".")[1])
if pyVersion >= 3.3:
    useTimeout = True
    print("BookReporter is running on Python version: " + str(pyVersion) + " and is using a check time limit of: " + str(gopts.timeoutVal) + "s")
else:
    print("BookReporter is running on Python version: " + str(pyVersion) + "; no check time limit is being enforced. Some books can take > 5 minutes to check...")


#
# ensure that targetDir is actually a directory...
#
targetDir = gopts.target
if not os.path.isdir(targetDir):
    print ("-d " + targetDir + " is not a directory, aborting...")
    sys.exit(1)

ppDiffs = gopts.ppDiffs
ppJson = gopts.ppJson
#
# listing will contain the list of files to check, either ePubs in targetDir or .jsondiffs.json or epubcheck.json files in the json output directory of either --ppDiffs or --ppJson are used
#
if gopts.overrideFile == "" or gopts.overrideFile == "none":
    print("BookReporter is using NO ePubCheck custom message file... even if one is specified by the environment variable $ePubCheckCustomMessageFile")
    overrideCmdStr = ""
else:
    overrideFile = os.path.expandvars(gopts.overrideFile)
    if overrideFile != "" and os.path.exists(overrideFile):
        print("BookReporter is using the ePubCheck custom message file: " + overrideFile)
        overrideCmdStr = ' -c "' + overrideFile + '" '
        print("BookReporter override command string= '" + overrideCmdStr +"'")
    else:
        print("BookReporter could not file the --customMessageFile file: " + overrideFile + "; check continuing without an override file...")
        overrideCmdStr = ""

#
# set up the errorLogDirectory name, if necessary; but don't create it until it's needed
#

if gopts.jsonDir == "":
    errorsDir = os.path.join(targetDir, "ePubCheckJson")
else:
    errorsDir = gopts.jsonDir

if not ppDiffs and not ppJson:
    targetFileType = ".epub"
    if gopts.targetFile == "":
        listing = os.listdir(targetDir)
    else: 
        #
        # handle the case where 
        #   target file is not specified
        #   where it contains a list of comma separated file names
        #   and where it contains a fq pathname
        # 
        filePath, fileName = os.path.split(gopts.targetFile)
        if filePath != "":
            targetDir = filePath
        listing = fileName.split(",")
else:
    targetDir = errorsDir
    if ppJson: 
        targetFileType = ".epubcheck.json"
    if ppDiffs: 
        targetFileType = ".jsondiffs.json"
    if gopts.targetFile == "":
        listing = os.listdir(targetDir)
    else:
        filePath, fileName = os.path.split(gopts.targetFile)
        if filePath != "":
            targetDir = filePath
        listing = fileName.split(",")

nClean=0
nErrs=0
nChecked = 0
nTotalFiles = len(listing)

print ("--\r\n")
print ("Target Dir= " + targetDir + " (contains "+ str(nTotalFiles) + " files; looking for files of type '" + targetFileType + "' to examine)")

saveJson = gopts.saveJson
if saveJson: 
   if not os.path.exists(errorsDir):
        print ("Dir: " + errorsDir + " does not exist; creating it...")
        os.mkdir(errorsDir)
else:
   print (".json output files are NOT being saved")
print ("--")
print ("")



for file in listing:
    if ppJson or ppDiffs:
        if targetFileType in file.lower():
            if ppJson and file.lower().find(targetFileType) == len(file)-len(targetFileType):
                print(" Pretty print: " + file)
                jsonFile = os.path.join(targetDir, file)
                jsonData = open(jsonFile, "r").read()
                checkResults = json.loads(jsonData)
                print("    Messages Summary:")
                logMessages(checkResults["messages"], "FATAL")
                logMessages(checkResults["messages"], "ERROR")
                if gopts.verbose or gopts.showWarning:
                    logMessages(checkResults["messages"], "WARNING")
                if gopts.verbose or gopts.showUsage:
                        logMessages(checkResults["messages"], "USAGE")
                print ("--")
                print ("")
            else:
                print ("File #" + str(nChecked) + ": " + file + " is not of type '" + targetFileType + "'; skipped...")
                continue
            if ppDiffs:
                print(" Prettyprint: " + file)
                jsonFile = os.path.join(targetDir, file)
                jsonData = open(jsonFile, "r").read()
                jsonDiffs = json.loads(jsonData)
                ppEpubCheckChanges(jsonDiffs)
        else:
            print ("File #" + str(nChecked) + ": " + file + " is not of type '" + targetFileType + "'; skipped...")
            continue
        continue

    startTime = time.time()
    nChecked += 1
    expString = ""
    if os.path.splitext(file)[-1].lower() != ".epub":
        if os.path.isdir(os.path.join(targetDir, file)) and os.path.exists(os.path.join(targetDir, file, "mimetype")):
            print ("File #" + str(nChecked) + ": " + file + " is a directory and mimetype file found; treating it as an expanded ePub...")
            expString = " --mode exp"
            if(os.path.exists(os.path.join(targetDir, file + ".epub"))):
                expString += " --save"
                print("    ePub: '" + os.path.join(targetDir, file + ".epub") + "' exists; it will be overwritten, or if severe errors exist when checking the expanded ePub directory '" + file + "' it will be deleted.")
        else:
            print ("File #" + str(nChecked) + ": " + file + " is not an ePub, skipped...")
            continue
#
# Process an ePub
#
    ePub = os.path.join(targetDir, file)
    print ("File #" + str(nChecked) + " (of " + str(nTotalFiles) + ") : " + ePub  +" checking...\r"),

    tmpOutputFile = os.path.join(targetDir, file + "Check.log")
    cmdStr = ePubCheckCmd + ' ' + '"' + ePub + '"' + overrideCmdStr
    #if gopts.jarArgs != "":
    #
    # hack to add default json output file name to the -j param until that bug gets fixed in ePubCheck
    #
    if not gopts.jsonNamedByEAN:
        jsonFileName = file + 'Check.json'
    else:
        if not file[0:13].isdigit():
            jsonFileName = file + 'Check.json'
            print ('File: "' + file + '" does not conform to the leading-EAN naming scheme; json output file will be named: "' + jsonFileName +'"')
        else:
            jsonFileName = file[0:13] + ".ePubCheck.json"
    jsonFile = os.path.join(targetDir, jsonFileName)
    cmdStr += ' -u -j "' + jsonFile + '"' + expString

#
# add the -u flag to epubcheckh if verbose output is on...
#
    if gopts.verbose: cmdStr += " -u"
#
# add the value of the env var ePubCheckCustomMessageFile if it exists
#

#
# add any more jarArgs to the command line
#  this is very brittle as the script adds a bunch of things autocratically which could conflict
#
    cmdStr += " " + gopts.jarArgs
    # print ("ePubCheck cmdstr= " + cmdStr)
#
# run ePubcheck and present the results stored in the json output file; If the script is running in Python 3.3 or later, use the timeout=
#
    tmpOutput = open(tmpOutputFile, "w")
    if(useTimeout):
        try:
            checkTime = time.time()
            checkProcess = subprocess.call(cmdStr, stdout=tmpOutput, stderr=tmpOutput, shell=True, timeout=gopts.timeoutVal)
            checkTime = time.time() - checkTime
        except subprocess.TimeoutExpired:
            if logging:
                elapsedTime = time.time() - startTime
                checkTime = gopts.timeoutVal
                logStats(statsLog, targetDir, file, ePub, str(elapsedTime), str(checkTime), "NA", "NA", "NA", "NA", "NA", "NA", "NA", "ePubcheck hang", "NA", "NA", "NA")
            print ("ePubCheck of " + file + " has hung; skipping this file")
            continue
    else:
        checkTime = time.time()
        checkProcess = subprocess.call(cmdStr, stdout=tmpOutput, stderr=tmpOutput, shell=True)
        checkTime = time.time() - checkTime
        elapsedTime = time.time() - startTime
    tmpOutput.close()
    if checkForException(tmpOutputFile, "com.adobe.epubcheck.tool.Checker.main"):
        print(" Epubcheck threw exception processing file: " + file + "; processing of this file aborting...")
        logStats(statsLog, "NA", targetDir, file, ePub, str(elapsedTime), str(checkTime), "NA", "NA", "NA", "NA", "NA", "NA", "NA", "ePubcheck exception", "NA", "NA", "NA")
        continue
#
# examine the .json output file and check the number of errors reported
#
    if os.path.exists(jsonFile):
        try:
            jsonData = open(jsonFile, "r").read()
            checkResults = json.loads(jsonData)
        except:
            if logging:
                elapsedTime = time.time() - startTime
                logStats(statsLog, targetDir, file, ePub, str(elapsedTime), str(checkTime), "NA", "NA", "NA", "NA", "NA", "NA", "NA", "exception on .json json.loads", "NA", "NA", "NA")
            print ("json output file " + jsonFile + " caused exception on load, processing of " + ePub + " abandoned...")
            continue
        checkerVersion = checkResults["checker"]["checkerVersion"]
        nErrs = checkResults["checker"]["nFatal"] + checkResults["checker"]["nError"]
        if nErrs == 0:
            print ("File #" + str(nChecked) + " (of " + str(nTotalFiles) + ") : " + ePub + " contained NO severe error messages:")
        else:
            print ("File #" + str(nChecked) + " (of " + str(nTotalFiles) + ") : " + ePub + " has " + str(nErrs) + " severe errors (" + str(checkResults["checker"]["nFatal"]) +" FATAL and " + str(checkResults["checker"]["nError"]) + " ERROR messages)")
#
# custom message file in use?
#
        customMessages = checkResults["customMessageFileName"]
        if customMessages is not None:
            print("    Custom message file in use: '" + customMessages +"'")
            print("")
#
# failure case, expected .json file not found, keep trying!
#
    else:
       if logging:
           elapsedTime = time.time() - startTime
           logStats(statsLog, "NA", targetDir, file, ePub, str(elapsedTime), str(checkTime), "NA", "NA", "NA", "NA", "na", "NA", "NA", ".json output FNF", "NA", "NA", "NA")
       print ("json output file " + jsonFile + " NOT found, processing aborting...")
       continue
#
# preserve the json if requested; [TODO] only diff them if saving and an older version was foun
#
# simple case, no prior json file exists, save it away and move on
#
    if not os.path.exists(os.path.join(errorsDir, jsonFileName)):
        if saveJson:
            shutil.move(jsonFile, errorsDir)
        oldPath = "NA"
        checkChanges = "NA"
        pubChanged = "NA"
        spineChanged = "NA"
        manifestChanged = "NA"
        messagesChanged = "NA"
        oldJsonExists = False
    else:
#
# existing json file for this epub was found; load the old json to see if its identical; if it is, save the latest copy
#
       oldJsonExists = True
       if gopts.compareJson:
           jsonData = open(os.path.join(errorsDir, jsonFileName), "r").read()
           oldResults = json.loads(jsonData)
           jsonChanges = CompareResults.compareResults(oldResults, checkResults)
           changesJsonFile = jsonFileName + "diffs.json"
           changesJson = os.path.join(errorsDir, changesJsonFile)
           if os.path.exists(changesJson):
                oldMTime = os.path.getmtime(changesJson)
                oldFMTtime = time.strftime("%Y-%m-%d-%H%M.%S", time.gmtime(oldMTime))
                newName = jsonFileName + "diffs." + oldFMTtime +".json"
                print ("    Found an older json diff output file for " + ePub + "; saving the older version as: '" + os.path.join(errorsDir, newName) + "'\r\n")
                os.rename(changesJson, os.path.join(errorsDir, newName))
           changesFP = open(changesJson, "w")
           json.dump(jsonChanges, changesFP, indent=2)
           changesFP.close()

       #
       # json schema change: moved file name from publication to checker; if the name isn't found, use one in publication; this hack can be removed soon.
       #
       try:
           oldPath = oldResults["checker"]["path"]
       except:
           oldPath = oldResults["publication"]["path"]
       #if (oldResults["publication"] == checkResults["publication"]) and (oldResults["items"] == checkResults["items"]) and (oldResults["messages"] == checkResults["messages"]):
       if jsonChanges["summary"]["publicationChanges"] + jsonChanges["summary"]["itemChanges"]  == 0:
           print ('    ePubCheck done on "' + checkResults["checker"]["checkDate"] + '" matched results of check done of "' + str(oldPath) + '" on "' + oldResults["checker"]["checkDate"] + '"; preserving the latest output file...')
           #
           # save the latest version
           #
           if saveJson:
               os.remove(os.path.join(errorsDir, jsonFileName))
               shutil.move(jsonFile, errorsDir)
           else:
               os.remove(jsonFile)
           pubChanged = False
           spineChanged = False
           manifestChanged = False
           checkChanges = False
           if oldResults["messages"] == checkResults["messages"]:
               messagesChanged = False

#
# the two json files aren't identical; preserve the old one by adding a timestamp to the file name and save the new one as "file".epubCheck.json
#
       else:
           checkChanges = True
           if saveJson:
                oldMTime = os.path.getmtime(os.path.join(errorsDir, jsonFileName))
                oldFMTtime = time.strftime("%Y-%m-%d-%H%M.%S", time.gmtime(oldMTime))
                newName = jsonFileName + "." + oldFMTtime +".json"
                print ("    Found an older, differing, json output file for " + ePub + "; saving the older version as: '" + os.path.join(errorsDir, newName) + "'\r\n")
                os.rename(os.path.join(errorsDir, jsonFileName), os.path.join(errorsDir, newName))
                shutil.move(jsonFile, errorsDir)
           else:
               os.remove(jsonFile)

           print ("")
       ppEpubCheckChanges(jsonChanges)
       pubChanged = jsonChanges["summary"]["encodedPubChanges"]
       spineChanged = jsonChanges["summary"]["encodedSpineChanges"]
       manifestChanged = jsonChanges["summary"]["encodedManiChanges"]

#
# log output to console
#
    if nErrs > 0 and gopts.showErrors:
        print ("")
        print ("    Messages Summary:")
        logMessages(checkResults["messages"], "FATAL")
        logMessages(checkResults["messages"], "ERROR")
    if gopts.verbose or gopts.showWarning:
        logMessages(checkResults["messages"], "WARNING")
    if gopts.verbose or gopts.showUsage:
            logMessages(checkResults["messages"], "USAGE")
    if oldJsonExists and gopts.compareJson:
        if checkResults["messages"] != oldResults["messages"] and (gopts.verbose):
            messagesChanged = compareMessages(checkResults["messages"], oldResults["messages"])
        if checkResults["messages"] != oldResults["messages"] and not gopts.verbose:
            messagesChanged = "True, comparison disabled"
            print ("    Generated messages from " + file + " changed...")
            print ("    --")
        if checkResults["messages"] == oldResults["messages"] and gopts.verbose:
            messagesChanged = "Mess=="
            print ("    Generated messages from " + file + " are unchanged...")
            print ("    --")
        else:
            messagesChanged = "NA, comparison disabled"
    print ("--")
    print ("")
#
# save the results and cleanup after each file
#
    elapsedTime = time.time() - startTime
    if logging: logStats(statsLog, checkerVersion, targetDir, file, ePub, str(elapsedTime), str(checkTime), str(checkResults["publication"]["ePubVersion"]), str(oldPath), str(checkChanges), pubChanged, spineChanged, manifestChanged, str(messagesChanged), str(checkResults["checker"]["nFatal"]), str(checkResults["checker"]["nError"]),  str(checkResults["publication"]["isScripted"]),  str(checkResults["publication"]["hasFixedFormat"]))
    os.remove(tmpOutputFile)
