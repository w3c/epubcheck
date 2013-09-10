/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 2/14/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
goog.provide('EpubCheck.ListViewer');
goog.require('goog.dom');
goog.require('goog.ui.TableSorter');


EpubCheck.ListViewer = function () {
  this.list = [];
  this.table = null;
  this.parentElement = null;
  this.columns = null;
  this.dataTable = null;
};

EpubCheck.ListViewer.prototype.render = function (parentElement, columns, initialList) {
  this.parentElement = parentElement;
  this.columns = columns;
  if (!parentElement || !columns)
  {
    return;
  }
  this.draw_(initialList);
};

EpubCheck.ListViewer.prototype.getSelection = function () {
  var selection = this.table.getSelection();
  if (selection.length > 0)
  {
    var index = selection[0].row;
    var row = this.list[index];
    return(row);
  }
  return null;
};

EpubCheck.ListViewer.prototype.getSelectedIndex = function () {
  var selection = this.table.getSelection();
  if (selection.length > 0)
  {
    return selection[0].row;
  }
  return -1;
};

EpubCheck.ListViewer.prototype.addRow = function (listItem, refresh) {
  var rowData = [];
  this.list.push(listItem);
  for (var column in this.columns)
  {
    var columnValue = this.columns[column];
    var columnName = columnValue.getName();
    if (columnValue.getLinkName())
    {
      var link = '<a href="' + listItem[columnValue.getLinkName()] + '" target="_blank">'
        + listItem[columnName] + '</a>';
      rowData.push(link);
    }
    else
    {
      var itemValue = "";
      if (columnName == "timestamp")
      {
        itemValue = new Date(listItem[columnName] * 1000).toLocaleString();
      } else
      {
        itemValue = listItem[columnName];
      }
      rowData.push(itemValue);
    }
  }
  this.dataTable.addRow(rowData);
  if (refresh)
  {
    this.drawTable_();
  }
};

EpubCheck.ListViewer.prototype.addSelectEventHandler = function (handler) {
  google.visualization.events.addListener(this.table, 'select', handler);
};

EpubCheck.ListViewer.prototype.updateRow = function (updatedObject) {
  var filter = [];
  for (var c = 0; c < this.columns.length; c++)
  {
    var column = this.columns[c];
    if (column.getIsId())
    {
      filter.push({column:c, value:updatedObject[column.getName()]});
    }
  }
  var rows = this.dataTable.getFilteredRows(filter);
  if (rows && rows.length === 1)
  {
    var row = rows[0];
    for (c = 0; c < this.columns.length; c++)
    {
      var col = this.columns[c];
      if (col.getIsEditable())
      {
        this.dataTable.setValue(row, c, updatedObject[col.getName()]);
      }
    }
    this.drawTable_();
  }
};

EpubCheck.ListViewer.prototype.draw_ = function (initialList) {

  while (this.parentElement.hasChildNodes())
  {
    this.parentElement.removeChild(this.parentElement.lastChild);
  }

  this.table = new google.visualization.Table(this.parentElement);
  this.dataTable = new google.visualization.DataTable();
  for (var col in this.columns)
  {
    var colValue = this.columns[col];
    this.dataTable.addColumn(colValue.getType(), colValue.getFriendlyName());
  }
  if (initialList)
  {
    for (var l = 0; l < initialList.length; l++)
    {
      var listItem = initialList[l];
      this.addRow(listItem, false);
    }
    this.drawTable_();
  }
  else
  {
    this.parentElement.textContent = "null";
  }
};

EpubCheck.ListViewer.prototype.drawTable_ = function () {
  this.table.draw(this.dataTable, {showRowNumber:false, allowHtml:true});
};
