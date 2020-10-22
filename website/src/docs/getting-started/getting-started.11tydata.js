module.exports = {
  // Skip subdirectory in output URL
  permalink: "{{ page | permalinkInParent }}",
  // Navigation (section + order)
  nav: {
    section: "getting-started",
    order: [
      "installation",
      "running",
      "apps-and-tools",
    ]
  }
}