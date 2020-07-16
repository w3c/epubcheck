Feature: EPUBCheck ▸ Localization

  Checks the localization configuration of EPUBCheck

  Background: 
    Given EPUB test files located at '/localization/files/'
    And EPUBCheck with default settings

  ## Test locale configuration

  Scenario: The default testing locale is English
    When checking EPUB 'schema-error'
    Then the following errors are reported
      | RSC-005 | Error |

  Scenario: The reporting locale can be configured
    Given the reporting locale is set to 'fr-FR'
    When checking EPUB 'schema-error'
    Then the following errors are reported
      | RSC-005 | Erreur |

  Scenario: The reporting locale overrides the default locale
    Given the default locale is set to 'fr-FR'
    Given the reporting locale is set to 'en'
    When checking EPUB 'schema-error'
    Then the following errors are reported
      | RSC-005 | Error |

  ## Test localization of various types of messages

  Scenario: CSS messages are localized
    Given the reporting locale is set to 'fr-FR'
    When checking EPUB 'css-error'
    Then the following errors are reported
      | CSS-008 | erreur |

  Scenario: Schema messages (Jing library) are localized
    Given the reporting locale is set to 'fr-FR'
    When checking EPUB 'schema-error'
    Then the following errors are reported
      | RSC-005 | balise |

  Scenario: Jing locale is properly reset (after the previous scenario)
    When checking EPUB 'schema-error'
    Then the following errors are reported
      | RSC-005 | tag |

  ## Test locale-specific issues

  Scenario: Case-conversion is locale-dependent
    See issue #711 for how 'tr-TR' affected upper-case conversion
    Given the default locale is set to 'tr-TR'
    When checking EPUB 'nav-page-list-valid'
    Then no errors or warnings are reported
