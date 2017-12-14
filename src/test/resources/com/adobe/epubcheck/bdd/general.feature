Feature: General
  As a user
  I want the application to display information clearly

  Scenario: [MUST] Visible version number
    When I run the application with "--help"
    Then The version number should be clearly visible on the first line
