var http = require('http');
var fs = require('fs');
var path = require('path');
var socketIO = require('socket.io');
var express = require('express');
var spawn = require('child_process').spawn;
var mkdirp = require('mkdirp');
var CheckedFile = require('./CheckedFile.js');
var CheckMessage = require('./CheckMessage.js');
var lineReader = require('line-reader');
var program = require('commander');
var formidable = require('formidable');
var serveStatic = require('serve-static');

var jarFileName = 'epubcheck.jar';
var compareFileName = 'CompareResultsUtil.py';
var comparePythonPath = path.join(__dirname, '..', compareFileName);
var jarFilePath = path.join(__dirname, '..', jarFileName);
var debug_output = false;

if (!fs.existsSync(jarFilePath))
{
  console.warn('Could not find ' + jarFilePath + '.');
  jarFilePath = path.join(__dirname, '../target', jarFileName);
  comparePythonPath = path.join(__dirname, '../target', compareFileName);
  console.warn('Trying local build path ' + jarFilePath + '...');
}

if (!fs.existsSync(jarFilePath))
{
  console.error('Could not find ' + jarFilePath + '!  Exiting.');
  process.exit(1);
}

var checkedFiles = [];
var checkMessages = [];
var io;
var epubDirectory = path.join(__dirname, './temp/epubs');
var diffDirectory = path.join(__dirname, './temp/epubs/diffs');
var uploadDirectory = path.join(__dirname, './temp/uploads');
var checkMessageOverrideFile = path.join(epubDirectory, 'CheckMessages.txt');

/**
 *
 * @param port - The port number the server should listen too
 * @param override - The file used to override epubcheck messages.
 * @param express_app - This needs to be provided when you want to extend your server with the server so you don't have to proxy
 * @param http_server - When supplying the express_app you need to also specify the http server
 * @param root - The root path used for responding with index.html
 * @returns {*}
 */
var main = function (port, override, express_app, http_server, root) {
  initialize_directories();
  var initialize_check_messages = function () {
    fs.exists(checkMessageOverrideFile, function (exists) {
      if (exists)
      {
        read_check_messages();
      }
      else
      {
        get_default_check_messages(function (success) {
          if (!success)
          {
            console.error('Could not generate the check message file.');
          }
        });
      }
    });
  };

  if (override)
  {
    override = path.join('./', override);
    fs.exists(override, function (exists) {
      if (exists)
      {
        var readStream = fs.createReadStream(override);
        var writeStream = fs.createWriteStream(checkMessageOverrideFile, { flags:'w',
          encoding:'UTF-8',
          mode:0666 });
        readStream.pipe(writeStream);

      }
      initialize_check_messages();
    });
  }
  else
  {
    initialize_check_messages();
  }

  var app = express_app || express();
  var server = http_server || http.createServer(app);
  configure_app(app, root);
  app.use(function(req, res, next) {
    var url = req.url;
    debug('unhandled: ' + url);
    next();
  });
  if (!http_server)
  {
    server.listen(port);
    console.log('Server is listening on port ' + port);
  }

  io = socketIO.listen(server);
  io.sockets.on('connection', function(socket) {
    debug("Socket connection: " + socket.id);

    for (var key = 0; key < checkedFiles.length; key++)
    {
      io.sockets.emit('results_ready', checkedFiles[key].stringify());
      debug('results_ready for ' + checkedFiles[key].getName());
    }
    socket.on('disconnect', function() {
      debug("Socket disconnect: " + socket.id);
    });
    socket.on('error', function(err) {
      debug('socket error (' + socket.id + '): ' + err);
    });
  });

  server.checkMessageFile = checkMessageOverrideFile;
  server.epubDir = epubDirectory;
  server.diffDir = diffDirectory;
  return server;
};

var configure_app = function(app, opt_root)
{
  app.use(function(req, res, next) {
    var url = req.url;
    debug('request: ' + url);

    res.on('finish', function() {
      debug('response ' + res.statusCode + ' sent: ' + url);
    });
    next();
  });
  app.use(common_epub_types);
  app.use(serveStatic(__dirname));
  app.use('/closure', serveStatic(path.join(__dirname, '../node_modules/closure-library')));

  var root = opt_root || '';
  app.get('/' + root, function (req, res) {
    res.sendfile(path.join(__dirname, 'index.html'));
  });

  app.get('/get_results', function (req, res) {
    var pub = req.headers.publication;
    var timestamp = req.headers.timestamp;
    if (pub && timestamp)
    {
      var results = getResults(pub, timestamp);
      if (results)
      {
        fs.readFile(results.getResultPath(), function (err, data) {
          if (err)
          {
            error_response(res, err);
          }
          else
          {
            data = add_links(results.getOutputFolder(), data);
            res.writeHead(200, {
              //'Content-Length':data.length,
              'Content-Type':'text/json' });
            res.write(data);
            res.end();
          }
        });
      }
      else
      {
        error_response(res, 'Entry not found for ' + pub);
      }
    }
    else
    {
      error_response(res, "The parameters 'publication' and 'timestamp' are required.");
    }
  });

  app.post('/check_epub_path', function (req, res) {
    var epub_path = req.headers.epub;
    if (epub_path)
    {
      epub_path = path.resolve(__dirname, epub_path);
      fs.exists(epub_path, function(exists){
        if (exists)
        {
          fs.stat(epub_path, function(err, stats){
            if (err)
            {
              error_response(res, 'Error reading file ' + epub_path);
              return;
            }
            if (!stats.isFile())
            {
              error_response(res, 'Path is not a file ' + epub_path);
              return;
            }

            var file = {};
            var name = path.basename(epub_path);
            file.timestamp = (stats.mtime / 1000).toString();
            file.name = name;
            file.path = epub_path;
            var files={};
            files[name] = file;
            var results = getResults(file.name, file.timestamp);
            if (results)
            {
              file.done = true;
              io.sockets.emit('results_ready', results.stringify());
              debug('results_ready for ' + results.getName());
              checkDone(files, res);
            }
            else
            {
              io.sockets.emit('status', 'Uploading ' + file.name);
              debug('Uploading ' + file.name);
              saveEpub(file, file.timestamp, files, res, false, saveFinished);
            }
          });
        }
        else
        {
          error_response(res, 'Could not find file ' + epub_path);
        }
      });
    }
    else
    {
      error_response(res, "The 'epub' parameters is required.");
    }
  });

  app.post('/check_epub', function (req, res) {
    var form = new formidable.IncomingForm();
    form.parse(req, function (err, fields, files) {
      for (var f in files)
      {
        if (files.hasOwnProperty(f))
        {
          var file = files[f];
          var tmp_path = file.path;
          var timestampProperty = file.name + '_Timestamp';
          var timestamp = fields[timestampProperty];
          if (!timestamp)
          {
            io.sockets.emit('error', 'Missing timestamp parameter (' + timestampProperty + ') in request.');
            file.done = true;
            continue;
          }
          file.timestamp = timestamp;
          var results = getResults(file.name, timestamp);
          if (results)
          {
            fs.unlink(tmp_path);
            file.done = true;
            io.sockets.emit('results_ready', results.stringify());
            debug('results_ready for ' + results.getName());
          }
          else
          {
            io.sockets.emit('status', 'Uploading ' + file.name);
            debug('Uploading ' + file.name);
            saveEpub(file, timestamp, files, res, true, saveFinished);
          }
        }
      }
      checkDone(files, res);
    });
  });

  app.get('/reset_default_messages', function (req, res) {
    get_default_check_messages(function (success) {
      if (success)
      {
        res.writeHead(200, {
          //'Content-Length':data.length,
          'Content-Type':'text/json' });
        res.write(JSON.stringify(checkMessages));
        res.end();
      }
      else
      {
        error_response(res, 'The default messages could not be generated');
      }
    });
  });

  app.get('/get_messages', function (req, res) {
    res.writeHead(200, {
      //'Content-Length':data.length,
      'Content-Type':'text/json' });
    res.write(JSON.stringify(checkMessages));
    res.end();
  });

  app.post('/set_messages', function (req, res) {
    req.rawBody = '';
    req.on('data', function(chunk) {
      req.rawBody += chunk;
    });

    req.on('end', function() {
      var newMessages = JSON.parse(req.rawBody);
      if (newMessages instanceof Array)
      {
        //TODO: Verify valid
        checkMessages = newMessages;
        write_check_messages();
        success_response(res);
      }
      else
      {
        error_response(res, "The check messages were not in the expected format.")
      }
    });
  });

  app.get('/get_comparison', function (req, res) {
    var pubA = req.headers.publicationa;
    var timestampA = req.headers.timestampa;
    var pubB = req.headers.publicationb;
    var timestampB = req.headers.timestampb;
    if (pubA && timestampA && pubB && timestampB)
    {
      var resultsA = getResults(pubA, timestampA);
      if (!resultsA)
      {
        error_response(res, 'The results for publication ' + pubA + ' with timestamp ' + timestampB + ' could not be found.');
        return;
      }

      var resultsB = getResults(pubB, timestampB);
      if (!resultsB)
      {
        error_response(res, 'The results for publication ' + pubB + ' with timestamp ' + timestampB + ' could not be found.');
        return;
      }

      run_comparison(resultsA, resultsB, function (output) {
        if (output)
        {
          fs.readFile(output, function (err, data) {
            if (err)
            {
              error_response(res, err);
            }
            else
            {
              res.writeHead(200, {
                //'Content-Length':data.length,
                'Content-Type':'text/json' });
              res.write(data);
              res.end();
            }
          });
        }
        else
        {
          error_response(res, 'The comparison failed.');
        }
      });
    }
    else
    {
      error_response(res, "The parameters 'publicationA' 'timestampA' 'publicationB' and 'timestampB' are required.");
    }
  });
};

var debug = function(message)
{
  if (debug_output)
  {
    console.log('[CheckServer] ' + new Date().toUTCString() + ' ' + message);
  }
};

var run_comparison = function (resultsA, resultsB, callback) {
  var resultAPath = resultsA.getResultPath();
  var resultBPath = resultsB.getResultPath();
  var output = path.join(diffDirectory, path.basename(resultAPath) + '_' + path.basename(resultBPath) + '.json');
  fs.exists(output, function (exists) {
    if (exists)
    {
      if (callback)
      {
        callback(output);
      }
    }
    else
    {
      var compare = spawn('python', [comparePythonPath, resultAPath, resultBPath, '-r', output]);
      compare.on('exit', function (code) {
        fs.exists(output, function (exists) {
          if (callback)
          {
            if (exists)
            {
              callback(output);
            }
            else
            {
              callback(null);
            }
          }
        });
      });
    }
  });
};

var initialize_directories = function () {
  mkdirp.sync(epubDirectory);
  mkdirp.sync(diffDirectory);
  mkdirp.sync(uploadDirectory);
  initialize_checked_epubs();
};

var common_epub_types = function (req, res, next) {

  if (req.method === 'GET' && req.url.indexOf('/temp/epubs/') === 0)
  {
    var extension = path.extname(req.url);
    if (extension)
    {
      extension = extension.toLowerCase();
      if (extension === '.opf' || extension === '.ncx')
      {
        res.contentType('xml');
        res.sendfile(path.join(__dirname, req.url));
        return;
      }
    }
    else
    {
      var basename = path.basename(req.url);
      if (basename === 'metadata')
      {
        res.contentType('txt');
        res.sendfile(path.join(__dirname, req.url));
        return;
      }
    }
  }
  next();
};

var add_links = function (outputFolder, data) {
  if (data)
  {
    var baseDir = path.relative(__dirname, outputFolder);
    var json = JSON.parse(data);
    var items = json['items'];
    if (items)
    {
      for (var i = 0; i < items.length; i++)
      {
        var item = items[i];
        item.link = path.join(baseDir, item['fileName']);
      }
    }
  }
  return JSON.stringify(json);
};

var saveFinished = function (err, file, timestamp, files, res) {
  var name = path.basename(file);
  if (!err)
  {
    io.sockets.emit('status', 'Checking ' + name);
    debug('Checking ' + name);
    check_epub(file, timestamp, files, res, checkFinished);
  }
  else
  {
    files[name].done = true;
    checkDone(files, res);
  }
};

var checkFinished = function (success, file, timestamp, output, files, res) {
  var name = path.basename(file);
  io.sockets.emit('status', 'Finished checking ' + name);
  debug('Finished checking ' + name + ' result was ' + success);
  if (success)
  {
    unzip_epub(file, timestamp, function (success2, outputFolder) {
      add_checked_file(output, outputFolder, function () {
        files[name].done = true;
        checkDone(files, res);
      });
      fs.unlink(file);
    });
  }
  else
  {
    files[name].done = true;
    checkDone(files, res);
    fs.unlink(file);
  }
};

var checkDone = function (files, res) {
  var data = '[';
  var index = 0;
  for (var f in files)
  {
    if (files.hasOwnProperty(f))
    {
      var file = files[f];
      if (!file.done)
      {
        return;
      }
      var timestamp = file.timestamp;
      var result = getResults(file.name, timestamp);
      if (result)
      {
        if (index > 0)
        {
          data += ',';
        }
        data += result.stringify();
      }
      index++;
    }
  }
  data += ']';
  res.writeHead(200, {
    'Content-Type':'text/json' });
  res.write(data);
  res.end();
};

var getResults = function (file, timestamp) {
  var result = null;
  for (var i = 0; i < checkedFiles.length; i++)
  {
    var checkedFile = checkedFiles[i];
    if (checkedFile.getName() === file && checkedFile.getTimestamp() === timestamp)
    {
      result = checkedFile;
      break;
    }
  }
  return result;
};

var saveEpub = function (file, timestamp, files, res, should_delete, callback) {
  var target_path = path.join(epubDirectory, file.name);
  var tmp_path = file.path;

  var newFile = fs.createWriteStream(target_path);
  var oldFile = fs.createReadStream(tmp_path);

  oldFile.pipe(newFile);

  if (should_delete)
  {
    fs.unlink(tmp_path, function(err) {
      if (err)
      {
        debug('Error updating file. ' + err);
      }
    });
  }

  callback(null, target_path, timestamp, files, res);
};

var success_response = function (res) {
  res.writeHead(200);
  res.write('success');
  res.end();
};

var error_response = function (res, error) {
  res.writeHead(500);
  res.write(error);
  res.end();
};

var unzip_epub = function (epubPath, timestamp, callback) {
  var output = epubPath + timestamp;
  var unzip = spawn('unzip', [epubPath, '-d', output]);
  unzip.on('exit', function (code) {
    fs.exists(output, function (exists) {
      if (callback)
      {
        callback(exists, output);
      }
    });
  });
};

var check_epub = function (epubPath, timestamp, files, res, callback) {
  var output = epubPath + timestamp + '.json';
  var parameters = ['-jar', jarFilePath, epubPath, '-u', '-j', output];

  fs.exists(checkMessageOverrideFile, function (exists) {
    if (exists)
    {
      parameters.push('-c');
      parameters.push(checkMessageOverrideFile);
    }

    var check = spawn('java', parameters);
    check.on('exit', function (code) {
      debug('epubcheck exited with code ' + code);
      fs.exists(output, function (exists) {
        if (callback)
        {
          callback(exists, epubPath, timestamp, output, files, res);
        }
      });
    });
  });
};

var add_checked_file = function (json_path, output_folder, callback) {
  var checkedFile = new CheckedFile(json_path, output_folder);
  checkedFile.initialize(function () {
    io.sockets.emit('results_ready', checkedFile.stringify());
    debug('results_ready for ' + checkedFile.getName());
    checkedFiles.push(checkedFile);
    if (callback)
    {
      callback();
    }
  });
};

var initialize_checked_epubs = function () {
  fs.readdir(epubDirectory, function (err, files) {
    for (var f = 0; f < files.length; f++)
    {
      var fullPath = path.join(epubDirectory, files[f]);
      var extension = path.extname(fullPath);
      if (extension === '.json')
      {
        add_checked_file(fullPath, fullPath.substr(0, fullPath.lastIndexOf(extension)));
      }
    }
  });
};

var read_check_messages = function (callback) {
  var headers = false;
  checkMessages = [];
  lineReader.eachLine(checkMessageOverrideFile, function (line, last) {
    if (headers)
    {
      var values = line.split("\t");
      checkMessages.push(new CheckMessage(values[0], values[1], values[2], values[3]))
    }
    else
    {
      // The first line in the file should be the header information
      headers = true;
    }
    if (last && callback)
    {
      callback();
    }
  });
};

var get_default_check_messages = function (callback) {
  var check = spawn('java', ['-jar', jarFilePath, '-l', checkMessageOverrideFile]);
  check.on('exit', function (code) {
    fs.exists(checkMessageOverrideFile, function (exists) {
      if (exists)
      {
        read_check_messages(function () {
          callback(true)
        });
      }
      else
      {
        if (callback)
        {
          callback(false);
        }
      }
    });
  });
};

var write_check_messages = function () {
  var data = "ID\tSeverity\tMessage\tSuggestion\n";
  for (var m = 0; m < checkMessages.length; m++)
  {
    var message = checkMessages[m];
    data += message.id + '\t';
    data += message.severity + '\t';
    data += message.message + '\t';
    data += message.suggestion + '\n';
  }
  fs.writeFile(checkMessageOverrideFile, data);
};

var add_options = function (commander) {
  commander
    .version('0.0.1')
    .option('-p, --port <port>', 'Run the checkserver on port [port]', 8080)
    .option('-o, --override <file>', 'Use the epubcheck override file specified', null)
};

if (require.main === module)
{
  add_options(program);
  program.parse(process.argv);
  main(program.port, program.override);
}

module.exports.main = main;
