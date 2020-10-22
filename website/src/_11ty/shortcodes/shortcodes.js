/**
 * Eleventy Shortcodes
 */
const fs = require('fs')
const path = require('path')
const memoize = require('micro-memoize')
const { deepEqual } = require('fast-equals')


module.exports = {
  icon: name => 
    `<svg aria-hidden="true" focusable="false" width="1em" height="1em">
        <use xlink:href="#icon-${name}"></use>
     </svg>`,
  inline: memoize(file => {
      return fs.readFileSync(path.join('_site',file.toString()),'utf8')
    },
    {
      maxSize: 10,
      isEqual: deepEqual,
    })
}