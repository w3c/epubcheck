/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 2/14/13
 * Time: 4:32 PM
 * To change this template use File | Settings | File Templates.
 */

goog.provide('EpubCheck.MasterDetailViewer');
goog.require('goog.dom');
goog.require('goog.style');

EpubCheck.MasterDetailViewer = function (list) {
  this.list = list;
  this.table = null;
  this.master = new EpubCheck.ListViewer();
  this.detail = null;
  this.updateHandlers = [];
  this.cellMaster_ = null;
  this.columns = [];
};

EpubCheck.MasterDetailViewer.prototype.render = function (parentElement, columns) {
  if (!parentElement)
  {
    return;
  }

  while (parentElement.hasChildNodes())
  {
    parentElement.removeChild(parentElement.lastChild);
  }

  this.columns = columns;

  this.table = goog.dom.createElement('table');
  goog.style.setWidth(this.table, "100%");
  goog.dom.appendChild(parentElement, this.table);
  var row = this.table.insertRow(-1);

  //Set up Master column
  this.cellMaster_ = row.insertCell(-1);
  this.cellMaster_.style.verticalAlign = "top";
  this.cellMaster_.style.border = "thin solid black";
  var width = parentElement.getBoundingClientRect().width * .69;
  goog.style.setWidth(this.cellMaster_, width + "px");
  goog.style.setHeight(this.cellMaster_, "600px");
  this.master.render(this.cellMaster_, columns, this.list);

  //Set up Detail column
  var cellDetail = row.insertCell(-1);
  cellDetail.style.border = "thin solid black";
  cellDetail.style.verticalAlign = "top";
  var divDetail = goog.dom.createElement("div");
  var detailWidth = parentElement.getBoundingClientRect().width * .29;
  goog.style.setWidth(divDetail, detailWidth + "px");
  goog.style.setHeight(divDetail, "600px");
  divDetail.style.overflow = "auto";
  goog.dom.appendChild(cellDetail, divDetail);
  divDetail.textContent = "Select an Item";

  goog.dom.appendChild(parentElement, this.table);
  this.master.addSelectEventHandler(selectHandler);
  var this_ = this;

  function selectHandler(e) {
    var index = this_.master.getSelectedIndex();
    if (index > -1)
    {
      this_.detail = new EpubCheck.ObjectViewer(this_.list[index], columns);
      for (var h = 0; h < this_.updateHandlers.length; h++)
      {
        this_.detail.addUpdateEventHandler(this_.updateHandlers[h]);
      }
      this_.detail.addUpdateEventHandler(function (event) {
        this_.master.updateRow(event.updated);
      });
      this_.detail.render(divDetail);
    }
    else
    {
      divDetail.textContent = "Select an Item";
    }
  }
};

EpubCheck.MasterDetailViewer.prototype.addUpdateEventHandler = function (handler) {
  this.updateHandlers.push(handler);
};


EpubCheck.MasterDetailViewer.prototype.refreshData = function (list) {
  this.list = list;
  this.master.render(this.cellMaster_, this.columns, this.list);
};