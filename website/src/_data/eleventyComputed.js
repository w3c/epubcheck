module.exports = {
  // Defines a global hierarchical navigation
  eleventyNavigation: {
    // Compute the navigation key
    // - use the slugified file name by default
    // - or the directory’s name for 'index.md' templates
    key: data => {
      if (data.page.fileSlug === "") {
        return "/"
      }
      return data.page.fileSlug
    },
    // Compute the navigation title
    // - use the explicit 'data.nav.title' if defined
    // - defaults to the page’s title
    title: data => {
      if (data.nav && data.nav.title)
        return data.nav.title
      return data.title
    },
    // Compute the navigation parent
    // - use the explicit 'data.nav.parent' if defined
    // - otherwise use the name of the parent folder
    // - if the file is declared as root, parent is undefined
    parent: data => {
      var parent;
      if (data.nav && data.nav.section && !data.nav.isRoot) {
        parent = data.nav.section
      } else if (!data.nav) {
        const segments = data.page.filePathStem.split('/');
        const index = segments.lastIndexOf(data.page.fileSlug)-1;
        switch (index) {
          case -1:
            break
          case 0:
            parent = "/"
          default:
            parent = segments[index]
        }
      }
      return parent
    },
    // The navigation children order is defined in the `nav.order` array
    order: data => {
      if (data.nav && Array.isArray(data.nav.order)) {
        var index = data.nav.order.indexOf(data.page.fileSlug);
        if (index>=0) {
          return index+1
        }
      }
      return 1000
    },
    tocTitle: data => {
      if (data.nav) return data.nav.tocTitle
    }
  }
}