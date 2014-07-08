/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 2/22/13
 * Time: 10:54 AM
 * Class that represents an epubcheck check message
 */

var EpubCheck = EpubCheck || {};

EpubCheck.CheckMessage = function (id, severity, message, suggestion) {
  this.id = id;
  this.severity = severity;
  this.message = message;
  this.suggestion = suggestion;
};

EpubCheck.CheckMessage.prototype.getId = function () {
  return this.id;
};

EpubCheck.CheckMessage.prototype.getSeverity = function () {
  return this.severity;
};

EpubCheck.CheckMessage.prototype.getMessage = function () {
  return this.message;
};

EpubCheck.CheckMessage.prototype.getSuggestion = function () {
  return this.getSuggestion();
};

module.exports = EpubCheck.CheckMessage;
