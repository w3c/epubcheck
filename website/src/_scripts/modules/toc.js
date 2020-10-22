  // Get all the toc’s headings
  // const tocHeadings = document.querySelectorAll('.toc h2')
  
  // Array.prototype.forEach.call(tocHeadings, heading => {
  //   // Give each <h2> a toggle button child
  //   // with the SVG plus/minus icon
  //   heading.innerHTML = `
  //     <button aria-expanded="false">
  //       ${heading.textContent}
  //       {% icon 'chevron-down' %}
  //     </button>
  //   `
  //   let button = heading.querySelector('button')
  //   let icon = heading.querySelector('use')
  //   let content = heading.nextElementSibling
    
  //   content.hidden = true
    
  //   button.onclick = () => {
  //     // Cast the state as a boolean
  //     let expanded = button.getAttribute('aria-expanded') === 'true' || false
  //     // Switch the state
  //     button.setAttribute('aria-expanded', !expanded)
  //     // Switch the content's visibility
  //     wrapper.hidden = expanded
  //     // Switch the icon // FIXME with CSS?
  //     icon.setAttribute("xlink:ref","icon-chevron-up")
  //   }
  // })