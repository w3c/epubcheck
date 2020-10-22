const {dest, src} = require('gulp');
const cleanCSS = require('gulp-clean-css');
const sassProcessor = require('gulp-sass')(require('sass'));

// Get the production build flag
const isProduction = process.env.NODE_ENV === 'production';

// Sass task:
// - grab all .scss files
// - process them through Sass
// - minify CSS if production mode
const sass = () => {
  return src('./src/_style/*.scss')
    .pipe(sassProcessor().on('error', sassProcessor.logError))
    .pipe(cleanCSS(isProduction ? { level: 2 } : {}))
    .pipe(dest('./_site/css', {sourceMaps: !isProduction}));
};

module.exports = sass;