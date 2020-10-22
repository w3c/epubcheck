/**
 * Shortcode to generate icon sprites
 * - arguments is a list of icon names
 * - sprite is memoized (compiled only once for a given set of icons)
 * - icons from the Feather icon set (MIT License)
 *     https://feathericons.com
 * - adapted from the sprite generation in 'eleventastic' by Max Böck (MIT License):
 *     https://github.com/maxboeck/eleventastic
 * - also inspired from this article by Nicolas Hoizey:
 *     https://nicolas-hoizey.com/articles/2020/10/15/how-i-build-my-svg-sprites/
 */
const fs = require('fs')
const path = require('path')
const File = require('vinyl')
const SVGSpriter = require('svg-sprite')
const memoize = require('micro-memoize')
const { deepEqual } = require('fast-equals')

const iconsDir = path.resolve(require.resolve("feather-icons"),"../icons");
const spriteConfig = {
  mode: {
      inline: true, // we want inline SVG content
      symbol: true, // we want sprites in <symbol> elements
  },
  shape: {
      transform: [{
        svgo: { // SVG optimization
          plugins: [
            {removeXMLNS: true} // individual <symbol> do not need namespaces
          ]
        }
      }],
      id: {
          generator: 'icon-%s' // the ID of the SVG <symbol> elements
      }
  },
  svg: {
      xmlDeclaration: false, // no XML decl for inline SVG
      doctypeDeclaration: false, // no doctype for inline SVG
      namespaceClassnames: false, // not needed, as Feather classes do not conflict by default
      rootAttributes: { style: 'display:none'}, // top-level sprite SVG must not be displayed
      transform: [
        svg => { return svg.replace(/feather/g,'icon') } // de-Featherize class names
      ]
  }
}

const spritify = async (icons) => {
  // Make a new SVGSpriter instance w/ configuration
  const spriter = new SVGSpriter(spriteConfig);
  // Promisified spriter compile function
  const compileSprite = async (args) => {
      return new Promise((resolve, reject) => {
          spriter.compile(args, (error, result) => {
              if (error) {
                  return reject(error)
              }
              resolve(result.symbol.sprite)
          })
      })
  }
  // Add all the given icons to the spriter
  icons.forEach(icon => {
    const iconPath = path.join(iconsDir, `${icon}.svg`);
    if (fs.existsSync(iconPath)) {
      spriter.add(new File({
        path: iconPath,
        base: iconsDir,
        contents: fs.readFileSync(iconPath)
      }))
    } else {
      console.log(`WARNING: icon '${icon}' could not be found`);
    }
  })
  // Compile the sprite file and return it as a string (+ add to cache)
  const sprite = await compileSprite(spriteConfig.mode)
  return `${sprite.contents.toString('utf8')}`
}

module.exports = memoize(spritify, {
  isPromise: true,
  isEqual: deepEqual,
  maxSize: 10,
});
