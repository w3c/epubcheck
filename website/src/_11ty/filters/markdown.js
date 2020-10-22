/**
 * Eleventy Filters
 */
const md = require('markdown-it')

module.exports = string => {
  return md({
    html: true
  }).render(string);
}