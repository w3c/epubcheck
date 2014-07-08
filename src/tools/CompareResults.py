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
    jsonDelta["spine"] = {}
    jsonDelta["spine"]["adds"] = {}
    jsonDelta["spine"]["cuts"] = {}
    jsonDelta["spine"]["orderChanges"] = {}
    jsonDelta["spine"]["contentChanges"] = []
    jsonDelta["spine"]["unchanged"] = 0
    oldPub = oldJson["publication"]
    newPub = newJson["publication"]
    newSpine = {}
    newSpineContents = {}
    oldSpineContents = {}
    oldSpine = {}
    newMani = newJson["items"]
    oldMani = oldJson["items"]
    
    newManiDict = Dictionary.makeDict(newMani, "id")
    oldManiDict = Dictionary.makeDict(oldMani, "id")
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
    # compare the spines
    #
    for item in newMani:
        if item["isSpineItem"]:
            newSpine[item["id"]] = item["spineIndex"]
            newSpineContents[item["id"]] = item["checkSum"]
    
    for item in oldMani:
        if item["isSpineItem"]:
            oldSpine[item["id"]] = item["spineIndex"]
            oldSpineContents[item["id"]] = item["checkSum"]
    
    
    deltaSpine = Dictionary.DictCompare(newSpine, oldSpine)
    deltaSpineContents = Dictionary.DictCompare(newSpineContents, oldSpineContents)
    #
    # look for adds/cuts/order changes in the spine
    #
    if len(deltaSpine.added()) > 0:
        for item in deltaSpine.added():
            jsonDelta["spine"]["adds"][item] = newSpine[item]
    if len(deltaSpine.removed()) > 0:
        for item in deltaSpine.removed():
            jsonDelta["spine"]["cuts"][item] = oldSpine[item]
    if len(deltaSpine.changed()) > 0:
        jsonDelta["spine"]["orderChanges"] = {}
        for item in deltaSpine.changed():
            jsonDelta["spine"]["orderChanges"][item] = {}
            jsonDelta["spine"]["orderChanges"][item]["newSpineIndex"] = newSpine[item]
            jsonDelta["spine"]["orderChanges"][item]["oldSpineIndex"] = oldSpine[item]
    jsonDelta["spine"]["unchanged"] = len(deltaSpine.unchanged())
    #
    # look for content changes only in items common to both spines
    #
    if len(deltaSpineContents.changed()) > 0:
        for item in deltaSpineContents.changed():
            jsonDelta["spine"]["contentChanges"] = {}
            jsonDelta["spine"]["contentChanges"][item] = newManiDict[item]["fileName"]
    #
    # summarize the spine changes
    #
    if len(jsonDelta["spine"]["adds"]) + len(jsonDelta["spine"]["cuts"]) + len(jsonDelta["spine"]["orderChanges"]) + len(jsonDelta["spine"]["contentChanges"]) == 0:
        spineChanges = "Spine==-N" + str(newPub["nSpines"])
    else:
        spineChanges = "Spine-N" + str(newPub["nSpines"])
        if jsonDelta["spine"]["unchanged"] == 0:
            spineChanges += "-xU"
        else:
            spineChanges += "-U" + str(jsonDelta["spine"]["unchanged"])
        if len(jsonDelta["spine"]["adds"]) > 0:
            spineChanges += "-A" + str(len(jsonDelta["spine"]["adds"]))
        else:
            spineChanges += "-xA"
        if len(jsonDelta["spine"]["cuts"]) > 0:
            spineChanges += "-R" + str(len(jsonDelta["spine"]["cuts"]))
        else:
            spineChanges += "-xR"
        if len(jsonDelta["spine"]["orderChanges"]) > 0:
            spineChanges += "-OC" + str(len(jsonDelta["spine"]["orderChanges"]))
        else:
            spineChanges += "-xOC"
        if len(jsonDelta["spine"]["contentChanges"]) > 0:
            spineChanges += "-CC" + str(len(jsonDelta["spine"]["contentChanges"]))
        else:
            spineChanges += "-xCC"

    jsonDelta["summary"]["spineChanges"] = len(jsonDelta["spine"]["adds"]) + len(jsonDelta["spine"]["cuts"]) + len(jsonDelta["spine"]["orderChanges"]) + len(jsonDelta["spine"]["contentChanges"]) 
    jsonDelta["summary"]["encodedSpineChanges"] = spineChanges
    #
    # do the manifest
    #
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