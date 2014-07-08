/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 2/14/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
goog.provide('EpubCheck.ObjectViewer');
goog.require('goog.dom');
goog.require('goog.style');

EpubCheck.ObjectViewer = function (object, columns) {
  this.object = object;
  this.table = null;
  this.columns = columns;
  this.updateHandlers = [];
};

EpubCheck.ObjectViewer.prototype.render = function (parentElement) {
  if (!parentElement)
  {
    return;
  }

  while (parentElement.hasChildNodes())
  {
    parentElement.removeChild(parentElement.lastChild);
  }

  if (this.table)
  {
    parentElement.appendChild(this.table);
  }
  if (this.object)
  {
    this.table = goog.dom.createElement('table');
    goog.style.setStyle(this.table, 'width', '100%');
    var isEditable = false;
    for (var key in this.object)
    {
      if (this.object.hasOwnProperty(key))
      {
        var row = this.table.insertRow(-1);
        var cellKey = row.insertCell(-1);
        goog.style.setStyle(cellKey, 'vertical-align', 'top');
        var cellValue = row.insertCell(-1);
        cellKey.textContent = key;
        var column = null;
        if (this.columns)
        {
          for (var c = 0; c < this.columns.length; c++)
          {
            if (this.columns[c].getName() === key)
            {
              column = this.columns[c];
              break;
            }
          }
        }
        isEditable = isEditable || (column && column.getIsEditable());
        this.setValue(this.object[key], cellValue, column);
      }
    }
    if (isEditable)
    {
      var buttonRow = this.table.insertRow(-1);
      var buttonCell = buttonRow.insertCell(-1);
      buttonCell.setAttribute('colspan', '2');
      var button = goog.dom.createElement('input');
      button.setAttribute('type', 'button');
      button.value = 'Update';
      var this_ = this;
      button.onclick = function (event) {
        this_.fireUpdateEvent(event);
      };
      buttonCell.appendChild(button);
    }
    goog.dom.appendChild(parentElement, this.table);
  }
  else
  {
    parentElement.textContent = 'null';
  }
};

EpubCheck.ObjectViewer.prototype.fireUpdateEvent = function (e) {
  for (var c = 0; c < this.columns.length; c++)
  {
    var column = this.columns[c];
    if (column.getIsEditable())
    {
      var valueContainer = document.getElementById(column.getName());
      this.object[column.getName()] = valueContainer.value;
    }
  }
  e.updated = this.object;
  for (var h = 0; h < this.updateHandlers.length; h++)
  {
    this.updateHandlers[h](e);
  }
};

EpubCheck.ObjectViewer.prototype.addUpdateEventHandler = function (handler) {
  this.updateHandlers.push(handler);
};

EpubCheck.ObjectViewer.prototype.setValue = function (value, parentElement, column) {
  if (value instanceof Array)
  {
    for (var i = 0; i < value.length; i++)
    {
      this.setValue(value[i], parentElement);
      if (i < value.length - 1)
      {
        parentElement.appendChild(goog.dom.createElement('br'));
      }
    }
  }
  else if (value instanceof Object)
  {
    /*var subObject = new EpubCheck.ObjectViewer(value);
     subObject.render(parentElement);*/
    for (var key in value)
    {
      if (value.hasOwnProperty(key))
      {
        parentElement.appendChild(document.createTextNode(key + ': '));
        this.setValue(value[key], parentElement);
        parentElement.appendChild(goog.dom.createElement('br'));
      }
    }
  }
  else
  {
    if (column && column.getIsEditable())
    {
      var values = column.getValidValues();
      if (values)
      {
        // Create a drop box
        var select = goog.dom.createElement('select');
        select.value = value;
        select.id = column.getName();
        parentElement.appendChild(select);
        for (var v = 0; v < values.length; v++)
        {
          var option = goog.dom.createElement('option');
          option.appendChild(document.createTextNode(values[v]));
          option.value = values[v];
          if (values[v] === value)
          {
            option.setAttribute('selected', 'selected');
          }
          select.appendChild(option);
        }
      }
      else
      {
        //Create a text box
        var input = goog.dom.createElement('input');
        input.id = column.getName();
        input.setAttribute('type', 'text');
        input.value = value;
        input.style.setProperty('width', '90%');
        parentElement.appendChild(input);
      }
    }
    else
    {
      parentElement.appendChild(document.createTextNode(value));
    }
  }
};