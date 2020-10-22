module.exports = {
  // Skip subdirectory in output URL
  permalink: "{{ page | permalinkInParent }}",
  // Navigation (section + order)
  nav: {
    section: "contributing",
    order: [
      "creating-issues",
      "building",
      "writing-tests",
      "writing-code",
      "translating",
      "releasing",
    ]
  }
}