module.exports = {
  // Skip subdirectory in output URL
  permalink: "{{ page | permalinkInParent }}",
  // Navigation (section + order)
  nav: {
    section: "user",
    order: [
      "cli",
      "messages",
      "report",
      "java-library",
    ]
  }
}