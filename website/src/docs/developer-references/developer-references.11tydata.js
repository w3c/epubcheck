module.exports = {
  // Skip subdirectory in output URL
  permalink: "{{ page | permalinkInParent }}",
  // Navigation (section + order)
  nav: {
    section: "developer",
    order: [
      "issue-tracker",
      "test-suite",
      "source-code",
      "releasing",
    ]
  }
}