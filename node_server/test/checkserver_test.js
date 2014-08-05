var request = require('supertest')
  , app = require('../checkserver').main(7777)
  , assert = require("assert");
var ioClient = require("socket.io-client");
var fs = require('fs');
var path = require('path');
var exec = require('child_process').exec,
  child;
var expectedMessageCount = 210;
var debug = true;

describe('checkserver tests', function () {
  var messages = [];
  var timestamp;
  var resultInfo = {};
  var messageBackup = '';
  if (debug)
  {
    var socket = ioClient.connect('http://localhost:7777');
    socket.on('status', function (data) {
      console.log(data);
    });
    socket.on('error', function (data) {
      console.log(data);
    });
  }

  before(function (done) {
    timestamp = new Date().getTime().toString();
    messageBackup = app.checkMessageFile + '.bak';
    fs.exists(app.checkMessageFile, function (exists) {
      if (exists)
      {
        fs.rename(app.checkMessageFile, messageBackup, function () {
          done();
        });
      }
      else
      {
        done();
      }
    });
  });

  after(function (done) {
    var testEpubDir = path.join(app.epubDir, 'Toy.epub' + timestamp);
    var testEpub2Dir = path.join(app.epubDir, 'Toy_modified.epub' + timestamp);
    fs.exists(messageBackup, function (exists) {
      if (exists)
      {
        fs.renameSync(messageBackup, app.checkMessageFile);
      }
      child = exec('rm -rf ' + testEpubDir, function (err, out) {
        testEpubDir += ".json";
        fs.unlink(testEpubDir, function () {
          child = exec('rm -rf ' + testEpub2Dir, function (err, out) {
            testEpub2Dir += ".json";
            fs.unlink(testEpub2Dir, function () {
              var diffFile = path.join(app.diffDir, path.basename(testEpubDir) + '_' + path.basename(testEpub2Dir) + '.json');
              fs.unlink(diffFile, function () {
                done();
              });
            });
          });
        });
      });
    });
  });

  describe('get root', function () {
    it('GET / should return 200', function (done) {
      request(app)
        .get('/')
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          done();
        })

    });
  });

  describe('reset messages', function () {
    it('GET /reset_default_messages should return default messages', function (done) {
      request(app)
        .get('/reset_default_messages')
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          messages = JSON.parse(res.text);
          assert.equal(messages.length, expectedMessageCount);
          var firstMessage = messages[0];
          assert.equal(firstMessage.id, 'ACC-001');
          assert.equal(firstMessage.severity, 'WARNING');
          done();
        });
    });
  });

  describe('set messages', function () {
    it('POST /set_messages should update the messages', function (done) {
      messages[0].severity = "USAGE";
      messages[0].message = "持";
      request(app)
        .post('/set_messages')
        .send(JSON.stringify(messages))
        .set('content-type', 'application/json; charset=UTF-8')
        .expect(200)
        .end(function (err, res)
        {
          log_err(err, res);
          assert.equal(err, null);
          done();
        });
    });
  });

  describe('get messages', function () {
    it('GET /get_messages should return new updated messages', function (done) {
      request(app)
        .get('/get_messages')
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          messages = JSON.parse(res.text);
          assert.equal(messages.length, expectedMessageCount);
          var firstMessage = messages[0];
          assert.equal(firstMessage.severity, 'USAGE');
          assert.equal(firstMessage.message, '持');
          done();
        });
    });
  });

  describe('reset messages', function () {
    it('GET /reset_default_messages should return default messages', function (done) {
      request(app)
        .get('/reset_default_messages')
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          messages = JSON.parse(res.text);
          assert.equal(messages.length, expectedMessageCount);
          var firstMessage = messages[0];
          assert.equal(firstMessage.id, 'ACC-001');
          assert.equal(firstMessage.severity, 'WARNING');
          done();
        });
    });
  });

  describe('Check a simple epub', function () {
    it('POST /check_epub should return epub result information', function (done) {
      request(app)
        .post('/check_epub')
        .field('Toy.epub_Timestamp', timestamp)
        .field('Toy_modified.epub_Timestamp', timestamp)
        .attach('Toy.epub', 'node_server/test/Toy.epub')
        .attach('Toy_modified.epub', 'node_server/test/Toy_modified.epub')
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          var results = JSON.parse(res.text);
          assert.equal(results.length, 2);
          var toyResultFound = false;
          
          resultInfo = results[0];
          debug_dump(resultInfo, 'resultInfo1');
          if (resultInfo.name ==='Toy.epub')
          {
            toyResultFound = true;
          }
          resultInfo = results[1];
          debug_dump(resultInfo, 'resultInfo2');
          if (resultInfo.name ==='Toy.epub')
          {
            toyResultFound = true;
          }
          assert.equal(toyResultFound, true);
          assert.equal(err, null);
          done();
        });
    });
  });

  describe('Get results from a simple epub', function () {
    it('Get check results', function (done) {
      request(app)
        .get('/get_results')
        .set('publication', 'Toy.epub')
        .set('timestamp', timestamp)
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          var results = JSON.parse(res.text);
          assert.equal(results.items.length, 7);
          assert.equal(results.messages.length, 13);
          assert.equal(results.publication.title, 'Toy book');
          done();
        });
    });
  });

  describe('Get differences between two epubs', function () {
    it('get diff results', function (done) {
      request(app)
        .get('/get_comparison')
        .set('publicationA', "Toy.epub")
        .set('timestampA', timestamp)
        .set('publicationB', "Toy_modified.epub")
        .set('timestampB', timestamp)
        .expect(200)
        .end(function (err, res) {
          log_err(err, res);
          assert.equal(err, null);
          var results = JSON.parse(res.text);
          assert.equal(results.summary.itemChanges, 3);
          done();
        });
    });
  });
});


var debug_dump = function (o, name) {
  if (debug)
  {
    console.log(name + ' values:');
    for (var key in o)
    {
      console.log('\t' + key + ": " + o[key]);
    }
  }
};

var log_err = function(err, res) {
  if (err)
  {
    if (res)
    {
      console.log(res.text);
    }
    console.log(err);
    debug_dump(err, 'error');
  }
};