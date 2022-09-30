Feature: Viewport meta tag syntax
  
  Tests the parser for the viewport meta tag syntax as defined
  in EPUB 3.3:
    https://www.w3.org/TR/epub-33/#app-viewport-meta-syntax
    
  Scenario Outline: parsing valid viewport values
		When parsing viewport <viewport>
    Then no error is returned
    
    Scenarios:
      | viewport                     |
      | "width=1200, height=600"     |
      | "p1=value"                   |
      | "  p1  =  value "            |
      | "	p1	=	value	"              |
      | "p1  =  value"               |
      | "p1=value,p2=value"          |
      | "p1=value;p2=value"          |
      | "p1=value,,,p2=value"        |
      | "p1=value;;;p2=value"        |
      | "p1=value   p2=value"        |
      | "p1=value, ;p2=value"        |
      | "p1=value,p2=value,p3=value" |
    
  Scenario Outline: parsing invalid viewport values
		When parsing viewport <viewport>
    Then error <error> is returned
    
    Scenarios:
      | viewport           | error              |
      | ""                 | NULL_OR_EMPTY      |
      | "p1==value"        | ASSIGN_UNEXPECTED  |
      | "p1=value=value"   | ASSIGN_UNEXPECTED  |
      | "p1"               | VALUE_EMPTY        |
      | "p1 value"         | VALUE_EMPTY        |
      | "p1=value value"   | VALUE_EMPTY        |
      | "p1,p2=value"      | VALUE_EMPTY        |
      | "p1=value,p2"      | VALUE_EMPTY        |
      | "p1="              | VALUE_EMPTY        |
      | "p1= "             | VALUE_EMPTY        |
      | "p1=value,p2="     | VALUE_EMPTY        |
      | "=value"           | NAME_EMPTY         |
      | "p1=value,=value"  | NAME_EMPTY         |
      | "p1=value, =value" | NAME_EMPTY         |
      | ",p1=value"        | LEADING_SEPARATOR  |
      | ";p1=value"        | LEADING_SEPARATOR  |
      | "p1=value,"        | TRAILING_SEPARATOR |
      | "p1=value;"        | TRAILING_SEPARATOR |
