/**
 *  A plugin for [markdown-it](https://github.com/markdown-it/markdown-it)
 *  that generates a table of contents as a JSON object, and adds it to
 *  the `toc` field of the runtime environment.
 * 
 *  Adapted from the AST generation in mardown-it-toc-done-right
 *  see https://github.com/nagaozen/markdown-it-toc-done-right
 * 
 *  MIT License.
 */
function slugify (x) {
  return encodeURIComponent(String(x).trim().toLowerCase().replace(/\s+/g, '-'))
}

function plugin(md, options) {
  options = Object.assign({}, {
    slugify: slugify,
    uniqueSlugStartIndex: 1,
    callback: undefined/* function(state, toc) {} */
  }, options)

  function headings2toc (tokens) {    
    
    const uniques = {}
    function unique (s) {
      let u = s
      let i = 1;
      while (Object.prototype.hasOwnProperty.call(uniques, u)) u = `${s}-${i++}`
      uniques[u] = true
      return u
    }

    const toc = { level: 0, title: '', url: '', children: [] }
    const stack = [toc]

    let isAside = false
    for (let i = 0, iK = tokens.length; i < iK; i++) {
      const token = tokens[i]
      if (token.type.startsWith('container_')) {
        isAside = token.type.endsWith('_open')
      }
      if (!isAside && token.type === 'heading_open') {
        const key = (
          tokens[i + 1]
            .children
            .filter(function (token) { return token.type === 'text' || token.type === 'code_inline' })
            .reduce(function (s, t) { return s + t.content }, '')
        )
  
        const node = {
          level: parseInt(token.tag.substr(1), 10),
          title: key,
          url: `#${unique(options.slugify(key))}`,
          children: []
        }
  
        if (node.level > stack[0].level) {
          stack[0].children.push(node)
          stack.unshift(node)
        } else if (node.level === stack[0].level) {
          stack[1].children.push(node)
          stack[0] = node
        } else {
          while (node.level <= stack[0].level) stack.shift()
          stack[0].children.push(node)
          stack.unshift(node)
        }
      }
    }

    return toc
  }

  md.core.ruler.push('addTocToEnv', function (state) {
    const tokens = state.tokens
    toc = headings2toc(tokens)
    if (state.env && !state.env.toc) {
      state.env.pageToc = toc;
    }
    if (typeof options.callback === 'function') {
      options.callback(state,toc)
    }
  })
}

module.exports = plugin;