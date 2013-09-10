var epubcheck = epubcheck || {};
epubcheck.test = epubcheck.test || {};
epubcheck.test.Sample = {};
epubcheck.test.Unused = {};

epubcheck.test.Sample = function (name) {
    this.name = name;
};

epubcheck.test.Sample.prototype.sayHello = function () {
    window.alert("hello" + this.name);
};

epubcheck.test.Unused = function (name) {
    this.name = name;
};

epubcheck.test.Unused.prototype.sayHello = function () {
    window.alert("Hello " + this.name);
};