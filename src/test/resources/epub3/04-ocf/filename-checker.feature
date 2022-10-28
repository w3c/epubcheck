Feature: File name checker
  
  Tests the checker for disallowed characters in file names.

  Note that these test cases evaluate raw Unicode file name strings.
  Additional functional tests (based on full publications or package documents)
  are also available in the feature file for container-relates tests.
  
  Scenario Outline: valid file name '<filename>' is accepted
		When checking file name '<filename>'
    Then no error or warning is reported
    
    Scenarios:
      | filename                  |
      | only-ascii                |
      | not-ascii-é               |
      | ideograph-䀀              |
      | emoji-😊                  |
      | emoji-tag-sequence-🏴󠁧󠁢󠁥󠁮󠁧󠁿     |
      
  Scenario Outline: space (<codepoint>) is reported as warning
		When checking file name containing code point <codepoint>
    Then warning PKG-010 is reported
    
    Scenarios:
      | codepoint | name       |
      | U+0009    | TAB        |
      | U+000A    | LF         |
      | U+000C    | FF         |
      | U+000D    | CR         |
      | U+0020    | SPACE      |
      | U+2009    | THIN SPACE |
      
  Scenario: disallow FULL STOP as the last character
  	When checking file name 'aname.'
    Then error PKG-011 is reported
      
  Scenario: disallowed characters are reported
  	When checking file name 'a*name'
    Then error PKG-009 is reported
  	Then the message contains 'U+002A (*)'

  Scenario: disallowed characters are all reported
  	When checking file name 'a*na"me'
    Then error PKG-009 is reported
  	Then the message contains 'U+002A (*), U+0022 (")'

  Scenario: disallowed characters are reported only once
  	When checking file name 'a*na*me'
    Then error PKG-009 is reported
  	Then the message contains 'U+002A (*).'
    
  Scenario Outline: disallowed character <codepoint> is reported
		When checking file name containing code point <codepoint>
    Then error PKG-009 is reported
    And the message contains '<codepoint> <tostring>'
    
    Scenarios:
      | codepoint | tostring |
      | U+0022    | (") |
      | U+002A    | (*)   |
      | U+003A    | (:) |
      | U+003C    | (<) |
      | U+003E    | (>) |
      | U+003F    | (?) |
      | U+005C    | (\) |
      | U+007C    | (\|) |
      | U+007F    | (CONTROL)   |
      | U+0000    | (CONTROL)   |
      | U+0080    | (CONTROL)   |
      | U+E000    | (PRIVATE USE)   |
      | U+FDD0    | (NON CHARACTER) |
      | U+FFFD    | REPLACEMENT CHARACTER (SPECIALS) |
      | U+FFFE    | (NON CHARACTER)   |
      | U+9FFFE   | (NON CHARACTER)   |
      | U+E0001   | LANGUAGE TAG (DEPRECATED)  |
      #| U+E007F   | CANCEL TAG  | # Re-instated for ETS use 
      | U+F0001   | (PRIVATE USE) |
      | U+100000  | (PRIVATE USE) |

  Scenario: VERTICAL LINE (|) is not disallowed in EPUB 2.0.1
    Given EPUBCheck configured to check EPUB 2 rules
  	When checking file name 'a|name'
    Then no error or warning is reported
