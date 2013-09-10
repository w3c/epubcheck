/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 2/22/13
 * Time: 10:54 AM
 * To change this template use File | Settings | File Templates.
 */
var fs = require('fs');
var path = require('path');
var EpubCheck = EpubCheck || {};

EpubCheck.CheckedFile = function (resultPath, output_folder) {
  this.resultPath = path.relative(__dirname, resultPath);
  this.outputFolder = output_folder;
  this.name = "";
  this.title = "";
  this.identifier = "";
  this.publisher = "";
  var fileName = path.basename(resultPath, '.json');
  var startIndex = fileName.lastIndexOf('.epub') + 5;
  this.timestamp = fileName.substr(startIndex);
};


EpubCheck.CheckedFile.prototype.initialize = function (callback) {
  var currentResultData;
  var this_ = this;
  fs.readFile(this.getResultPath(), function (err, data) {
    if (err) throw err;
    currentResultData = JSON.parse(data);
    var publicationData = currentResultData['publication'];
    this_.name = currentResultData['checker']['filename'];
    this_.title = publicationData['title'];
    this_.identifier = publicationData['identifier'];
    this_.publisher = publicationData['publisher'];
    if (callback)
    {
      callback();
    }
  });
};

EpubCheck.CheckedFile.prototype.stringify = function () {
  return JSON.stringify(this, function (key, val) {
    if (key !== "resultPath" && key !== "outputFolder")
    {
      return val;
    }
  });
};

EpubCheck.CheckedFile.prototype.getName = function () {
  return this.name;
};

EpubCheck.CheckedFile.prototype.getTitle = function () {
  return this.title;
};

EpubCheck.CheckedFile.prototype.getIdentifier = function () {
  return this.identifier;
};

EpubCheck.CheckedFile.prototype.getResultPath = function () {
  return path.join(__dirname, this.resultPath);
};

EpubCheck.CheckedFile.prototype.getTimestamp = function () {
  return this.timestamp;
};

EpubCheck.CheckedFile.prototype.getOutputFolder = function () {
  return this.outputFolder;
};

module.exports = EpubCheck.CheckedFile;
