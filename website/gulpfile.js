const {parallel, watch} = require('gulp');

// Gulp Tasks
const sass = require('./build/sass.js');
const js = require('./build/js.js');
// const images = require('./build/images.js');

// Associate tasks with watched files
const watcher = () => {
  watch('./src/_style/**/*.scss', {ignoreInitial: true}, sass);
  // watch('./src/_scripts/**/*.js', {ignoreInitial: true}, js);
};

// By default, run tasks in parrallel
exports.default = parallel(sass);

// Also export the watching tasks
exports.watch = watcher;