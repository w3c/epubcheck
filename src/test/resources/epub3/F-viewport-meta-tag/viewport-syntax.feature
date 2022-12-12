Feature: Viewport meta tag syntax
  
  Tests the parser for the viewport meta tag syntax as defined
  in EPUB 3.3:
    https://www.w3.org/TR/epub-33/#app-viewport-meta-syntax

  Scenario Outline: parsing valid viewport values
		When parsing viewport <viewport>
    Then no error is returned
    And the parsed viewport equals <result>
    
    Scenarios:
      | viewport                 | result                  |
      | "width=1200, height=600" | "width=1200;height=600" |
      # single values
      | "p1=v1"                  | "p1=v1"                 |
      | "  p1  =  v1 "           | "p1=v1"                 |
      | "	p1	=	v1	"            | "p1=v1"                 |
      | "p1  =  v1"              | "p1=v1"                 |
      # separators
      | "p1=v1,p2=v2"            | "p1=v1;p2=v2"           |
      | "p1=v1;p2=v2"            | "p1=v1;p2=v2"           |
      | "p1=v1,,,p2=v2"          | "p1=v1;p2=v2"           |
      | "p1=v1;;;p2=v2"          | "p1=v1;p2=v2"           |
      | "p1=v1   p2=v2"          | "p1=v1;p2=v2"           |
      | "p1=v1, ;p2=v2"          | "p1=v1;p2=v2"           |
      | "p1=v1,p2=v2,p3=v3"      | "p1=v1;p2=v2;p3=v3"     |
      # value-less names
      | "p1"                     | "p1="                   |
      | "p1 "                    | "p1="                   |
      | "p1,p2=v2"               | "p1=;p2=v2"             |
      | "p1 ,p2=v2"              | "p1=;p2=v2"             |
      | "p1=v1,p2"               | "p1=v1;p2="             |
      | "p1=v1 p2"               | "p1=v1;p2="             |
      # multiple values
      | "p1=v1a,p1=v1b"          | "p1=v1a,v1b"            |
      | "p1=v1a,p2=v2,p1=v1b"    | "p1=v1a,v1b;p2=v2"      |
    
  Scenario Outline: parsing invalid viewport values
		When parsing viewport <viewport>
    Then error <error> is returned
    
    Scenarios:
      | viewport    | error              |
      | ""          | NULL_OR_EMPTY      |
      | "p1==v1"    | ASSIGN_UNEXPECTED  |
      | "p1=v1=v"   | ASSIGN_UNEXPECTED  |
      | "p1 v1"     | VALUE_EMPTY        |
      | "p1="       | VALUE_EMPTY        |
      | "p1= "      | VALUE_EMPTY        |
      | "p1=v1,p2=" | VALUE_EMPTY        |
      | "=value"    | NAME_EMPTY         |
      | "p1=v1,=v"  | NAME_EMPTY         |
      | "p1=v1, =v" | NAME_EMPTY         |
      | ",p1=v1"    | LEADING_SEPARATOR  |
      | ";p1=v1"    | LEADING_SEPARATOR  |
      | "p1=v1,"    | TRAILING_SEPARATOR |
      | "p1=v1;"    | TRAILING_SEPARATOR |
