Feature: URL fragment parser
  
  Tests the parser for URL fragments
    
  Scenario Outline: HTML ID-based fragment <fragment>
		* <fragment> is a valid HTML fragment
		And it indicates an element with ID <id>

    Scenarios:
      | fragment        | id   |
      | "id"            | "id" |
      | "%40%40"        | "@@" |
      | "id:~:text=a,b" | "id" |

    Scenarios: Text fragments (experimental)
      | fragment         |  id  |
      | "id:~:text=a,b"  | "id" |
      | ":~:text=a,b"    |  ""  |

    Scenarios: "invalid" non-ID-based fragments are processed as IDs
      | fragment   |  id         |
      | "foo=bar"  |  "foo=bar"  |
      | "epubcfi(" |  "epubcfi(" |


  Scenario Outline: HTML scheme-based fragment <fragment>
		* <fragment> is a valid HTML fragment
		And it has scheme <scheme>
		And it does not indicate an element

    Scenarios:
      | fragment            |   scheme   |
      | "xpointer(id(foo))" | "xpointer" |
      | "epubcfi(/6/4[chap01ref]!/4[body01])" |  "epubcfi"  |

  Scenario Outline: HTML media fragment <fragment>
		* <fragment> is a valid HTML fragment
		And it is a media fragment
		And it does not indicate an element

    Scenarios:
      | fragment       |
      | "xywh=1,1,1,1" |
      | "t=10"         |
      | "track=audio"  |
      | "id=foo"       |

  Scenario Outline: SVG shorthand fragment <fragment>
		* <fragment> is a <validity> SVG fragment
		And it indicates an element with ID <id>

    Scenarios: Shorthand fragments
      
      | fragment         | validity |  id   |
      | "id"             |  valid   | "id"  |
      | "id&t=10"        |  valid   | "id"  |
      | "id&t=10&t=5"    |  valid   | "id"  |
      | "id&foo=bar"     | invalid  | "id"  |
      | "id&t="          | invalid  | "id"  |
      | "id&"            | invalid  | "id"  |
      | "*id"            | invalid  | "*id" | (not an NCName)
      | "%40%40"         | invalid  | "@@"  | (not an NCName)
    
    
  Scenario Outline: SVG media fragment <fragment>
		* <fragment> is a <validity> SVG fragment
		
    Scenarios: Temporal media fragment
      | fragment             | validity |
      | "t=npt:10,20"        |  valid   |
      | "t=npt:,121.5"       |  valid   |
      | "t=0:02:00,121.5"    |  valid   |
      | "t=npt:120,0:02:01." |  valid   |
      | "t=60:00"            | invalid  |
      | "t=00:99"            | invalid  |
      | "t=123:00:00"        |  valid   |
      | "t=10&xywh=0,0,1,1"  |  valid   |
		
    Scenarios: Spatial media fragment
      | fragment                     | validity |
      | "xywh=160,120,320,240"       |  valid   |
      | "xywh=pixel:160,120,320,240" |  valid   |
      | "xywh=percent:25,25,50,50"   |  valid   |
      | "xywh=160,120,320"           | invalid  |
      | "xywh=px:160,120,320,240"    | invalid  |
		
    Scenarios: SVG view specification
      | fragment                                 | validity |
      | "svgView(viewBox(0,0,200,200))"          |  valid   |
      | "svgView(preserveAspectRatio(xMidYMid))" |  valid   |
      | "svgView(transform(scale(5))"            |  valid   |
      | "svgView()"                              | invalid  |
      | "svgView(viewBox(0,0,200,200"            | invalid  |
    
  Scenario Outline: SVG invalid fragments <fragment>
    Should not be parsed as legit IDs
		* <fragment> is a <validity> SVG fragment
		And it indicates an element with ID <id>

    Scenarios: Unknown or invalid media fragments
      | fragment  | validity | id  |
      | "foo=bar" | invalid  | ""  |
      | "foo="    | invalid  | ""  |
      | "=foo"    | invalid  | ""  |
    
