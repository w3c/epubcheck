/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 2/14/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
goog.provide('EpubCheck.ColumnDefinition');

EpubCheck.ColumnDefinition = function (name, friendlyName, type, linkName, isEditable, validValues) {
  this.name = name;
  this.friendlyName = friendlyName;
  this.type = type;
  this.linkName = linkName;
  this.isEditable = isEditable;
  this.validValues = validValues;
  this.isId = false;
};

EpubCheck.ColumnDefinition.prototype.getName = function () {
  return this.name;
};

EpubCheck.ColumnDefinition.prototype.getFriendlyName = function () {
  return this.friendlyName;
};

EpubCheck.ColumnDefinition.prototype.getType = function () {
  return this.type;
};

EpubCheck.ColumnDefinition.prototype.getLinkName = function () {
  return this.linkName;
};

EpubCheck.ColumnDefinition.prototype.getIsEditable = function () {
  return this.isEditable;
};

EpubCheck.ColumnDefinition.prototype.getValidValues = function () {
  return this.validValues;
};

EpubCheck.ColumnDefinition.prototype.getIsId = function () {
  return this.isId;
};

EpubCheck.ColumnDefinition.prototype.setIsId = function (value) {
  this.isId = value;
};