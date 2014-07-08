/**
 * Created with JetBrains WebStorm.
 * User: apond
 * Date: 1/15/13
 * Time: 11:34 AM
 * This node script was used to generate the files needed for the Excessive Spine Items test.
 */
var fs = require('fs');
var path = require('path');

main = function() {
  var destination = "Excessive_Spine_Items_epub3/OPS";
  var templateHtml = '<?xml version="1.0"?>\n' +
    '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">\n' +
    '  <head>\n' +
    '    <title>Excessive Spine Items Page page#</title>\n' +
    '  </head>\n' +
    '  <body>\n' +
    '    <div>\n' +
    '      <p>Excessive Spine Items Page page#</p>\n' +
    '    </div>\n' +
    '  </body>\n' +
    '</html>';
  var manifestItems = "";
  var itemRefs = "";
  for (var i = 1; i < 101; i++)
  {
    var index = i.toString();
    while (index.length < 3)
    {
      index = '0' + index;
    }
    var spineItem = path.join(destination, "page" + index + ".xhtml");

    fs.writeFile(spineItem, templateHtml.replace(/page#/g, i), function(err) {
      if (err)
      {
        console.log(err);
      }
    });
    manifestItems += '<item id="page' + index +'" href="page'+index+'.xhtml" media-type="application/xhtml+xml" />\n';
    itemRefs +='<itemref idref="page'+index+'" />\n';
  }
  var manifestFile = path.join(destination, "manifest.txt");
  fs.writeFile(manifestFile, manifestItems, function(err) {
    if (err)
    {
      console.log(err);
    }
  });
  var itemRefFile = path.join(destination, "itemRefs.txt");
  fs.writeFile(itemRefFile, itemRefs, function(err) {
    if (err)
    {
      console.log(err);
    }
  });
};
main();