var fs = require("fs");

var create = function() {
  fs.writeFile('style.', 'div{color:purple;}', function(err) {
    if (err) throw err;
    console.log('file created');
  });
};

var remove = function() {
  fs.unlink('style.', function() {
    console.log('file deleted');
    fs.exists('style.', function(exists) {
      console.log('Existance: ' + exists);
    });
  });
};

remove();
