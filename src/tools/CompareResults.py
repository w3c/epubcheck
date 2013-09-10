#!c:\python27\python
import os
import sys
import optparse
import json
import Dictionary

def compareResults(oldJson, newJson):
    #
    # initialize the differences structure and local copies of the publication metadata and the manifest for comparison
    #
    jsonDelta = {}
    jsonDelta["summary"] = {}
    jsonDelta["summary"]["newCheck"] = newJson["checker"]
    jsonDelta["summary"]["oldCheck"] = oldJson["checker"]
    jsonDelta["publication"] = {}
    jsonDelta["publication"]["adds"] = {}
    jsonDelta["publication"]["cuts"] = {}
    jsonDelta["publication"]["changes"] = {}
    jsonDelta["manifest"] = {}
    jsonDelta["manifest"]["adds"] = {}
    jsonDelta["manifest"]["cuts"] = {}
    jsonDelta["manifest"]["changes"] = {}
    oldPub = oldJson["publication"]
    newPub = newJson["publication"]
    newMani = newJson["items"]
    oldMani = oldJson["items"]
    #
    # compare the publication metadata
    #
    deltaPub = Dictionary.DictCompare(newPub, oldPub)
    for item in deltaPub.added():
        jsonDelta["publication"]["adds"][item] = newPub[item]
    for item in deltaPub.removed():
        jsonDelta["publication"]["cuts"][item] = oldPub[item]
    pubChanges = deltaPub.changed()
    if len(pubChanges) > 0:
        for item in pubChanges:
            jsonDelta["publication"]["changes"][item] = {"newValue" : newPub[item], "oldValue" : oldPub[item]}
    if len(jsonDelta["publication"]["adds"]) + len(jsonDelta["publication"]["cuts"]) + len(jsonDelta["publication"]["changes"]) == 0:
        pubChanges = "Pub=="
    else:
        pubChanges = "Pub-"
        if len(jsonDelta["publication"]["adds"]) > 0:
            pubChanges += "A" + str(len(jsonDelta["publication"]["adds"]))
        else:
            pubChanges += "xA"
        if len(jsonDelta["publication"]["cuts"]) > 0:
            pubChanges += "-R" + str(len(jsonDelta["publication"]["cuts"]))
        else:
            pubChanges += "-xR"
        if len(jsonDelta["publication"]["changes"]) > 0:
            pubChanges += "-C" + str(len(jsonDelta["publication"]["changes"]))
        else:
            pubChanges += "-xC"
    jsonDelta["summary"]["publicationChanges"] = len(jsonDelta["publication"]["adds"]) + len(jsonDelta["publication"]["cuts"]) + len(jsonDelta["publication"]["changes"])
    jsonDelta["summary"]["encodedPubChanges"] = pubChanges
        # print(" Calculated publication changes: adds: " + str(len(jsonDelta["publication"]["adds"])) + "; cuts: " + str(len(jsonDelta["publication"]["cuts"])) + "; changes: " + str(len(jsonDelta["publication"]["changes"])))

    #
    # do the manifest
    #
    newManiDict = Dictionary.makeDict(newMani, "id")
    oldManiDict = Dictionary.makeDict(oldMani, "id")
    deltaMani = Dictionary.DictCompare(newManiDict, oldManiDict)
    for item in deltaMani.added():
        jsonDelta["manifest"]["adds"][item] = newManiDict[item]
    for item in deltaMani.removed():
        jsonDelta["manifest"]["cuts"][item] = oldManiDict[item]
    maniChanges = list(deltaMani.changed())
    if (len(maniChanges)) > 0:
        for item in maniChanges:
            itemDelta = {}
            deltaItem = Dictionary.DictCompare(newManiDict[item], oldManiDict[item])
            if len(deltaItem.added()) > 0:
                itemDelta["adds"] = {}
                for theAdd in deltaItem.added():
                    #print("property added: " + theAdd + " value: " + str(newManiDict[item][theAdd]))
                    itemDelta["adds"][theAdd] = newManiDict[item][theAdd]
            if len(deltaItem.removed()) > 0:
                itemDelta["cuts"] = {}
                for theCut in deltaItem.removed():
                    itemDelta["cuts"][theCut] = oldManiDict[item][theCut]
            if len(deltaItem.changed()) > 0:
                itemDelta["changes"] = {}
                for itemProp in deltaItem.changed():
                    if itemProp == "referencedItems":
                        oldRefs = set(oldManiDict[item][itemProp])
                        newRefs = set(newManiDict[item][itemProp])
                        refsCommon = newRefs & oldRefs
                        refsAdded = newRefs - oldRefs
                        refsRemoved = oldRefs - refsCommon
                        itemDelta["changes"][itemProp] = { "adds" : list(refsAdded), "cuts" : list(refsRemoved) }
                    elif itemProp == "checkSum":
                        itemDelta["changes"][itemProp] = {"newValue" : newManiDict[item]["fileName"], "oldValue" : oldManiDict[item]["fileName"]}
                    else:
                        itemDelta["changes"][itemProp] = {"newValue" : newManiDict[item][itemProp], "oldValue" : oldManiDict[item][itemProp]}
            jsonDelta["manifest"]["changes"][item] = itemDelta
            #print("   Manifest item id : " + item + " changed...")
            #print("   Manifest item delta: " + str(itemDelta))

    # print(" Calculated manifest changes: adds: " + str(len(jsonDelta["manifest"]["adds"])) + "; cuts: " + str(len(jsonDelta["manifest"]["cuts"])) + "; changes: " + str(len(jsonDelta["manifest"]["changes"])))
    jsonDelta["summary"]["itemChanges"] = len(jsonDelta["manifest"]["adds"]) + len(jsonDelta["manifest"]["cuts"]) + len(jsonDelta["manifest"]["changes"])
    if jsonDelta["summary"]["itemChanges"] == 0:
        jsonDelta["summary"]["encodedManiChanges"] = "Mani=="
    else:
        maniChanges = "Mani-"
        if len(jsonDelta["manifest"]["adds"]) > 0:
            maniChanges += "A" + str(len(jsonDelta["manifest"]["adds"]))
        else:
            maniChanges += "xA"
        if len(jsonDelta["manifest"]["cuts"]) > 0:
            maniChanges += "-R" + str(len(jsonDelta["manifest"]["cuts"]))
        else:
            maniChanges += "-xR"
        if len(jsonDelta["manifest"]["changes"]) > 0:
            maniChanges += "-C" + str(len(jsonDelta["manifest"]["changes"]))
        else:
            maniChanges += "-xC"
        jsonDelta["summary"]["encodedManiChanges"] = maniChanges
    return(jsonDelta)