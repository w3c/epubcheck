package org.idpf.epubcheck.util.css;


import com.adobe.epubcheck.util.outWriter;
import com.google.common.collect.Lists;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssToken.CssTokenConsumer;
import org.idpf.epubcheck.util.css.CssToken.Type;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class CssScannerTest {
	
	@Test
	public void testLexerNull() throws Exception {
		String s = "";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(0, tokens.size());
		assertEquals(0, exceptions.size());			
	}
	
	@Test
	public void testLexerSpace_10() throws Exception {
		String s = "  ";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.S, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerSpace_20() throws Exception {
		String s = " \n \t \f ";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.S, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerCDOCDC_10() throws Exception {
		String s = "<!--";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.CDO, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerCDOCDC_20() throws Exception {
		String s = "-->";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.CDC, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerCDOCDC_30() throws Exception {
		String s = "<!--/*c*/-->";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.CDO, tokens.get(0).getType());
		assertEquals(CssToken.Type.COMMENT, tokens.get(1).getType());
		assertEquals(1, tokens.get(1).getChars().length());
		assertEquals(CssToken.Type.CDC, tokens.get(2).getType());
	}
	
	@Test
	public void testLexerCDOCDC_40() throws Exception {		
		/* 
		 * http://www.w3.org/TR/CSS2/syndata.html#tokenization:
		 * 
		 * For example, the rule of the longest match means that 
		 * "red-->" is tokenized as the IDENT "red--" followed by 
		 * the DELIM ">", rather than as an IDENT followed by a CDC.
		 * 
		 */
		String s = "red-->";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(2, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertTrue(tokens.get(0).getChars().equals("red--"));
		assertEquals(CssToken.Type.CHAR, tokens.get(1).getType());
		assertTrue(tokens.get(1).getChars().equals(">"));
	}
		
	@Test
	public void testLexerString_10() throws Exception {
		String s = "'string'";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.STRING, tokens.get(0).getType());
		assertEquals(6, tokens.get(0).getChars().length());
	}
	
	@Test
	public void testLexerString_20() throws Exception {
		String s = "\"string\"";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.STRING, tokens.get(0).getType());
		assertEquals(6, tokens.get(0).getChars().length());
	}
	
	@Test
	public void testLexerString_30() throws Exception {
		String s = "''";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.STRING, tokens.get(0).getType());
		assertEquals(0, tokens.get(0).getChars().length());
	}
	
	@Test
	public void testLexerString_40() throws Exception {
		String s = "\"string string\"";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.STRING, tokens.get(0).getType());
		assertEquals(13, tokens.get(0).getChars().length());
	}
	
	@Test
	public void testLexerString_50() throws Exception {
		// literal nl not allowed
		String s = "\"string\nstring\"";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(1, exceptions.size());				
	}
	
	/*
	User agents must close strings upon reaching the end of a line 
	(i.e., before an unescaped line feed, carriage return or form feed character), 
	but then drop the construct (declaration or rule) in which the string was found. 
	For example:
	      p {
	        color: green;
	        font-family: 'Courier New Times
	        color: red;
	        color: green;
	      }
	...would be treated the same as:
	      p { color: green; color: green; }
	...because the second declaration (from 'font-family' to the 
	semicolon after 'color: red') is invalid and is dropped.
	*/
	
	@Test
	public void testLexerString_51() throws Exception {
		// literal nl not allowed; check forwarding as per 2.1
		// note that the lexer keeps the erronuous property, 
		// but it has an error flag
		String s = "p { color: green; font-family: 'Courier New Times\n color: red; color: green; }";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(15, stripTokens(Type.S, tokens).size());
		assertEquals(1, exceptions.size());				
	}
	
	@Test
	public void testLexerComment_10() throws Exception {
		String s = "/* comment */";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.COMMENT, tokens.get(0).getType());
		assertEquals(9, tokens.get(0).getChars().length());
	}
	
	@Test
	public void testLexerComment_15() throws Exception {
		String s = "/**/";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.COMMENT, tokens.get(0).getType());
		assertEquals(0, tokens.get(0).getChars().length());
	}
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-002.xht	
//    <meta name="assert" content="Comments cannot be nested." />
//    <style type="text/css">
//        div
//        {
//            /*/**/color:green;*/
//        }
//    </style>
//</head>
//<body>
//    <p>Test passes if the "Filler Text" below is green.</p>
//    <div>Filler Text</div>
//</body>
	
	@Test
	public void testLexerComment_16() throws Exception {
		String s = "div { /*/**/color:green;*/}";		
		List<CssToken> tokens = execScan(s);
		//the scanner just closes at the first occurence
		//of comment close; error will not be detected
		//until grammar level		
		assertEquals(1, getTokenTypeCount(Type.COMMENT, tokens));
		assertTrue(hasIdent(tokens, "color"));
		assertTrue(hasIdent(tokens, "green"));
		assertEquals(Type.CHAR, tokens.get(tokens.size()-2).getType());
		assertEquals(Type.CHAR, tokens.get(tokens.size()-3).getType());
	}
	
	
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-003.xht
//    <meta name="flags" content="invalid" />
//    <meta name="assert" content="SGML comment delimiters do not delimit CSS comments." />
//    <style type="text/css">
//        <![CDATA[
//        div
//        {
//          color: green;
//        }
//        <!--
//        /*
//        #div1
//        {
//            color: red;
//        }
//        -->
//        #div1
//        {
//            color: red;
//        }
//        ]]>
//    </style>
//</head>
//<body>
//    <p>Test passes if the "Filler Text" below is green.</p>
//    <div id="div1">Filler Text</div>
//</body>
		
	@Test
	public void testLexerComment_17() throws Exception {
		String s = "div {color: green;}<!--/*#div1{color: red;}-->#div1{color: red;}";		
		List<CssToken> tokens = execScan(s);
		assertFalse(hasIdent(tokens, "red"));
	}
	
	
	
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-004.xht
//    <meta name="flags" content="invalid" />
//    <meta name="assert" content="Comments that are invalid are ignored." />
//    <style type="text/css">
//        div
//        {
//            /*/*/color: green;
//        }
//    </style>
//</head>
//<body>
//    <p>Test passes if the "Filler Text" below is green.</p>
//    <div>Filler Text</div>
//</body>

	//TODO ignored??
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-005.xht
//    <meta name="flags" content="invalid" />
//    <meta name="assert" content="A slash '/' escaped with a backslash will remove its special meaning." />
//    <style type="text/css">
//        div
//        {
//            /* *\/*/color: green;
//        }
//    </style>
//</head>
//<body>
//    <p>Test passes if the "Filler Text" below is green.</p>
//    <div>Filler Text</div>
//</body>
		
	@Test
	public void testLexerComment_18() throws Exception {
		//TODO this is a grammar-level test
		String s = "div{/* *\\/*/color: green;}";		
		List<CssToken> tokens = execScan(s);
		assertEquals(0, exceptions.size());	
		assertEquals(1, getTokenTypeCount(Type.COMMENT, tokens));
		assertEquals(" *\\/", getFirstTokenOfType(Type.COMMENT, tokens).getChars());
		assertTrue(hasIdent(tokens, "color"));
		assertTrue(hasIdent(tokens, "green"));
	}
	
	
	
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-006.xht
//	<meta name="assert" content="A star '*' escaped with a backslash will not remove its special meaning." />
//    <style type="text/css">
//        *
//        {
//            color: green;
//        }
//        p
//        {
//            color: black;
//        }
//        div
//        {
//            /*\*/*/color: red;
//        }
//    </style>
//</head>
//<body>
//    <p>Test passes if the "Filler Text" below is green.</p>
//    <div>Filler Text</div>
//</body>
//
	
	//TODO this is a grammar-level recovery issue
	
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-008.xht
//    <meta name="flags" content="invalid" />
//    <meta name="assert" content="Escaping a slash '/' for an opening comment makes the comment invalid." />
//    <style type="text/css">
//        div
//        {
//            \/*;color: green;*/
//        }
//    </style>
//</head>
//<body>
//    <p>Test passes if the "Filler Text" below is green.</p>
//    <div>Filler Text</div>
//</body>
	
	@Test
	public void testLexerComment_20() throws Exception {
		String s = "div{\\/*;color: green;*/}";		
		List<CssToken> tokens = execScan(s);		
		assertTrue(hasIdent(tokens, "color"));
		assertTrue(hasIdent(tokens, "green"));
	}
	
//http://test.csswg.org/suites/css2.1/20110323/xhtml1/comments-009.xht
//    <meta name="flags" content="invalid" />
//    <meta name="assert" content="Escaping a star '*' for an opening comment makes the comment invalid." />
//    <style type="text/css">
//        div
//        {
//            /\*;color: green;*/
//        }
//    </style>
	
		
	@Test
	public void testLexerComment_21() throws Exception {
		String s = "div{/\\*;color: green;*/}";		
		List<CssToken> tokens = execScan(s);		
		assertTrue(hasIdent(tokens, "color"));
		assertTrue(hasIdent(tokens, "green"));
	}
	
	@Test
	public void testLexerSpaceAndComment_10() throws Exception {
		String s = "  /* c1 */  /* c2 */\t";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(5, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.S, tokens.get(0).getType());
		assertEquals(CssToken.Type.COMMENT, tokens.get(1).getType());
		assertEquals(CssToken.Type.S, tokens.get(2).getType());
		assertEquals(CssToken.Type.COMMENT, tokens.get(3).getType());
		assertEquals(CssToken.Type.S, tokens.get(4).getType());
	}
	
	@Test
	public void testLexerIdent_10() throws Exception {
		String s = "ident";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());	
		assertEquals("ident", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerIdent_20() throws Exception {
		String s = "-ident";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals("-ident", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerIdent_21() throws Exception {
		String s = "-ident-ident";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals("-ident-ident", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerIdent_22() throws Exception {
		String s = "a";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals("a", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerIdent_23() throws Exception {
		String s = "-a";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals("-a", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerAtKeyword_10() throws Exception {
		String s = "@ident";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.ATKEYWORD, tokens.get(0).getType());	
		assertEquals("@ident", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerAtKeyword_20() throws Exception {
		String s = "@-ident";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.ATKEYWORD, tokens.get(0).getType());	
		assertEquals("@-ident", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerAtKeyword_30() throws Exception {
		String s = "@z";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.ATKEYWORD, tokens.get(0).getType());	
		assertEquals("@z", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerAtKeyword_40() throws Exception {
		String s = "@- ";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());		
		assertTrue(CssToken.Type.CHAR == tokens.get(0).getType());
		assertTrue(CssToken.Type.CHAR == tokens.get(1).getType());
		assertTrue(CssToken.Type.S == tokens.get(2).getType());
	}
	
	@Test
	public void testLexerAtKeyword_41() throws Exception {
		String s = "@-";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(2, tokens.size());		
		assertTrue(CssToken.Type.CHAR == tokens.get(0).getType());
		assertTrue(CssToken.Type.CHAR == tokens.get(1).getType());			
	}
	
	@Test
	public void testLexerAtKeyword_42() throws Exception {
		String s = "@\\zoo";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.ATKEYWORD, tokens.get(0).getType());	
		assertEquals("@zoo", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerAtKeyword_43() throws Exception {
		String s = "@zo\\o";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.ATKEYWORD, tokens.get(0).getType());	
		assertEquals("@zoo", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerUri_10() throws Exception {
		String s = "url(\"foo\")";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url('foo')", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerUri_11() throws Exception {
		String s = "url(  \"foo\"  )";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url('foo')", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerUri_20() throws Exception {
		String s = "url('foo')";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url('foo')", tokens.get(0).getChars());
	}
	
	
	@Test
	public void testLexerUri_30() throws Exception {
		String s = "url(foo)";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url(foo)", tokens.get(0).getChars());
	}
	
	
	@Test
	public void testLexerUri_31() throws Exception {
		String s = "url(  foo  )";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url(foo)", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerUri_40() throws Exception {
		String s = "UrL(fOo)";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url(fOo)", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerUri_50() throws Exception {		
//		Note that COMMENT tokens cannot occur within other tokens: thus, 
//      'url(/*x*/pic.png)' denotes the URI '/*x*/pic.png', not 'pic.png'. 
		
		String s = "url(/*x*/pic.png)";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url(/*x*/pic.png)", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerUri_51() throws Exception {		
//		Note that COMMENT tokens cannot occur within other tokens: thus, 
//      'url(/*x*/pic.png)' denotes the URI '/*x*/pic.png', not 'pic.png'. 
		
		String s = "url(pic/*x*/.png)";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.URI, tokens.get(0).getType());	
		assertEquals("url(pic/*x*/.png)", tokens.get(0).getChars());
	}

//	"url("{w}{string}{w}")" {return URI;}
//	"url("{w}{url}{w}")"    {return URI;}
//	baduri1         url\({w}([!#$%&*-\[\]-~]|{nonascii}|{escape})*{w}
//	baduri2         url\({w}{string}{w}
//	baduri3         url\({w}{badstring}
//	badstring       {badstring1}|{badstring2}
//	badstring1      \"([^\n\r\f\\"]|\\{nl}|{escape})*\\?
//	badstring2      \'([^\n\r\f\\']|\\{nl}|{escape})*\\?	
	
	@Test
	public void testLexerUriBad_10() throws Exception {
		String s = "url(pic.png";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(1, exceptions.size());		
	}
	
	@Test
	public void testLexerUriBad_11() throws Exception {
		String s = "url('pic.png";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(1, exceptions.size());		
	}
	
	@Test
	public void testLexerUriBad_12() throws Exception {
		// ')' is lexically ok in the string type
		// therefore we get premature eof
		String s = "url('pic.png) foo";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(1, exceptions.size());		
	}
	
	@Test
	public void testLexerUriBad_13() throws Exception {		
		String s = "url('pic.png' foo";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(2, tokens.size());
		assertEquals(1, exceptions.size());		
	}
	
	@Test
	public void testLexerFunction_10() throws Exception {
		String s = "ident(";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.FUNCTION, tokens.get(0).getType());	
		assertEquals("ident(", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerNum_10() throws Exception {
		String s = "1";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.INTEGER, tokens.get(0).getType());	
		assertEquals("1", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerNum_11() throws Exception {
		String s = "1.1";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.NUMBER, tokens.get(0).getType());	
		assertEquals("1.1", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerNum_12() throws Exception {
		String s = "1.11";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.NUMBER, tokens.get(0).getType());	
		assertEquals("1.11", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerNum_13() throws Exception {
		String s = ".1";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.NUMBER, tokens.get(0).getType());	
		assertEquals(".1", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerNum_14() throws Exception {
		String s = ".11";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.NUMBER, tokens.get(0).getType());	
		assertEquals(".11", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerNum_15() throws Exception {
		String s = "1.";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(2, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.INTEGER, tokens.get(0).getType());
		assertEquals(CssToken.Type.CHAR, tokens.get(1).getType());
		
	}
	
	@Test
	public void testLexerNum_16() throws Exception {
		String s = ".";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.CHAR, tokens.get(0).getType());		
	}

  @Test
  public void testLexerQnty_10() throws Exception {
    String s = "10em";

    List<CssToken> tokens = execScan(s);
    assertEquals(1, tokens.size());
    assertEquals(0, exceptions.size());
    assertEquals(CssToken.Type.QNTY_EMS, tokens.get(0).getType());
    assertEquals("10em", tokens.get(0).getChars());
  }

  @Test
  public void testLexerQnty_10REM() throws Exception {
    String s = "10rem";

    List<CssToken> tokens = execScan(s);
    assertEquals(1, tokens.size());
    assertEquals(0, exceptions.size());
    assertEquals(CssToken.Type.QNTY_REMS, tokens.get(0).getType());
    assertEquals("10rem", tokens.get(0).getChars());
  }

  @Test
	public void testLexerQnty_11() throws Exception {
		String s = "10ex";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.QNTY_EXS, tokens.get(0).getType());
		assertEquals("10ex", tokens.get(0).getChars());				
	}
	
	@Test
	public void testLexerQnty_12() throws Exception {
		String s = "10%";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());		
		assertEquals(CssToken.Type.QNTY_PERCENTAGE, tokens.get(0).getType());
		assertEquals("10%", tokens.get(0).getChars());				
	}
	
	@Test
	public void testLexerQnty_13() throws Exception {
		// QNTY_LENGTH, {num}cm, {num}px, {num}mm, {num}in, {num}pt, {num}pc
		String s = "10cm 10px 10mm 10in 10pt 10pc";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(11, tokens.size());
		assertEquals(0, exceptions.size());
		int c = 0;
		for(CssToken tk : tokens) {
			if(tk.getType() == CssToken.Type.QNTY_LENGTH) {
				c++;
				assertEquals(4, tk.getChars().length());
			}
		}
		assertEquals(6, c);						
	}
	
	@Test
	public void testLexerQnty_14() throws Exception {
		// QNTY_ANGLE, {num}deg, {num}rad, {num}grad
		String s = "10deg 10rad 10grad";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(5, tokens.size());
		assertEquals(0, exceptions.size());
		int c = 0;
		for(CssToken tk : tokens) {
			if(tk.getType() == CssToken.Type.QNTY_ANGLE) {
				c++;
			}
		}
		assertEquals(3, c);						
	}
	
	@Test
	public void testLexerQnty_15() throws Exception {
		// QNTY_TIME {num}ms, {num}s
		String s = "10ms 10s";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());
		assertEquals(0, exceptions.size());
		int c = 0;
		for(CssToken tk : tokens) {
			if(tk.getType() == CssToken.Type.QNTY_TIME) {
				c++;
			}
		}
		assertEquals(2, c);						
	}
	
	@Test
	public void testLexerQnty_16() throws Exception {
		// FREQ {num}Hz, {num}kHz
		String s = "10hz 10khz";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());
		assertEquals(0, exceptions.size());
		int c = 0;
		for(CssToken tk : tokens) {
			if(tk.getType() == CssToken.Type.QNTY_FREQ) {
				c++;
			}
		}
		assertEquals(2, c);						
	}
	
	@Test
	public void testLexerQnty_17() throws Exception {
		// QNTY_RESOLUTION	{num}{D}{P}{I}, {num}{D}{P}{C}{M}
		String s = "10DPI 10dpcm";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());
		assertEquals(0, exceptions.size());
		int c = 0;
		for(CssToken tk : tokens) {
			if(tk.getType() == CssToken.Type.QNTY_RESOLUTION) {
				c++;
			}
		}
		assertEquals(2, c);						
	}
	
	@Test
	public void testLexerQnty_20() throws Exception {
		//{num}{ident}
		String s = "10foo";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(0).getType());
		assertEquals("10foo", tokens.get(0).getChars());								
	}
	
	@Test
	public void testLexerQnty_21() throws Exception {
		//{num}{ident}
		String s = "10emFOO";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(0).getType());
		assertEquals("10emFOO", tokens.get(0).getChars());
								
	}
	
	@Test
	public void testLexerQnty_22() throws Exception {
		String s = "10em10em";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(0).getType());
	}
	
	@Test
	public void testLexerQnty_23() throws Exception {
		String s = "10emFoo,10emBAR,10emBIP";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(5, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(0).getType());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(2).getType());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(4).getType());
		assertEquals(CssToken.Type.CHAR, tokens.get(1).getType());
		assertEquals(CssToken.Type.CHAR, tokens.get(3).getType());
		
	}
	
	@Test
	public void testLexerQnty_24() throws Exception {
		String s = "10emX";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_DIMEN, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerQnty_25() throws Exception {
		String s = "10\\px";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_LENGTH, tokens.get(0).getType());
		assertEquals("10px", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerQnty_26() throws Exception {
		String s = "10e\\m";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.QNTY_EMS, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerQnty_27() throws Exception {
		String s = "1 10 -10 +10 +.10 -.10";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(6, stripTokens(Type.S, tokens).size());
		assertEquals(0, exceptions.size());
		assertEquals(2, getTokenTypeCount(Type.NUMBER, tokens));
		assertEquals(4, getTokenTypeCount(Type.INTEGER, tokens));
	}

  @Test
  public void testLexerQnty_28REM() throws Exception {
    String s = "1rem 10rem -10rem +10rem +.10rem -.10rem";
    List<CssToken> tokens = execScan(s);
    assertEquals(6, stripTokens(Type.S, tokens).size());
    assertEquals(0, exceptions.size());
    assertEquals(6, getTokenTypeCount(Type.QNTY_REMS, tokens));
  }
  @Test
  public void testLexerQnty_28() throws Exception {
    String s = "1em 10em -10em +10em +.10em -.10em";
    List<CssToken> tokens = execScan(s);
    assertEquals(6, stripTokens(Type.S, tokens).size());
    assertEquals(0, exceptions.size());
    assertEquals(6, getTokenTypeCount(Type.QNTY_EMS, tokens));
  }


  @Test
	public void testLexerQnty_29() throws Exception {
		String s = "1em,10em,-10em,+10em,+.10em,-.10em";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(6, stripTokens(Type.CHAR, tokens).size());
		assertEquals(0, exceptions.size());
		assertEquals(6, getTokenTypeCount(Type.QNTY_EMS, tokens));
	}
	
	@Test
	public void testLexerQnty_30() throws Exception {
		String s = "1,10,-10,+10,+.10,-.10";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(6, stripTokens(Type.CHAR, tokens).size());
		assertEquals(0, exceptions.size());
		assertEquals(2, getTokenTypeCount(Type.NUMBER, tokens));
		assertEquals(4, getTokenTypeCount(Type.INTEGER, tokens));
	}
	
	
	@Test
	public void testLexerQnty_40() throws Exception {
		//all lengths in 3
		String s = "1vmin 1cm 1px 1mm 1in 1pt 1pc 1ch 1vw 1vh";
		List<CssToken> tokens = execScan(s);		
		assertEquals(10, stripTokens(Type.S, tokens).size());
		assertEquals(0, exceptions.size());
		assertEquals(10, getTokenTypeCount(Type.QNTY_LENGTH, tokens));
	}
	@Test
	public void testLexerQnty_41() throws Exception {
		//all angles in 3
		String s = "1grad 1turn 1deg 1rad";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(4, stripTokens(Type.S, tokens).size());
		assertEquals(0, exceptions.size());
		assertEquals(4, getTokenTypeCount(Type.QNTY_ANGLE, tokens));		
	}
	
	@Test
	public void testLexerQnty_42() throws Exception {
		//all resolution in 3
		String s = "1dpcm 1dppx 1dpi";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, stripTokens(Type.S, tokens).size());
		assertEquals(0, exceptions.size());
		assertEquals(3, getTokenTypeCount(Type.QNTY_RESOLUTION, tokens));		
	}

	@Test
	public void testLexerMQ_10() throws Exception {
		String s = "only";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.ONLY, tokens.get(0).getType());
		assertEquals("only", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerMQ_11() throws Exception {
		String s = "ONLy";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.ONLY, tokens.get(0).getType());
		assertEquals("only", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerMQ_12() throws Exception {
		String s = "onlyFOO";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());				
	}
	
	@Test
	public void testLexerMQ_20() throws Exception {
		String s = "not";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.NOT, tokens.get(0).getType());
		assertEquals("not", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerMQ_21() throws Exception {
		String s = "NoT";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.NOT, tokens.get(0).getType());
		assertEquals("not", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerMQ_22() throws Exception {
		String s = "notFOO";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());				
	}
	
	@Test
	public void testLexerMQ_30() throws Exception {
		String s = "and";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.AND, tokens.get(0).getType());
		assertEquals("and", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerMQ_31() throws Exception {
		String s = "AND";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.AND, tokens.get(0).getType());
		assertEquals("and", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerMQ_32() throws Exception {
		String s = "andFOO";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());				
	}
	
	@Test
	public void testLexerIncludes10() throws Exception {
		String s = "~=";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.INCLUDES, tokens.get(0).getType());				
	}
	
	@Test
	public void testLexerDashmatch10() throws Exception {
		String s = "|=";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.DASHMATCH, tokens.get(0).getType());				
	}
	
	@Test
	public void testLexerSelectors3_10() throws Exception {
//		PREFIXMATCH,		//		"^="
//		SUFFIXMATCH,		//		"$="             
//		SUBSTRINGMATCH,		//		"*="             
		String s = "^= $= *=";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(5, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.PREFIXMATCH, tokens.get(0).getType());
		assertEquals(CssToken.Type.SUFFIXMATCH, tokens.get(2).getType());
		assertEquals(CssToken.Type.SUBSTRINGMATCH, tokens.get(4).getType());
	}
	
	@Test
	public void testLexerSelectors3_20() throws Exception {             
		String s = "p[title*='hello']";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(6, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals(CssToken.Type.SUBSTRINGMATCH, tokens.get(3).getType());
	}
		
	@Test
	public void testLexerHash10() throws Exception {
		String s = "#n";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.HASHNAME, tokens.get(0).getType());
		assertEquals("#n", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerHash20() throws Exception {
		String s = "#name";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.HASHNAME, tokens.get(0).getType());	
		assertEquals("#name", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerHash30() throws Exception {
		String s = "#";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.CHAR, tokens.get(0).getType());			
	}
	
	@Test
	public void testLexerHash40() throws Exception {
		String s = "#!";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(2, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.CHAR, tokens.get(0).getType());
		assertEquals(CssToken.Type.CHAR, tokens.get(1).getType());
	}
	
	@Test
	public void testLexerHash50() throws Exception {
		String s = "#name 89";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.HASHNAME, tokens.get(0).getType());
		assertEquals(CssToken.Type.S, tokens.get(1).getType());
		assertEquals(CssToken.Type.INTEGER, tokens.get(2).getType());
	}
	
	@Test
	public void testLexerHash51() throws Exception {
		String s = "#\\name\\name";		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.HASHNAME, tokens.get(0).getType());
		assertEquals("#namename", tokens.get(0).getChars());
		
	}
	
	@Test
	public void testLexerImportant10() throws Exception {
		String s = "!important";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IMPORTANT, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerImportant20() throws Exception {
		String s = "!  \t\t\t\r\n\fimportant";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IMPORTANT, tokens.get(0).getType());
		
	}
	
	@Test
	public void testLexerImportant30() throws Exception {
		String s = "!  IMPORTANT";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(1, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IMPORTANT, tokens.get(0).getType());
	}
	
	@Test
	public void testLexerImportant40() throws Exception {
		String s = "!importan  ";
		
		List<CssToken> tokens = execScan(s);		
		assertEquals(3, tokens.size());
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.CHAR, tokens.get(0).getType());
		assertEquals(CssToken.Type.IDENT, tokens.get(1).getType());
		assertEquals(CssToken.Type.S, tokens.get(2).getType());
	}
	
	@Test
	public void testLexerUrange10() throws Exception {
		String s = "U+A5 U+0-7F U+590-5ff U+4E00-9FFF U+30?? U+FF00-FF9F U+??? U+0??? ";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(8, getTokenTypeCount(Type.URANGE, tokens));
		
	}
	
	@Test
	public void testLexerUrange20() throws Exception {
		String s = "U+A5,U+0-7F,U+590-5ff,U+4E00-9FFF,U+30??,U+FF00-FF9F,U+???,U+0???";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(8, getTokenTypeCount(Type.URANGE, tokens));
	}
	
	@Test
	public void testLexerUrange30() throws Exception {
		//first invalid URANGE, second not even an URANGE, instead expect IDENT (u) and CHAR (+)
		String s = "U+0?????? U+ "; 	
		List<CssToken> tokens = execScan(s);				
		assertEquals(1, exceptions.size());
		assertEquals(CssToken.Type.URANGE, tokens.get(0).getType());
		assertEquals(1, getTokenTypeCount(Type.URANGE, tokens));
		assertEquals(1, getTokenTypeCount(Type.IDENT, tokens));
		assertEquals(1, getTokenTypeCount(Type.CHAR, tokens));
		
	}
	
	@Test
	public void testLexerUrange40() throws Exception {		
		String s = "U+0????? U+00000A-00000F "; 	
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());		
		assertEquals(2, getTokenTypeCount(Type.URANGE, tokens));		
	}
	
	@Test
	public void testLexerUrange50() throws Exception {		
		String s = "\\U+A U+A"; 	
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());		
		assertEquals(1, getTokenTypeCount(Type.URANGE, tokens));		
	}
	
	@Test
	public void testLexerEscape09() throws Exception {
		String s = "\\";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(1, exceptions.size());
		assertEquals(CssToken.Type.CHAR, tokens.get(0).getType());		
	}
	
	@Test
	public void testLexerEscape10() throws Exception {
		//STRING: backslash + literal new skipped
		String s = "'a not s\\\no very long title'";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.STRING, tokens.get(0).getType());
		assertEquals("a not so very long title", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerEscape11() throws Exception {
		//STRING: propertly escaped newlines kept literally
		String s = "'an escaped newline: \\A'";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.STRING, tokens.get(0).getType());	
		assertEquals("an escaped newline: \\A", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerEscape12() throws Exception {
		//IDENT: base test
		String s = "\\64";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());	
		assertEquals("d", tokens.get(0).getChars());
				
	}
	
	@Test
	public void testLexerEscape13() throws Exception {
		//IDENT: base test
		String s = "\\p";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals("p", tokens.get(0).getChars());
		
	}
	
	@Test
	public void testLexerEscape14() throws Exception {
		//IDENT: base test
		String s = "\\px";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());
		assertEquals("px", tokens.get(0).getChars());
		
	}
	
	@Test
	public void testLexerEscape15() throws Exception {
		//IDENT: base test
		String s = "\\64 \\64 ";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.IDENT, tokens.get(0).getType());	
		assertEquals("dd", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerEscape16() throws Exception {
		String s = "#f\\oo";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(CssToken.Type.HASHNAME, tokens.get(0).getType());	
		assertEquals("#foo", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerEscape17() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-001.xht
		String s = "\\64\\69\\76{\\63\\6F\\6C\\6F\\72:\\67\\72\\65\\65\\6E;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape18() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-char-001.xht
		//Escaped characters are treated as normal characters.
		String s = "di\\v{c\\o\\l\\o\\r:green;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape19() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-001.xht
		//Escaped identifiers are parsed and spaces between them are ignored.	
		String s = "\\64\\69\\76 {\\63 \\6F \\6C \\6F \\72 :\\67 \\72 \\65 \\65 \\6E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape20() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-001.xht
		//Escaped identifiers are parsed and spaces between them are ignored.	
		String s = "\\64\\69\\76 {\\63 \\6F \\6C \\6F \\72 :\\67 \\72 \\65 \\65 \\6E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape21() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-002.xht
		//Escaped character sequences are terminated by white space.	
		String s = "div{\\63 \\06F \\006C \\0006F \\72 :\\067 \\0072 \\00065 \\00065 \\6E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape22() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-003.xht
		//Escaped character sequences are terminated by exactly six hex digits.			
		String s = "div{c\\00006Fl\\00006Fr:\\000067r\\000065e\\00006E;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape23() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-004.xht
		//Escaped character sequences are terminated by exactly six hex digits and white space.			
		String s = "div{\\000063 \\00006F \\00006C \\00006F \\000072 :\\000067 \\000072 \\000065 \\000065 \\00006E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}

	@Test
	public void testLexerEscape24() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-005.xht
		//Escaped character sequences are terminated by white space.			
		String s = "div{\\63 \\06F \\006C \\0006F \\72 :\\067 \\0072 \\00065 \\00065 \\6E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape25() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-006.xht
		//Escaped character sequences (two hex digits) are terminated by white space.			
		String s = "div{\\63 \\6F \\6C \\6F \\72 :\\67 \\72 ee\\6E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape26() throws Exception {
		//http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-spaces-007.xht
		//Escaped character sequences (less than six hex digits) are terminated by white space, tabs and linefeeds.			
		String s = "div{\\63\n\\06F\fl\\0006F\rr:g\\0072\t\\00065\r\fe\\6E ;}";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(7, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("div", tokens.get(0).getChars());
		assertEquals(Type.CHAR, tokens.get(1).getType());
		assertEquals("{", tokens.get(1).getChars());		
		assertEquals(Type.IDENT, tokens.get(2).getType());
		assertEquals("color", tokens.get(2).getChars());
		assertEquals(Type.CHAR, tokens.get(3).getType());
		assertEquals(":", tokens.get(3).getChars());
		assertEquals(Type.IDENT, tokens.get(4).getType());
		assertEquals("green", tokens.get(4).getChars());
		assertEquals(Type.CHAR, tokens.get(5).getType());
		assertEquals(";", tokens.get(5).getChars());
		assertEquals(Type.CHAR, tokens.get(6).getType());
		assertEquals("}", tokens.get(6).getChars());
	}
	
	@Test
	public void testLexerEscape27() throws Exception {
		String s = "\\0020red";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(1, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("\\0020red", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerEscape28() throws Exception {
		String s = "\\0020red\\0020red\\0020";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(1, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("\\0020red\\0020red\\0020", tokens.get(0).getChars());
	}
	
	@Test
	public void testLexerEscape29() throws Exception {
		String s = "\\000020 red\\000020 red\\000020";		
		List<CssToken> tokens = execScan(s);				
		assertEquals(0, exceptions.size());
		assertEquals(1, tokens.size());
		assertEquals(Type.IDENT, tokens.get(0).getType());
		assertEquals("\\000020 red\\000020 red\\000020", tokens.get(0).getChars());
	}
	
	/*
    
    	
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-014.xht  
	CSS Test: Escaping and spaces with declarations

   .test { color: white; background: green; }
   .test { color:\0020yellow; background:\0020red; }

	<p class="test">This line should be green.</p>

	
	
	
    
    
    
	   
      
		  
	
	
    
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-002.xht
    escaped "1st'Class'"    
    .\31st\'Class\27 
            {
                color: green;
            }

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-003.xht
    invalid: Newlines cannot be escaped within identifiers.            
            #my\
id
            {
                color: red;
            }


http://test.csswg.org/suites/css2.1/20110323/xhtml1/escaped-ident-004.xht
	The {unicode} chars are not treated like {escape} chars.
	
	.\a\b\c\1\2\3
	    {
	        color: red;
	    }
	    



            

            



            


http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-000.xht
	values identical
	  p.one:before { content: "This "; }
	  p.two:before { content: "Th\
	is "; }
	
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-001.xht
	  p { background: red; color: white; }
  	  p { font-family: "\"", '\'', serif; background: green; }
  	  
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-002.xht
  	CSS Test: Invalid Punctuation Escapes
  	  p.class#id { background: green; color: white; }
	  p\.class#id { background: red; }	
	  p.class\#id { background: red; }	
	  p.class#id { background\: red; }	
	  p.class#id { background: red\; }	
	  p.class#id \{ background: red; \}
	  p.class#id { background: red; } 
	  
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-003.xht
	  CSS Test: Class and Keyword Letter Escapes
	    p.class { background: red; color: white; }
  		p.c\lass { bac\kground: g\reen; }

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-004.xht  	
  	CSS Test: Unicode Escapes	
  p.class { background: red; color: white; }
  p.c\00006Cas\000073 { back\000067round: gr\000065en; }
  
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-005.xht  
  	CSS Test: Unicode Escapes and Case Insensitivity
    p.class { background: red; color: white; }
  	p.c\00006Cas\000073 { back\000047round: gr\000045en; }
  
http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-006.xht  
	CSS Test: Incorrect Letter Escape (Class Selector)  
	  p.class { background: green; color: white; }
  	  p.cl\ass { background: red; }

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-007.xht
	CSS Test: Space-terminated Unicode Escapes
	  p.class { background: red; color: white; }
	  p.c\06C ass { back\67 round: gr\000065 en; }
  

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-008.xht
	Invalid
	CSS Test: Invalid Space-terminated Character Escapes    
	      p.class { background: green; color: white; }
	  p.c\06C  ass { back\67round: r\000065 ed; }
	  p.c\06Cass { back\67
	 round: r\000065ed; }
	  p.c\06Cass { back\67round: r\000065 
	ed; }

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-009.xht
	CSS Test: Characters and case: Escaping a character inside a keyword
	Invalid
	p {color: green}
	p {color: r\ed}

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-010.xht
	Unicode escapes cannot substitute for '{' or '}' in CSS syntax.
	This text should be green, not red.

	p { color: green }
	p \7B color: red \7D

http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-012.xht
	This text should be green.
	'grEen'
	p { color: red; color: \g\r\45\65\n; } 


http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-011.xht
	'\r\e\d' is 'r^N^M', which isn't valid
	p { color: green; color: \r\e\d; } 


http://test.csswg.org/suites/css2.1/20110323/xhtml1/escapes-013.xht
	CSS Test: Escaping and spaces with classes
   .css\0031 p { color: yellow; background: red; }
   .css\0032 p { color: white; background: green; }
   
     <div class="css1"><p>This line should be unstyled.</p></div>
  <p class="css2p">This line should be green.</p>
  


*/	
	
	@Test
	public void testMessages() throws Exception {
		//tests the l12n properties file				
		for (CssErrorCode cec : CssExceptions.CssErrorCode.values()) {
			String s = Messages.get(cec.name());
			//the key is returned if there is no value
			assertTrue(!s.equals(cec.toString()));			
		}
	}
	
	private List<CssToken> execScan(String css, boolean debug) throws Exception {	
		exceptions.clear();
		final CssTokenList tokens = new CssTokenList();
		CssScanner lexer = new CssScanner(new StringReader(css), CssLocation.NO_SID, new ErrorListener(), new CssTokenConsumer() {			
			public void add(CssToken token) {
				tokens.add(token);				
			}
		});
		lexer.scan();
		if(debug) {
			outWriter.println("input: " + css);
			for(CssToken t : tokens) {
				outWriter.println('\t' + t.toString());
			}
			outWriter.println();
		}
		
		return tokens;
	}
	
	private List<CssToken> execScan(String css) throws Exception {		
		return execScan(css, false);
	}
	
	List<CssException> exceptions = Lists.newArrayList();
	class ErrorListener implements CssErrorHandler {
		public void error(CssException e) throws CssException {
			exceptions.add(e);			
		}		
	}
		
	private List<CssToken> stripTokens(Type type, List<CssToken> tokens) {	
		List<CssToken> list = Lists.newArrayList();
		for(CssToken tk : tokens) {
			if(tk.getType() != type) {
				list.add(tk);
			}
		}
		return list;
	}	
	
	private int getTokenTypeCount(CssToken.Type type, List<CssToken> tokens) {
		int c = 0;
		for(CssToken tk : tokens) {
			if(tk.getType() == type) {
				c++;
			}
		}
		return c;
	}
	
	private CssToken getFirstTokenOfType(CssToken.Type type, List<CssToken> tokens) {		
		for(CssToken tk : tokens) {
			if(tk.getType() == type) {
				return tk;
			}
		}
		return null;
	}
	
	private boolean hasIdent(List<CssToken> tokens, String ident) {
		for(CssToken tk : tokens) {
			if(tk.getType() == Type.IDENT && tk.getChars().equals(ident)) {
				return true;
			}
		}
		return false;
	}
		
}
