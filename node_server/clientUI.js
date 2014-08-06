var currentResultData;
var tabBar;
var itemColumnDefinitions = [];
var messageColumnDefinitions = [];
var fileColumnDefinitions = [];
var checkMessageColumnDefinitions = [];
var lastViewed;
var socket;
var knownFiles = [];
var checkMessages = [];
var fileListViewer;
var checkMessageViewer;

var init = function () {
  itemColumnDefinitions.push(new EpubCheck.ColumnDefinition('fileName', "File Name", "string", "link"));
  var itemColumnId = new EpubCheck.ColumnDefinition('id', "ID", "string");
  itemColumnId.setIsId(true);
  itemColumnDefinitions.push(itemColumnId);
  itemColumnDefinitions.push(new EpubCheck.ColumnDefinition('media_type', "Mime Type", "string"));
  itemColumnDefinitions.push(new EpubCheck.ColumnDefinition('uncompressedSize', "Size", "number"));
  var messageColumnId = new EpubCheck.ColumnDefinition("ID", "ID", "string");
  messageColumnId.setIsId(true);
  messageColumnDefinitions.push(messageColumnId);
  messageColumnDefinitions.push(new EpubCheck.ColumnDefinition("severity", "Severity", "string"));
  messageColumnDefinitions.push(new EpubCheck.ColumnDefinition("message", "Message", "string"));
  fileColumnDefinitions.push(new EpubCheck.ColumnDefinition('name', "Name", "string"));
  var fileColumnId = new EpubCheck.ColumnDefinition('identifier', "ID", "string");
  fileColumnId.setIsId(true);
  fileColumnDefinitions.push(fileColumnId);
  fileColumnDefinitions.push(new EpubCheck.ColumnDefinition("title", "Title", "string"));
  fileColumnDefinitions.push(new EpubCheck.ColumnDefinition("publisher", "Publisher", "string"));
  fileColumnDefinitions.push(new EpubCheck.ColumnDefinition("timestamp", "Modified", "string"));
  checkMessageColumnId = new EpubCheck.ColumnDefinition('id', "ID", "string");
  checkMessageColumnId.setIsId(true);
  checkMessageColumnDefinitions.push(checkMessageColumnId);
  checkMessageColumnDefinitions.push(new EpubCheck.ColumnDefinition('severity', "Severity", "string", null, true, ['FATAL', 'ERROR', 'WARNING', 'USAGE', 'SUPPRESSED']));
  checkMessageColumnDefinitions.push(new EpubCheck.ColumnDefinition('message', "Message", "string", null, true));
  checkMessageColumnDefinitions.push(new EpubCheck.ColumnDefinition('suggestion', "Suggestion", "string", null, true));
  document.getElementById('epub').addEventListener('change', handleFileSelect, false);
  socket = io.connect(window.location.origin);
  var status = goog.dom.getElement('status');
  var error = goog.dom.getElement('error');
  socket.on('results_ready', function (data) {
    var fileInfo = JSON.parse(data);
    addKnownFile(fileInfo);
    hide_Spinner();
  });
  socket.on('status', function (data) {
    status.textContent = data;
  });
  socket.on('error', function (data) {
    error.textContent = data;
  });
  setupUI();
  hide_Spinner();
  process_parameters();
};

var process_parameters = function () {
  var uri = new goog.Uri(window.location.href);
  var epubParameter = uri.getParameterValue('epub');
  if (epubParameter)
  {
    var status = goog.dom.getElement('status');
    var check_finished = function(e) {
      status.textContent = 'Finished checking all files.';
      if (e.target.getStatus() !== 200)
      {
        var message = e.target.getResponseText();
        var errorElement = goog.dom.getElement('error');
        errorElement.textContent = 'Checking failed for ' + epubParameter + ' ' + message;
      }
    };

    var map = new goog.structs.Map;
    map.set('epub', epubParameter);
    status.textContent = 'Checking ' + epubParameter;
    goog.net.XhrIo.send('/check_epub_path', check_finished, 'POST', null, map);
  }
};

var toTimestamp = function (strDate) {
  var datum = Date.parse(strDate);
  return datum / 1000;
};

var addKnownFile = function (fileInfo) {
  if (!isKnownFile(fileInfo.name, fileInfo.timestamp))
  {
    knownFiles.push(fileInfo);
    if (fileListViewer)
    {
      fileListViewer.addRow(fileInfo, true);
    }
  }
};

var isKnownFile = function (name, timestamp) {
  var exists = false;
  for (var i = 0; i < knownFiles.length; i++)
  {
    var knownFile = knownFiles[i];
    if (knownFile.name === name && knownFile.timestamp === timestamp)
    {
      exists = true;
      break;
    }
  }
  return exists;
};

var setupUI = function () {
  tabBar = new goog.ui.TabBar();
  tabBar.decorate(goog.dom.getElement('tabBar'));
  setContent();
  // Handle SELECT events dispatched by tabs.
  goog.events.listen(tabBar, goog.ui.Component.EventType.SELECT,
    function (e) {
      setContent();
    });
};

var getResults = function (selection) {
  var receive_CheckResults = function (e) {
    if (e.target.getStatus() == 200)
    {
      clearContent();
      currentResultData = e.target.getResponseJson();
      setContent();
      setFileInfo();
    }
  };
  var map = new goog.structs.Map;
  map.set("publication", selection.name);
  map.set("timestamp", selection.timestamp);
  goog.net.XhrIo.send('/get_results', receive_CheckResults, "GET", null, map);
};

var getCheckMessages = function (callback) {
  var receive_CheckMessages = function (e) {
    if (e.target.getStatus() == 200)
    {
      checkMessages = e.target.getResponseJson();
    }
    if (callback)
    {
      callback();
    }
  };
  goog.net.XhrIo.send('/get_messages', receive_CheckMessages, "GET");
};

var handleFileSelect = function (evt) {
  var unknownFiles = 0;
  var files = evt.target.files;
  var formData = new FormData();
  var status = goog.dom.getElement('status');
  var check_finished = function (e) {
    status.textContent = "Finished checking all files.";
  };
  for (var i = 0, f; f = files[i]; i++)
  {
    if (!isKnownFile(f.name))
    {
      formData.append(f.name, f);
      formData.append(f.name + '_Timestamp', toTimestamp(f.lastModifiedDate));
      unknownFiles++;
    }
  }

  if (unknownFiles > 0)
  {
    status.textContent = "Sending " + files.length + " to the server.";
    goog.net.XhrIo.send('/check_epub', check_finished, 'POST', formData);
    show_Spinner();
  }
  else if (files.length > 0)
  {
    status.textContent = "Files have already been checked.";
  }
};

var show_Spinner = function () {
  var spinner = goog.dom.getElement('spinner');
  goog.style.setStyle(spinner, 'display', 'inline');
  var fileButton = goog.dom.getElement('epub');
  goog.dom.forms.setDisabled(fileButton, true);
};

var hide_Spinner = function () {
  var spinner = goog.dom.getElement('spinner');
  goog.style.setStyle(spinner, 'display', 'none');
  var fileButton = goog.dom.getElement('epub');
  goog.dom.forms.setDisabled(fileButton, false);
};

var setFileInfo = function () {
  if (currentResultData)
  {
    var publicationData = currentResultData['publication'];
    var checkerData = currentResultData['checker'];
    goog.dom.getElement('fileName').textContent = checkerData['filename'];
    goog.dom.getElement('publisher').textContent = publicationData['publisher'];
    goog.dom.getElement('title').textContent = publicationData['title'];
  }
};

var clearContent = function () {
  tabBar.forEachChild(function (child, index) {
    var caption = child.getCaption();
    if (caption !== "Files" && caption !== "Comparisons" && caption !== "Check Messages")
    {
      var contentElement = getContentElement(caption);
      clearHTML(contentElement);
    }
  });
};

var clearHTML = function (parentElement) {
  if (parentElement && parentElement.hasChildNodes())
  {
    while (parentElement.childNodes.length >= 1)
    {
      parentElement.removeChild(parentElement.lastChild);
    }
  }
};

var setContent = function () {
  if (tabBar)
  {
    if (lastViewed)
    {
      lastViewed.style.display = "none";
    }
    var currentTab = tabBar.getSelectedTab().getCaption();
    var contentElement = getContentElement(currentTab);
    contentElement.style.display = "inline";
    lastViewed = contentElement;
    if (currentTab == "Files")
    {
      if (contentElement.childElementCount === 0)
      {
        clearHTML(contentElement);
        fileListViewer = new EpubCheck.ListViewer();
        fileListViewer.render(contentElement, fileColumnDefinitions, knownFiles);
        fileListViewer.addSelectEventHandler(function (e) {
          var selection = fileListViewer.getSelection();
          if (selection)
          {
            getResults(selection);
          }
        });
      }
    }
    else if (currentTab == "Comparisons")
    {
      var resultsA = goog.dom.getElement("resultA");
      var resultsB = goog.dom.getElement("resultB");
      if (resultsA && resultsB)
      {
        clearHTML(resultsA);
        clearHTML(resultsB);
        for (var i = 0; i < knownFiles.length; i++)
        {
          var knownFile = knownFiles[i];
          var fileDate = new Date(knownFile.timestamp * 1000);
          var optionString = knownFile.name + " " + fileDate.toLocaleString();
          var option = goog.dom.createElement("option");
          option.textContent = optionString;
          option.value = i;
          goog.dom.appendChild(resultsA, option);
          goog.dom.appendChild(resultsB, option.cloneNode(true));
        }
      }
    }
    else if (currentTab == "Check Messages")
    {
      contentElement = goog.dom.getElement('check_messages_content_table');
      var populateMessages = function () {
        if (contentElement.childElementCount === 0)
        {
          checkMessageViewer = new EpubCheck.MasterDetailViewer(checkMessages);
          checkMessageViewer.render(contentElement, checkMessageColumnDefinitions);
          checkMessageViewer.addUpdateEventHandler(function (event) {
            var updatedObject = event.updated;
            for (var i = 0; i < checkMessages.length; i++)
            {
              if (checkMessages[i]['id'] === updatedObject['id'])
              {
                checkMessages[i] = updatedObject;
                break;
              }
            }

            goog.net.XhrIo.send('/set_messages', null, "POST", JSON.stringify(checkMessages), null);
          });
        }
      };
      if (checkMessages.length == 0)
      {
        getCheckMessages(populateMessages);
      }
      else
      {
        populateMessages();
      }
    }
    else if (currentResultData)
    {
      if (currentTab == "Publication")
      {
        if (contentElement.childElementCount === 0)
        {
          var content = new EpubCheck.ObjectViewer(currentResultData['publication']);
          content.render(contentElement);
        }
      }
      else if (currentTab == "Manifest Items")
      {
        if (contentElement.childElementCount === 0)
        {
          var manifestContent = new EpubCheck.MasterDetailViewer(currentResultData['items']);
          manifestContent.render(contentElement, itemColumnDefinitions);
        }
      }
      else if (currentTab == "Spine Items")
      {
        if (contentElement.childElementCount === 0)
        {
          var spineContent = new EpubCheck.MasterDetailViewer(getSpineItems());
          spineContent.render(contentElement, itemColumnDefinitions);
        }
      }
      else if (currentTab == "Messages")
      {
        if (contentElement.childElementCount === 0)
        {
          var messageContent = new EpubCheck.MasterDetailViewer(currentResultData['messages']);
          messageContent.render(contentElement, messageColumnDefinitions);
        }
      }
    }
    else
    {
      goog.dom.setTextContent(contentElement,
        'Please select results from the "File" tab.');
    }
  }
};

var getContentElement = function (tab) {
  switch (tab)
  {
    case 'Publication':
      return goog.dom.getElement("publication_content");
      break;
    case 'Messages':
      return goog.dom.getElement("messages_content");
      break;
    case 'Spine Items':
      return goog.dom.getElement("spine_items_content");
      break;
    case 'Manifest Items':
      return goog.dom.getElement("manifest_items_content");
      break;
    case 'Files':
      return goog.dom.getElement("files_content");
      break;
    case 'Check Messages':
      return goog.dom.getElement("check_messages_content");
      break;
    case 'Comparisons':
      return goog.dom.getElement("comparison_content");
      break;
    default:
      return null;
  }
};

var reset_check_messages = function () {
  var receive_CheckMessages = function (e) {
    if (e.target.getStatus() == 200)
    {
      checkMessages = e.target.getResponseJson();
      if (checkMessageViewer)
      {
        checkMessageViewer.refreshData(checkMessages);
      }
    }
  };
  goog.net.XhrIo.send('/reset_default_messages', receive_CheckMessages, "GET");
};

var getSpineItems = function () {
  var result = [];
  if (currentResultData)
  {
    var items = currentResultData['items'];
    for (var i = 0; i < items.length; i++)
    {
      var item = items[i];
      if (item["isSpineItem"])
      {
        result.push(item);
      }
    }
  }
  return result;
};

var handleCompareClick = function (evt) {
  var resultsA = goog.dom.getElement("resultA");
  var resultsB = goog.dom.getElement("resultB");
  if (resultsA && resultsB)
  {
    var valueA = resultsA.value;
    var valueB = resultsB.value;
    if (valueA === valueB)
    {
      alert("Please choose different files");
      return;
    }
    var receive_Diff = function (e) {

      if (e.target.getStatus() == 200)
      {
        var currentDiffJson = e.target.getResponseText();
        var diffContent = goog.dom.getElement("diffContent");
        clearHTML(diffContent);
        var preTag = goog.dom.createElement('pre');
        preTag.textContent = currentDiffJson;
        diffContent.appendChild(preTag);
      }
      else
      {
        var message = e.target.getResponseText();
        var diffErrorContent = goog.dom.getElement("diffErrorContent");
        diffErrorContent.textContent = "Error: " + e.target.getLastError() + " " + message;
      }
    };
    var pubA = knownFiles[valueA];
    var pubB = knownFiles[valueB];
    var map = new goog.structs.Map;
    map.set("publicationA", pubA.name);
    map.set("timestampA", pubA.timestamp);
    map.set("publicationB", pubB.name);
    map.set("timestampB", pubB.timestamp);
    goog.net.XhrIo.send('/get_comparison', receive_Diff, "GET", null, map);
  }
};
