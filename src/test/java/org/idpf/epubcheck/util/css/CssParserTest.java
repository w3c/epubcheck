package org.idpf.epubcheck.util.css;

import static org.idpf.epubcheck.util.css.CssTokenList.Filters.FILTER_NONE;
import static org.idpf.epubcheck.util.css.CssTokenList.Filters.FILTER_S_CMNT;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssScannerException;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssConstruct;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammar.CssFunction;
import org.idpf.epubcheck.util.css.CssGrammar.CssQuantity;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelectorCombinator;
import org.idpf.epubcheck.util.css.CssGrammar.CssSimpleSelectorSequence;
import org.idpf.epubcheck.util.css.CssGrammar.CssURI;
import org.idpf.epubcheck.util.css.CssToken.Type;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class CssParserTest {
	private static final CssLocation MOCK_LOCATION = new CssLocation(-1,-1,0,CssLocation.NO_SID);
	
	@Test
	public void testParser000() throws Exception {
		String s = " ";
		checkBasics(exec(s));
	}
	
	@Test
	public void testParser000b() throws Exception {
		String s = "";
		checkBasics(exec(s));
	}
	
	@Test
	public void testParser001() throws Exception {
		String s = "@charset \t 'UTF-8'; <!-- ";
		HandlerImpl handler = checkBasics(exec(s));
		assertTrue(handler.startAtRuleCount == 1);
		assertTrue(handler.atRules.size() == 1);
		assertTrue(handler.atRules.get(0).getComponents().size() == 1);
		assertTrue(handler.atRules.get(0).getComponents().get(0).type == CssConstruct.Type.STRING);
		assertEquals("'UTF-8'", handler.atRules.get(0).getComponents().get(0).toCssString());
	}
	
	@Test
	public void testParser002() throws Exception {
		String s = "@charset \t 'UTF-8'; <!-- @namespace 'http://foo/ns' ; --> ";
		HandlerImpl handler = checkBasics(exec(s));	
		
		assertTrue(handler.startAtRuleCount == 2);
		assertTrue(handler.atRules.size() == 2);
		assertTrue(handler.atRules.get(0).toCssString().startsWith("@charset"));
		assertTrue(handler.atRules.get(1).toCssString().startsWith("@namespace"));
		assertTrue(handler.atRules.get(0).getComponents().size() == 1);
		assertTrue(handler.atRules.get(1).getComponents().size() == 1);
		assertTrue(handler.atRules.get(0).getComponents().get(0).type == CssConstruct.Type.STRING);
		assertTrue(handler.atRules.get(1).getComponents().get(0).type == CssConstruct.Type.STRING);
		assertEquals("'UTF-8'", handler.atRules.get(0).getComponents().get(0).toCssString());
		assertEquals("'http://foo/ns'", handler.atRules.get(1).getComponents().get(0).toCssString());
		
	}
	
	@Test
	public void testParser003() throws Exception {
		String s = "@charset 'UTF-8'; selector1 { name1a : property1a ; name1b : property1b } " 
				+ "selector2 { name2a : property2a ; name2b : property2b ; } ";
		HandlerImpl handler = checkBasics(exec(s));		
		assertTrue(handler.atRules.size() == 1);
		assertTrue(handler.selectors.size() == 2);
		assertTrue(handler.declarations.size() == 4);	
	}
	
	@Test
	public void testParser004() throws Exception {
		String s = "E { } E{}";
		HandlerImpl handler = checkBasics(exec(s));		
		assertTrue(handler.atRules.size() == 0);
		assertTrue(handler.selectors.size() == 2);
		assertTrue(handler.declarations.size() == 0);	
	}
	
	@Test
	public void testParser005() throws Exception {
		String s = "E { border : 1px 2px 3px 4px } ";
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.declarations.size());
		assertEquals(4, handler.declarations.get(0).components.size());
		for(CssConstruct cc : handler.declarations.get(0).components) {
			assertTrue(cc instanceof CssQuantity);
			assertTrue(((CssQuantity)cc).subType == CssQuantity.Unit.LENGTH);
		}
	}
	
	@Test
	public void testParser006() throws Exception {
		String s = "E { foo : bar(1px 2em 'foo' bar) } ";
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.declarations.size());
		assertEquals(1, handler.declarations.get(0).components.size());
		assertTrue(handler.declarations.get(0).components.get(0) instanceof CssFunction);		
	}
	
	@Test
	public void testParser007() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css1));		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testParser008() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css2));		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testParser009() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css3));		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testParser010() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css4));		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testParser011() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css5));		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testParser012() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css6));		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testParser013() throws Exception {		
		HandlerImpl handler = checkBasics(exec(css7));		
		assertEquals(0, handler.errors.size());
	}
			
	@Test
	public void testParserAtRule001() throws Exception {
		//@rule with declaration block
		String s = "@rule {n1:v1;n2:v2;} ";
		HandlerImpl handler = checkBasics(exec(s));
		assertTrue(handler.atRules.get(0).hasBlock);
		assertEquals(0, handler.selectors.size());		
		assertEquals(2, handler.declarations.size());		
	}
	
	@Test
	public void testParserAtRule002() throws Exception {
		//@rule with ruleset
		String s = "@rule{s1{n1:v1}s2{n2:v2}} ";
		HandlerImpl handler = checkBasics(exec(s));	
		assertTrue(handler.atRules.get(0).hasBlock);
		assertEquals(2, handler.selectors.size());
		assertEquals(2, handler.declarations.size());			
	}
		
	@Test
	public void testParserAtRule003() throws Exception {
		//@rule with no block
		String s = "@rule;";
		HandlerImpl handler = checkBasics(exec(s));	
		assertFalse(handler.atRules.get(0).hasBlock);			
	}
	
	@Test
	public void testParserAtRule004() throws Exception {
		//@rule with ruleset and nested declblock atrule
		String s = "@rule1{ s1 {n1:v1} @rule2 {n2:v2} } ";
		HandlerImpl handler = checkBasics(exec(s));	
		assertEquals(2, handler.atRules.size());
		assertTrue(handler.atRules.get(0).hasBlock);
		assertTrue(handler.atRules.get(1).hasBlock);		
		assertEquals(1, handler.selectors.size());
		assertEquals(2, handler.declarations.size());			
	}
	
	@Test
	public void testParserAtRule005() throws Exception {
		String s = "@rule1{ E1 {n1:v1} @rule2 {n2:v2} } @rule3 { E2 { n3:v3; } }";
		HandlerImpl handler = checkBasics(exec(s));	
		assertEquals(3, handler.atRules.size());
		assertTrue(handler.atRules.get(0).hasBlock);
		assertTrue(handler.atRules.get(1).hasBlock);		
		assertEquals(2, handler.selectors.size());
		assertEquals(3, handler.declarations.size());			
	}

	@Test
	public void testParserAtRule006() throws Exception {
		String s = "@media all and (min-width: 60em) and (min-height: 50em) { E { p1:v1;} }";
		HandlerImpl handler = checkBasics(exec(s));	
		assertEquals(1, handler.atRules.size());
		assertEquals(5, handler.atRules.get(0).getComponents().size());
		assertTrue(handler.atRules.get(0).hasBlock);		
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.declarations.size());			
	}
	
	@Test
	public void testParserAtRule007() throws Exception {
		String s = "@rule one, 2, [3,4], (5,6)  { E { p1:v1;} }";
		HandlerImpl handler = checkBasics(exec(s));	
		assertEquals(1, handler.atRules.size());
		assertEquals(7, handler.atRules.get(0).getComponents().size());
		assertTrue(handler.atRules.get(0).hasBlock);		
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.declarations.size());			
	}
	
	@Test
	public void testParserAtRule008() throws Exception {
		String s = "@rule one, 2, [3,4], (5,6);";
		HandlerImpl handler = checkBasics(exec(s));	
		assertEquals(1, handler.atRules.size());
		assertEquals(7, handler.atRules.get(0).getComponents().size());
		assertFalse(handler.atRules.get(0).hasBlock);		
		assertEquals(0, handler.selectors.size());
		assertEquals(0, handler.declarations.size());			
	}
	
	
	
	@Test
	public void testParserSelector001() throws Exception {
		String s = "E1 {}";
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.selectors.get(0).size());
		
		List<CssSelector> selectors = handler.selectors.get(0);
		assertEquals(1, selectors.size());
		CssSelector first = selectors.get(0);
		assertTrue(first.getComponents().get(0) instanceof CssSimpleSelectorSequence);		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)first.getComponents().get(0);		
		assertEquals("E1", seq.getComponents().get(0).toCssString());
		
								
	}
	
	@Test
	public void testParserSelector002() throws Exception {
		
		String s = "E1,E2,E3 {}";
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(1, handler.selectors.size());
		
		List<CssSelector> selectors = handler.selectors.get(0);
		assertEquals(3, selectors.size());
		
		CssSelector first = selectors.get(0);
		assertTrue(first.getComponents().get(0) instanceof CssSimpleSelectorSequence);		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)first.getComponents().get(0);		
		assertEquals("E1", seq.getComponents().get(0).toCssString());
		
		CssSelector third = selectors.get(2);
		assertTrue(third.getComponents().get(0) instanceof CssSimpleSelectorSequence);		
		CssSimpleSelectorSequence seq2 = (CssSimpleSelectorSequence)third.getComponents().get(0);		
		assertEquals("E3", seq2.getComponents().get(0).toCssString());
						
	}
	
	@Test
	public void testParserSelector003() throws Exception {		
		String s = "E1 E2 {}";
		HandlerImpl handler = checkBasics(exec(s));
		
		assertEquals(1, handler.selectors.size());		
		List<CssSelector> selectors = handler.selectors.get(0);
		assertEquals(1, selectors.size());		
		CssSelector first = selectors.get(0);
		assertTrue(first.getComponents().get(0) instanceof CssSimpleSelectorSequence);
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)first.getComponents().get(0);		
		assertEquals("E1", seq.getComponents().get(0).toCssString());
		assertTrue(first.getComponents().get(1) instanceof CssSelectorCombinator);
		assertTrue(first.getComponents().get(2) instanceof CssSimpleSelectorSequence);
								
	}
	
	@Test
	public void testParserSelector004() throws Exception {
		String s = "A B C D > E + F {} ";
		HandlerImpl handler = checkBasics(exec(s));
		assertEquals(1, handler.selectors.get(0).size());
	}
	
	@Test
	public void testParserSelector005() throws Exception {
		String s = "A, B, C  , D > E, F + G {} ";
		HandlerImpl handler = checkBasics(exec(s));	
		assertEquals(5, handler.selectors.get(0).size());
	}
	
	@Test
	public void testParserSelector006() throws Exception {
		String s = "html|*:not(:link):not(:visited) { float: right; }";
		checkBasics(exec(s));		
	}
	
	@Test
	public void testParserSelector007() throws Exception {
		String s = "a:focus:hover {}";
		checkBasics(exec(s));		
	}
	
	@Test
	public void testParserSelectorsFunctionalPseudoValid001() throws Exception {
		String s = 
			 "tr:nth-child(2n+1) {} /* represents every odd row of an HTML table */"
			+"tr:nth-child(odd)  {}/* same */"
			+"tr:nth-child(2n+0) {}/* represents every even row of an HTML table */"
			+"tr:nth-child(even) {}/* same */"
			+"/* Alternate paragraph colours in CSS */"
			+"p:nth-child(4n+1) { color: navy; }"
			+"p:nth-child(4n+2) { color: green; }"
			+"p:nth-child(4n+3) { color: maroon; }"
			+"p:nth-child(4n+4) { color: purple; }"
			+":nth-child(10n-1)  {}/* represents the 9th, 19th, 29th, etc, element */"
			+":nth-child(10n+9)  {}/* Same */"
			+":nth-child( 3n + 1 ) {}"
			+":nth-child( +3n - 2 ) {}"
			+":nth-child( -n+ 6) {}"
			+":nth-child( +6 ) {}";			
		checkBasics(exec(s));		
	}
	
	@Test
	public void testParserSelectorsFunctionalPseudoInvalid001() throws Exception {
		//all invalid TODO these pass without exception, break at parser level or leave to validator...
		String s = 
			 ":nth-child(10n+-1) {}"
			+":nth-child(3 n) {}"
			+":nth-child(+ 2n) {}"
			+":nth-child(+ 2) {}";
		checkBasics(exec(s));		
	}
	
	@Test
	public void testParserSelectorsKeyword() throws Exception {
		//w3c css validator has parse error here too
		
		String s = "not {}";			
		checkBasics(exec(s), true);		
	}
	
	@Test
	public void testParserAttrAndChildSelector() throws Exception {			
		String s = "*[compact='yes']>li { margin-top: 0em;}";			
		HandlerImpl handler = checkBasics(exec(s));
		assertEquals(1, handler.selectors.get(0).size());
		assertEquals(1, handler.declarations.size());
	}

	@Test
	public void testParserSelectorsEscapedInit1() throws Exception {
		String s = "\\hot {}";			
		checkBasics(exec(s), false);				
	}
		
	@Test 
	public void testParserSelectorBulk() throws Exception {
		String s = "*  {} " +
				"E {} " +
				"*|E {} " +
				"ns|E {} " +
				"|E {} " +
				"E[foo] {} " +
				"E[foo='bar'] {} " +
				"E[foo~='bar'] {} " +
				"E[foo^='bar'] {} " +
				"E[foo$='bar'] {} " +
				"E[foo*='bar'] {} " +
				"E[foo|='en'] {} " +
				"E:root {} " +
				"E:nth-child(n) {} " +
				"E:nth-last-child(n) {} " +
				"E:nth-of-type(n) {} " +
				"E:nth-last-of-type(n) {} " +
				"E:first-child {} " +
				"E:last-child {} " +
				"E:first-of-type {} " +
				"E:last-of-type {} " +
				"E:only-child {} " +
				"E:only-of-type {} " +
				"E:empty {} " +
				"E:link {} " +
				"E:visited {} " +
				"E:active {} " +
				"E:hover {} " +
				"E:focus {} " +
				"E:target {} " +
				"E:lang(fr) {} " +
				"E:enabled {} " +
				"E:disabled {} " +
				"E:checked {} " +
				"E::first-line {} " +
				"E::first-letter {} " +
				"E::before {} " +
				"E::after {} " +
				"E.warning {} " +
				"E#myid {} " +
				"E:not(s) {} " +
				"E F	 {} " +
				"E > F {} " +
				"E + F {} " +
				"E ~ F {} " +
				"A B C D > E + F {} ";
		HandlerImpl handler = checkBasics(exec(s));
		assertEquals(46, handler.selectors.size());
	}
	
	@Test
	public void testParserSelectorErr001() throws Exception {
		String s = "E1,E2, {}";
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr002() throws Exception {
		String s = ",E1,E2 {}";
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr003() throws Exception {
		String s = "E1,,E2 {}";
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr004() throws Exception {
		String s = "E1,E2 $ {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr005() throws Exception {
		String s = "E[att=123] {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr006() throws Exception {
		String s = "E[att 'val'] {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr007() throws Exception {
		String s = "E['val'] {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr008() throws Exception {
		String s = "E[*|2] {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorErr009() throws Exception {
		String s = "E[|2] {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(0, handler.selectors.size());
		assertEquals(1, handler.errors.size());						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping001() throws Exception {
		String s = "A B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.DESCENDANT, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping002a() throws Exception {
		String s = "A > B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.CHILD, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping002b() throws Exception {
		String s = "A>B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.CHILD, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping003a() throws Exception {
		String s = "A + B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.ADJACENT_SIBLING, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping003b() throws Exception {
		String s = "A+B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.ADJACENT_SIBLING, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping004a() throws Exception {
		String s = "A ~ B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.GENERAL_SIBLING, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);						
	}
	
	@Test
	public void testParserSelectorCombinatorTyping004b() throws Exception {
		String s = "A~B {}"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		CssSelector selector = handler.selectors.get(0).get(0);
		assertEquals(3, selector.components.size());
		
		CssSimpleSelectorSequence seq = (CssSimpleSelectorSequence)selector.components.get(0);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);
		
		assertEquals(CssConstruct.Type.COMBINATOR, selector.components.get(1).type);
		CssSelectorCombinator comb = (CssSelectorCombinator) selector.components.get(1);
		assertEquals(CssSelectorCombinator.Type.GENERAL_SIBLING, comb.subType);
		
		seq = (CssSimpleSelectorSequence)selector.components.get(2);
		assertEquals(CssConstruct.Type.TYPE_SELECTOR, seq.getComponents().get(0).type);						
	}
	
	@Test
	public void testParserImportant001() throws Exception {
		String s = "E { n1 : v1 !important } E2 { n2 : v2 !important}"; 
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(2, handler.selectors.size());
		assertEquals(0, handler.errors.size());
		assertEquals(2, handler.declarations.size());
		for(CssDeclaration decl : handler.declarations) {
			assertTrue(decl.important);
		}
	}
	
	@Test
	public void testParserImportant002() throws Exception {
		String s = "E { n1 : v1 !important ; n2 : v2;}"; 
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(1, handler.selectors.size());
		assertEquals(0, handler.errors.size());
		assertEquals(2, handler.declarations.size());
		assertTrue(handler.declarations.get(0).important);
		assertFalse(handler.declarations.get(1).important);		
	}
	
	@Test
	public void testParserImportant003() throws Exception {
		String s = "E { n1 : v1 !important ; n2 : v2 !important}"; 
		HandlerImpl handler = checkBasics(exec(s));		
		assertEquals(1, handler.selectors.size());
		assertEquals(0, handler.errors.size());
		assertEquals(2, handler.declarations.size());
		for(CssDeclaration decl : handler.declarations) {
			assertTrue(decl.important);
		}
	}
	
	@Test
	public void testParserImportant004() throws Exception {
		String s = "E { n1 : !important }"; 
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertEquals(1, handler.selectors.size());
		assertEquals(1, handler.errors.size());
		assertEquals(0, handler.declarations.size());
		
	}
	
	@Test
	public void testParserErr001() throws Exception {
		String s = "@charset ";
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertTrue(handler.atRules.size() == 1);
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_PREMATURE_EOF
				, handler.errors.get(0).errorCode);
	}
	
	@Test
	public void testParserErr002() throws Exception {
		//first atrule skipped
		String s = "@charset } ; @namespace 'http://foo.ns' ; ";
		HandlerImpl handler = checkBasics(exec(s), true);		
		assertTrue(handler.atRules.size() == 1);
		assertTrue(handler.atRules.get(0).toCssString().startsWith("@namespace"));
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN
				, handler.errors.get(0).errorCode);		
	}
	

	@Test
	public void testParserErr003() throws Exception {
		// note the skip algo goes to '}' 
		String s = " E { name1 : ; name2 : value2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(0, handler.declarations.size());
	}
	
	@Test
	public void testParserErr004() throws Exception {
		//no value for second token, first decl should be retained
		String s = " E { name1 : value1 ; : value2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(1, handler.declarations.size());
		assertEquals("name1", handler.declarations.get(0).getName().get());
	}
	
	@Test
	public void testParserErr005() throws Exception {
		//a stray delim following first decl
		String s = " E { name1 : value1 ; : }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(1, handler.declarations.size());
		assertEquals("name1", handler.declarations.get(0).getName().get());
	}
	
	@Test
	public void testParserErr006() throws Exception {
		//first propname not ident, second should be retained
		String s = " E { 10% : 20% ; name2 : value2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(1, handler.declarations.size());
		assertEquals("name2", handler.declarations.get(0).getName().get());
	}
	
	@Test
	public void testParserErr007() throws Exception {
		String s = " E { ;:;:;:;:;:;:;:;:; } E { foo : bar }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertTrue(handler.errors.size() > 0 );
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(1, handler.declarations.size());
		assertEquals("foo", handler.declarations.get(0).getName().get());	
	}
	
	@Test
	public void testParserErr008() throws Exception {
		String s = " E {{ p1 : v1 ; p2 : v2; }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(1, handler.declarations.size());
		assertEquals("p2", handler.declarations.get(0).getName().get());			
	}
	
	@Test
	public void testParserErr009() throws Exception {
		String s = " E {{ p1 : v1 ; p2 : v2; ";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(2, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_EXPECTING_TOKEN, handler.errors.get(0).errorCode);
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_PREMATURE_EOF, handler.errors.get(1).errorCode);
		assertEquals(1, handler.declarations.size());
		assertEquals("p2", handler.declarations.get(0).getName().get());		
	}
	
	@Test
	public void testParserErr010() throws Exception {
		//extra '}' which causes entire second ruleset to be skipped too
		String s = " E1 {p1a:v1a;p1b:v1b;}} E2 {p2a:v2a;p2b:v2b;}";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, handler.errors.get(0).errorCode);		
		assertEquals(2, handler.declarations.size());
		assertEquals("p1b", handler.declarations.get(1).getName().get());
		assertEquals(1, handler.selectors.size());				
	}
	
	@Test
	public void testParserErr011a() throws Exception {
		//function not closing
		String s = " E1 { foo : bar( 1px, 2px, 3px ; } E2 { foo2 : bar2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, handler.errors.get(0).errorCode);		
		assertEquals(1, handler.declarations.size());
		assertEquals("foo2", handler.declarations.get(0).getName().get());						
			
	}
	
	@Test
	public void testParserErr011b() throws Exception {
		//function not closing, because err token is '}' next decl is skipped too...
		String s = " E1 { foo : bar( 1px, 2px, 3px } E2 { foo2 : bar2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, handler.errors.get(0).errorCode);		
		assertEquals(0, handler.declarations.size());										
	}
	
	@Test
	public void testParserErr011ok() throws Exception {
		//function not closing
		String s = " E1 { foo : bar( 1px, 2px, 3px ); } E2 { foo2 : bar2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(0, handler.errors.size());				
		assertEquals(2, handler.declarations.size());
		assertEquals("foo2", handler.declarations.get(1).getName().get());							
	}
	
	@Test
	public void testParserErr012() throws Exception {
		//prop value
		String s = " E { foo : {23 } E2 {foo2:bar}";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, handler.errors.get(0).errorCode);		
		assertEquals(1, handler.declarations.size());
		assertEquals("foo2", handler.declarations.get(0).getName().get());						
			
	}
	
	@Test
	public void testParserErr013() throws Exception {
		//check that at-rule skip-to-} skips past nested blocks
		String s = "@outer } { @inner1 { p1:v1 } @inner2 { p1:v1 } } E { p2:v2 }";
		HandlerImpl handler = checkBasics(exec(s), true);						
		assertEquals(1, handler.errors.size());
		assertEquals(CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, handler.errors.get(0).errorCode);		
		assertEquals(1, handler.declarations.size());
		assertEquals("p2", handler.declarations.get(0).getName().get());						
				
	}
	
	@Test
	public void testParserFile001() throws Exception {		
		HandlerImpl handler = checkBasics(execFile("counter-styles.css", false));						
		assertEquals(31, handler.atRules.size());
	}
	
	@Test
	public void testParserFile002() throws Exception {		
		checkBasics(execFile("counter-styles.css", false));								
	}
	
	@Test
	public void testParserFile003() throws Exception {		
		checkBasics(execFile("counter-styles.css", false));								
	}
	
	@Test
	public void testParserFile004() throws Exception {		
		checkBasics(execFile("counter-styles.css", false));								
	}
	
	@Test
	public void testGrammarToCssString001() throws Exception {				
		String s = "E {color:black}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.declarations.size());
		assertEquals("color : black ;", handler.declarations.get(0).toCssString());							
	}
	
	@Test
	public void testGrammarToCssString002() throws Exception {				
		String s = "@foo 'bar'; @baz quux('qing'); ";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(2, handler.atRules.size());
		assertEquals("@foo 'bar'", handler.atRules.get(0).toCssString());
		assertEquals("@baz quux('qing')", handler.atRules.get(1).toCssString());
	}
	@Test
	public void testGrammarToCssString003() throws Exception {				
		String s = "A,B,C {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		List<CssSelector> list = handler.selectors.get(0);
		assertEquals("A", list.get(0).toCssString());
		assertEquals("B", list.get(1).toCssString());
		assertEquals("C", list.get(2).toCssString());			
	}
	
	@Test
	public void testGrammarToCssString004() throws Exception {				
		String s = "A>B {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("A>B", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString005() throws Exception {				
		String s = "A B {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("A B", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString006() throws Exception {				
		String s = "tr:nth-child(odd)  {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("tr:nth-child(odd)", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString007() throws Exception {				
		String s = "E:first-child {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E:first-child", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString008() throws Exception {				
		String s = "*|E {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("*|E", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString009() throws Exception {				
		String s = "E[foo='bar'] {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E[foo='bar']", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString010() throws Exception {				
		String s = "E:not(s) {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E:not(s)", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString011() throws Exception {				
		String s = "E::first-letter {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E::first-letter", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString012() throws Exception {				
		String s = "E[foo^='bar'] {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E[foo^='bar']", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString013() throws Exception {				
		String s = "E[a='b'][c='d'] {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E[a='b'][c='d']", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString014() throws Exception {				
		String s = "E[*|a='b'][p|c='d'] {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("E[*|a='b'][p|c='d']", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString015() throws Exception {				
		String s = "tr:nth-child(2n+0) {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("tr:nth-child(2n+0)", handler.selectors.get(0).get(0).toCssString());		
	}
		
	@Test
	public void testGrammarToCssString016() throws Exception {				
		String s = "body > h2:not(:first-of-type):not(:last-of-type) {}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals(1, handler.selectors.size());
		assertEquals("body>h2:not(:first-of-type):not(:last-of-type)", handler.selectors.get(0).get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToCssString017() throws Exception {				
		String s = "E {color:black}";
		HandlerImpl handler = checkBasics(exec(s));												
		assertEquals("color : black ;", handler.declarations.get(0).toCssString());		
	}
	
	@Test
	public void testGrammarToURIString001() throws Exception {				
		String s = "url(foo)";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("foo", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString002() throws Exception {				
		String s = "url('foo')";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("foo", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString003() throws Exception {				
		String s = "url(\"foo\")";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("foo", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString004() throws Exception {				
		String s = "url(  \"foo\"  )";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("foo", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString005() throws Exception {				
		String s = "url(  \'foo\'  )";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("foo", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString006() throws Exception {				
		String s = "url(  \"f o o\"  )";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("f o o", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString007() throws Exception {				
		String s = "url(  \"  f o o  \"  )";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("f o o", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString008() throws Exception {				
		String s = "url()";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString009() throws Exception {				
		String s = "url('')";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("", cu.toUriString());				
	}
	
	@Test
	public void testGrammarToURIString010() throws Exception {				
		String s = "url( ' ' )";
		CssURI cu = new CssURI(s, MOCK_LOCATION);
		assertEquals("", cu.toUriString());				
	}
	
	@Test
	public void testParseStyleAttribute001() throws Exception {				
		String s = "color:black";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(1, handler.declarations.size());
		assertEquals(0, handler.errors.size());				
	}
	
	@Test
	public void testParseStyleAttribute002() throws Exception {				
		String s = "color:black; color:green";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(2, handler.declarations.size());
		assertEquals(0, handler.errors.size());				
	}
	
	@Test
	public void testParseStyleAttribute003() throws Exception {				
		String s = " color:black; color:green; color : blue ; ";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(3, handler.declarations.size());
		assertEquals(0, handler.errors.size());				
	}
	
	@Test
	public void testParseStyleAttribute004() throws Exception {				
		String s = " color:black !important";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(1, handler.declarations.size());
		assertEquals(true, handler.declarations.get(0).important);
		assertEquals(0, handler.errors.size());				
	}
	
	@Test
	public void testParseStyleAttribute005() throws Exception {				
		String s = "{color:black}";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(0, handler.declarations.size());		
		assertEquals(1, handler.errors.size());				
	}
	
	@Test
	public void testParseStyleAttribute006() throws Exception {				
		String s = "color:black}";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(0, handler.declarations.size());		
		assertEquals(1, handler.errors.size());
	}
	
	@Test
	public void testParseStyleAttribute007() throws Exception {				
		String s = "color";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(0, handler.declarations.size());		
		assertEquals(1, handler.errors.size());
	}
	
	@Test
	public void testParseStyleAttribute008() throws Exception {				
		String s = ": color";
		HandlerImpl handler = execStyleAttr(s, false);												
		assertEquals(0, handler.declarations.size());		
		assertEquals(1, handler.errors.size());
	}
	
	@Test
	public void testParseStyleAttribute009() throws Exception {
		//Issue 238
		String s = ";font-size:83%;";
		HandlerImpl handler = execStyleAttr(s, false);	
		assertEquals(1, handler.declarations.size());		
		assertEquals(0, handler.errors.size());
	}
	
	@Test
	public void testIssue231() throws Exception {
		//line numbers, CR+LF lf=\n, cr=\r
		HandlerImpl handler = execFile("issue231-crlf.css", false);
		int line = handler.errors.get(0).location.line;
		int col = handler.errors.get(0).location.col;
		assertEquals(102, line);		
		assertEquals(1, col);
		
		handler = execFile("issue231-lf.css", false);
		line = handler.errors.get(0).location.line;
		col = handler.errors.get(0).location.col;
		assertEquals(102, line);		
		assertEquals(1, col);
	}
	
	@Test
	public void testIssue240_1() throws Exception {
		HandlerImpl handler = execFile("issue240-1.css", false);
		assertEquals(0, handler.errors.size());		
	}
	
	@Test
	public void testIssue240_2() throws Exception {
		HandlerImpl handler = execFile("issue240-2.css", false);
		assertEquals(0, handler.errors.size());		
	}
		
	@Test
	public void testIssue241() throws Exception {
		HandlerImpl handler = execFile("issue241.css", false);
		assertEquals(0, handler.errors.size());			
	}
	
	@Test
	public void testIssue262() throws Exception {
		HandlerImpl handler = execFile("issue262.css", false);
		assertEquals(0, handler.errors.size());		
	}
	
	@Test
	public void testIssue333() throws Exception {
//	  String s = "color: rgba(198,215,225,0);";
	  String s = "background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, rgba(198,215,225,0)), color-stop(100%, rgba(198,215,225,1)));";
    HandlerImpl handler = execStyleAttr(s, false);                        
    assertEquals(1, handler.declarations.size());
    assertEquals(0, handler.errors.size());   
	}
	
	HandlerImpl exec(String css, boolean debug) throws IOException, CssException {		
		HandlerImpl handler = new HandlerImpl(debug);
		new CssParser().parse(new StringReader(css), CssLocation.NO_SID, handler, handler);
		return handler;
	}
	
	HandlerImpl execStyleAttr(String css, boolean debug) throws IOException, CssException {		
		HandlerImpl handler = new HandlerImpl(debug);
		new CssParser().parseStyleAttribute(new StringReader(css), CssLocation.NO_SID, handler, handler);
		return handler;
	}
	
	HandlerImpl execFile(String file, boolean debug) throws IOException, CssException {		
		HandlerImpl handler = new HandlerImpl(debug);
		URL fileURL = this.getClass().getResource(CssInputStreamTest.PATH_TEST_BASE + file);
		CssSource cs = new CssSource(fileURL.toString(), fileURL.openStream());
		new CssParser().parse(cs.newReader(), CssLocation.NO_SID, handler, handler);
		cs.getInputStream().close();
		return handler;
	}
	
	HandlerImpl exec(String css) throws IOException, CssException {
		return exec(css, false);
	}
	
	HandlerImpl checkBasics(HandlerImpl handler) {
		return checkBasics(handler, false);
	}
	
	HandlerImpl checkBasics(HandlerImpl handler, boolean expectErrors) {
		assertTrue("No #startDocument", handler.sawStartDocument);
		assertTrue("No #endDocument", handler.sawEndDocument);
		assertTrue("Uneven start+end atrule count: " + handler.startAtRuleCount + ", " + handler.endAtRuleCount, handler.startAtRuleCount == handler.endAtRuleCount);
		for(CssAtRule atRule : handler.atRules) {
			assertFalse("No keyword in atrule", Strings.isNullOrEmpty(atRule.getName().get()));
			assertTrue("No start '@' in atrule", atRule.getName().get().startsWith("@"));
			assertTrue("Null parameters in atrule", atRule.getComponents() != null);
		}

		if(handler.errors.size() > 0 && !expectErrors) {
			for(CssException exc :handler.errors) {
				System.err.println(exc.getMessage() + ": " + exc.getLocation().getLine() + " " + exc.getErrorCode());
			}
		}
		
		if(!expectErrors)
			assertTrue(handler.errors.size() == 0);

		return handler;
	}
	
	static class HandlerImpl implements CssErrorHandler, CssContentHandler {
		List<CssException> errors = Lists.newArrayList();
		List<CssAtRule> atRules = Lists.newArrayList();
		List<CssDeclaration> declarations = Lists.newArrayList();
		List<List<CssSelector>> selectors = Lists.newArrayList();
		boolean sawStartDocument = false;
		boolean sawEndDocument = false;
		boolean print = false;
		int startAtRuleCount;
		int endAtRuleCount;		
		PrintStream out = System.out;
		int callbacks;
		
		HandlerImpl(boolean printStdOut) {
			this.print = printStdOut;
		}
		
		public void startDocument() {
			sawStartDocument = true;	
			callbacks++;
			if(print) {
				out.println("#startDocument");
			}
		}

		public void endDocument() {
			sawEndDocument = true;
			callbacks++;
			if(print) {
				out.println("#endDocument");
			}	
		}

		public void startAtRule(CssAtRule atRule) {
			startAtRuleCount++;		
			callbacks++;
			if(print) {
				out.println("#" + atRule.toString());				
			}
			atRules.add(atRule);
		}

		public void endAtRule(String name) {
			endAtRuleCount++;
			callbacks++;
			if(print) {
				out.println("#endAtRule");
			}
		}

		public void selectors(List<CssSelector> sel) {
			selectors.add(sel);
			callbacks++;
			if(print) {
				out.println("#selectors: " +  Joiner.on(", ").join(sel));
			}
		}

    @Override
    public void endSelectors(List<CssSelector> sel) {
      // do nothing
    }

    public void declaration(CssDeclaration declaration) {
			declarations.add(declaration);
			callbacks++;
			if(print) {
				out.println("#" + declaration.toString());
			}
			
		}

		public void error(CssException e) throws CssException {
			errors.add(e);
			callbacks++;
			if(print) {
				out.println("#error: " + e.toString());
			}
		}
		
	}
	
		
	static final CssLocation loc = new CssLocation(1, 1, 1, CssLocation.NO_SID);
	static final List<CssScannerException> err = Lists.newArrayList();
	static final String text = "text";
	static final CssToken com = new CssToken(Type.COMMENT, loc, text, err);
	static final CssToken cdo = new CssToken(Type.CDO, loc, "<!--", err);
	static final CssToken chr = new CssToken(Type.CHAR, loc, "x", err);
	static final CssToken spc = new CssToken(Type.S, loc, " ", err);
	
	@Test
	public void testTokenList10() throws Exception {		
		CssTokenList ctl = new CssTokenList();		
		assertEquals(0, countTokens(ctl.iterator(FILTER_NONE)));
	}
	
	@Test
	public void testTokenList11() throws Exception {		
		CssTokenList ctl = new CssTokenList();
		ctl.add(com);		
		assertEquals(1, countTokens(ctl.iterator(FILTER_NONE)));
	}
		
	@Test
	public void testTokenList12() throws Exception {		
		CssTokenList ctl = new CssTokenList();
		ctl.add(spc);
		ctl.add(chr);
		ctl.add(chr);
		ctl.add(com);
		ctl.add(chr);
		CssTokenIterator iter = ctl.iterator(FILTER_S_CMNT);
		assertEquals(3, countTokens(iter));
		assertEquals(4, iter.index());
	}
		
	private int countTokens(CssTokenIterator iter) {
		int c = 0;
		while(iter.hasNext()) {
			iter.next();
			c++;
		}
		return c;
	}
	
	
	public static String css1 = ".linkparent1 { color: #a00; }" 
			+ " .linkparent1:hover span " 
			+"{" 
			+ "		left: auto;"
			+ "}  " 
			+ "/* this hover on the link changes the nested span's left value to auto */"
			+ " .linkparent1 span " 
			+ "{" 
			+ "  	position: absolute; " 
			+ " 	left: -999em; "
			+ " 	border: 1px solid white; " 
			+ "		background: #446;" 
			+ " 	color: white;"
			+ " 	font-weight: bold;" 
			+ " 	padding: 2px 4px;" 
			+ " 	text-decoration: none; "
			+ " }  /* tooltip may be custom styled as desired */ " 
			+ " "
			+ ".linkparent1:hover { "
			+ "    background: url(bgfix.gif); " 
			+ "}";

	public static String css2 = ".gradient-bg {"
			+ " background-color: #1a82f7; "
			+ " background-image: url(images/fallback-gradient.png); "
			+ " background-image: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#2F2727), to(#1a82f7)); "
			+ " background-image: -webkit-linear-gradient(top, #2F2727, #1a82f7); "
			+ " background-image:    -moz-linear-gradient(top, #2F2727, #1a82f7); "
			+ " background-image:     -ms-linear-gradient(top, #2F2727, #1a82f7); "
			+ " background-image:      -o-linear-gradient(top, #2F2727, #1a82f7); " + "} ";

	public static String css3 = " /* fallback font - size: 4.5MB */ " + " @font-face { "
			+ " font-family: DroidSans; " + " src: url(DroidSansFallback.ttf);  "
			+ " /* no range specified, defaults to entire range */  " + " }  " + "   "
			+ " /* Japanese glyphs - size: 1.2MB */  " + " @font-face {  "
			+ "   font-family: DroidSans;  " + "   src: url(DroidSansJapanese.ttf);  "
			+ "   unicode-range: U+3000-9FFF  " + " }  " + "  " //U+ff??
			+ " /* Latin, Greek, Cyrillic along with some  "
			+ "   punctuation and symbols - size: 190KB */  " + " @font-face {  "
			+ "   font-family: DroidSans;  " + "   src: url(DroidSans.ttf);  "
			+ "   unicode-range: U+000-5FF, U+1e00-1fff, U+2000-2300;  " + " }"
			+ " *:lang(ja-jp) { font: 900 14pt/16pt \"Heisei Mincho W9\", serif; } "
			+ " *:lang(zh-tw) { font: 800 14pt/16.5pt \"Li Sung\", serif; } ";
	
	public static String css4 = "E {" +
	"background-image: url(flower.png), url(ball.png), url(grass1.png);"
	+"background-position: center center, 20% 80%, top left;"
	+"background-origin: border-box, content-box, border-box;"
	+"background-repeat: no-repeat, no-repeat, no-repeat; }";
	
	public static String css5 = 
			 "body"
					+ "{ "
					+ "	font-family: \"Chaparral Pro\", serif;"
					+ "	line-height: 1.5;"
					+ "}"
					+ ""
					+ "p"
					+ "{ "
					+ "	margin: 0px;"
					+ "	text-align: justify;"
					+ "}"
					+ ""
					+ ".cp"
					+ "{"
					+ "	text-indent: 1.5em;"
					+ "}"
					+ ""
					+ ".pull-quote-cont"
					+ "{"
					+ "	float: right;"
					+ "	clear: right;"
					+ "	max-width: 28em;"
					+ "	line-height: 1.25;"
					+ "	margin-bottom: 1em;"
					+ "	background-color: #FFBBBB;"
					+ "}"
					+ ""
					+ ".pull-quote"
					+ "{"
					+ "	font-family: \"Menlo\", sans-serif;"
					+ "	font-size: 1.5em;"
					+ "	font-variant: small-caps;"
					+ "	text-align: right;"
					+ "}"
					+ ""
					+ ".sidebar-cont"
					+ "{"
					+ "	float: right;"
					+ "	clear: right;"
					+ "	width: 30em;"
					+ "}"
					+ ""
					+ ".sidebar-cont img"
					+ "{"
					+ "	width: 100%;"
					+ "}"
					+ ""
					+ ".header"
					+ "{"
					+ "	background: black;"
					+ "	color: white;"
					+ "	padding: 1em;"
					+ "	font-family: \"Menlo\", sans-serif;"
					+ "}"
					+ ""
					+ "@-epubx-page-template"
					+ "{"
					+ ""
					+ ".header"
					+ "{"
					+ "	-epubx-flow-options: static;"
					+ "	-epubx-flow-into: top;"
					+ "}"
					+ ""
					+ ".pull-quote-cont"
					+ "{"
					+ "	float: none;"
					+ "	clear: none;"
					+ "	width: 29em;"
					+ "	padding: 1em 3em 1em 2em;"
					+ "	-epubx-flow-into: pull-quote;"
					+ "	-epubx-flow-options: last exclusive;"
					+ "	-epubx-flow-linger: 2;"
					+ "}"
					+ ""
					+ ".sidebar-cont"
					+ "{ "
					+ "	float: none;"
					+ "	clear: none;"
					+ "	-epubx-flow-into: sidebar;"
					+ "}"
					+ ""
					+ ".pict"
					+ "{"
					+ "	-epubx-flow-into: pict;"
					+ "	display: block;"
					+ "	margin: 15px;"
					+ "}"
					+ ""
					+ ".kill-pull-quote"
					+ "{"
					+ "	-epubx-flow-into: pull-quote;"
					+ "	-epubx-flow-options: exclusive;"
					+ "	-epubx-flow-priority: 2;"
					+ "}"
					+ ""
					+ "@media all and (min-width: 60em) and (min-height: 50em)"
					+ "{"
					+ ""
					+ "@-epubx-page-master sidebar-width-pull-quote"
					+ "{	"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: body;"
					+ "		overflow: visible;"
					+ "		margin-left: 3em;"
					+ "		margin-right: 3em;"
					+ "		margin-bottom: 0.5em;"
					+ "		top: 4.5em;"
					+ "		column-width: 20em;"
					+ "		column-gap: 1.3em;"
					+ "	}	"
					+ "	"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: pull-quote;"
					+ "		-epubx-min-page-width: 35em;"
					+ "		top: 50%;"
					+ "		margin: 2em 1em 0.5em 0px;"
					+ "		width: 33em;"
					+ "		left: 0px;"
					+ "	}"
					+ "	"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: sidebar;"
					+ "		-epubx-required: true;"
					+ "		-epubx-min-page-width: 40em;"
					+ "		top: 3.5em;"
					+ "		margin: 0px 0px 0.5em 1em;"
					+ "		width: 30em;"
					+ "		right: 0px;"
					+ "	}"
					+ ""
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: top;"
					+ "		background: black;"
					+ "		height: 3.5em;"
					+ "		z-index: 1;"
					+ "	}"
					+ "	"
					+ "	"
					+ "}"
					+ ""
					+ "}"
					+ ""
					+ "@-epubx-region .black"
					+ "{"
					+ "	p { color: white; }"
					+ "}"
					+ ""
					+ ""
					+ "@-epubx-page-master yin-yang"
					+ "{"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: body;"
					+ "		padding-right: 3em;"
					+ "		padding-left: 1em;"
					+ "		left: 50%;"
					+ "		padding-top: 1em;"
					+ "		padding-bottom: 1em;"
					+ "		column-width: 20em;"
					+ "		column-gap: 1.3em;"
					+ "	}"
					+ "	"
					+ "	@-epubx-partition class(black)"
					+ "	{"
					+ "		-epubx-flow-from: body;"
					+ "		padding-left: 3em;"
					+ "		padding-right: 1em;"
					+ "		padding-top: 1em;"
					+ "		padding-bottom: 1em;"
					+ "		right: 50%;"
					+ "		background: black;"
					+ "		column-width: 20em;"
					+ "		column-gap: 1.3em;"
					+ "	}"
					+ "		"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-required: true;"
					+ "		-epubx-flow-from: pict;"
					+ "		-epubx-shape-outside: polygon(50%,0%, 34.5%,2.4%, 20.6%,9.5%, 9.5%,20.6%, 2.4%,34.5%, 0%,50%, 2.4%,65.5%, 9.5%,79.4%, 20.6%,90.5%, 34.5%,97.6%, 50%,100%, 65.5%,97.6%, 79.4%,90.5%, 90.5%,79.4%, 97.6%,65.5%, 100%,50%, 97.6%,34.5%, 90.5%,20.6%, 79.4%,9.5%, 65.5%,2.4%); "
					+ "		top: -epubx-expr(50% - 100px); /* 50% - 100px */"
					+ "		left: -epubx-expr(f oo);		/* 50% - 100px */		"
					+ "		width: 200px;"
					+ "		height: 200px;"
					+ "		z-index: 1;"
					+ "	}"
					+ "}"
					+ ""
					+ "@-epubx-page-master sidebar-only"
					+ "{	"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: body;"
					+ "		overflow: visible;"
					+ "		margin-left: 3em;"
					+ "		margin-right: 3em;"
					+ "		margin-bottom: 0.5em;"
					+ "		top: 4.5em;"
					+ "		column-width: 20em;"
					+ "		column-gap: 1.3em;"
					+ "	}	"
					+ "	"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: sidebar;"
					+ "		-epubx-required: true;"
					+ "		-epubx-min-page-width: 40em;"
					+ "		top: 3.5em;"
					+ "		margin: 0px 0px 0.5em 1em;"
					+ "		width: 30em;"
					+ "		right: 0px;"
					+ "	}"
					+ ""
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: top;"
					+ "		background: black;"
					+ "		height: 3.5em;"
					+ "		z-index: 1;"
					+ "	}"
					+ "}"
					+ ""
					+ "@-epubx-page-master pull-quote-only"
					+ "{	"
					+ "	"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: body;"
					+ "		overflow: visible;"
					+ "		margin-left: 3em;"
					+ "		margin-right: 3em;"
					+ "		margin-bottom: 0.5em;"
					+ "		top: 4.5em;"
					+ "		column-width: 20em;"
					+ "		column-gap: 1.3em;"
					+ "	}"
					+ "		"
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: pull-quote;"
					+ "		-epubx-min-page-width: 35em;"
					+ "		top: 40%;"
					+ "		margin: 2em 0px 0.5em 1em;"
					+ "		width: 33em;"
					+ "		right: 0px;"
					+ "	}"
					+ ""
					+ "	@-epubx-partition"
					+ "	{"
					+ "		-epubx-flow-from: top;"
					+ "		background: black;"
					+ "		height: 3.5em;"
					+ "	}"
					+ "}"
					+ "}";
	
	public static String css6 = 
			"p { color: red; font-size: 12pt } \n "
			+ "a:link { color: blue } \n "
			+" p::first-line { color: blue } \n "
			+" html|*:not(:link):not(:visited) { float: right; } \n"
			+" *|*:not(*) { float: right; } \n"
			+" img:nth-of-type(2n+1) { float: right; } \n"
			+" body>h2:nth-of-type(n+2):nth-last-of-type(n+2) { float: right; } \n"
			+" body>h2:not(:first-of-type):not(:last-of-type) { float: right; } \n";
	
	public static String css7 = "@counter-style disc { system: cyclic; symbols: \2022; /* • */ suffix: ''; } \n " +
			"@counter-style circle { system: cyclic; symbols: \25E6; /* ◦ */ suffix: ''; } \n " +
			"@counter-style square { system: cyclic; symbols: \25FE; /* ◾ */ suffix: ''; }"; 
	
}
