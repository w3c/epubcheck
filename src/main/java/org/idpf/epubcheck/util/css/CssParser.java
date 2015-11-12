/*
 * Copyright (c) 2012 International Digital Publishing Forum
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.idpf.epubcheck.util.css;

import static com.google.common.base.Preconditions.*;
import static org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode.*;
import static org.idpf.epubcheck.util.css.CssToken.Matchers.*;
import static org.idpf.epubcheck.util.css.CssTokenList.Filters.FILTER_S_CMNT;
import static org.idpf.epubcheck.util.css.CssTokenList.Filters.FILTER_S_CMNT_CDO_CDC;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssGrammar.CssAtRule;
import org.idpf.epubcheck.util.css.CssGrammar.CssConstruct;
import org.idpf.epubcheck.util.css.CssGrammar.CssConstructFactory;
import org.idpf.epubcheck.util.css.CssGrammar.CssDeclaration;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelector;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelectorCombinator;
import org.idpf.epubcheck.util.css.CssGrammar.CssSelectorConstructFactory;
import org.idpf.epubcheck.util.css.CssGrammar.CssSimpleSelectorSequence;
import org.idpf.epubcheck.util.css.CssToken.CssTokenConsumer;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;
import org.idpf.epubcheck.util.css.CssTokenList.PrematureEOFException;

import com.adobe.epubcheck.util.Messages;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * A CSS parser.
 *
 * @author mgylling
 */
public final class CssParser
{
  private final boolean debug = false;
  private final Messages messages;
  private final CssSelectorConstructFactory cssSelectorFactory;

  /**
   * Builds a new CSS parser reporting errors in the default locale.
   * <p>
   * The localized constructor {@link #CssParser(Locale)} should be preferred over
   * this one, except for tests.
   * </p>
   */
  public CssParser()
  {
    this(Locale.getDefault());
  }

  /**
   * Builds a new CSS parser that will report parsing errors in the given locale.
   * 
   * @param locale
   *          the locale used in the parsing errors.
   */
  public CssParser(Locale locale)
  {
    this.messages = Messages.getInstance(locale, CssParser.class);
    this.cssSelectorFactory = new CssSelectorConstructFactory(locale);
  }

  /*
    * TODOs
    * - pseudo-elements are restricted to one per selector and
    *   occur only in the last simple_selector_sequence (testParserSelectorsFunctionalPseudoInvalid001).
    * - baduri 1 and 2 and 3 #buildURI
    * - check uc range in tokenbuilder#append
    * - cssreader#forward should be fully escape aware
    */

  /**
   * Parse a CSS document.
   */
  public void parse(final CssSource source, final CssErrorHandler err, final CssContentHandler doc) throws
      IOException,
      CssException
  {
    parse(source.newReader(), source.getSystemID(), err, doc);
  }

  /**
   * Parse a CSS document.
   */
  public void parse(final Reader reader, final String systemID, final CssErrorHandler err, final CssContentHandler doc)
      throws
      IOException,
      CssException
  {

    CssTokenIterator iter = scan(reader, systemID, err);

    doc.startDocument();

    while (iter.hasNext(FILTER_S_CMNT_CDO_CDC))
    {
      CssToken tk = iter.next(FILTER_S_CMNT_CDO_CDC);
      try
      {

        if (tk.type == CssToken.Type.ATKEYWORD)
        {
          handleAtRule(tk, iter, doc, err);
          if (debug)
          {
            checkArgument(MATCH_SEMI_CLOSEBRACE.apply(iter.last));
          }
        }
        else
        {
          handleRuleSet(tk, iter, doc, err);
          if (debug)
          {
            checkArgument(MATCH_CLOSEBRACE.apply(iter.last));
          }
        }

      }
      catch (PrematureEOFException te)
      {
        // The subroutines report premature EOF to ErrHandler
        // on occurrence; if the listener rethrows it will
        // be a CssException so we don't catch it here.
        break;
      }
    }

    doc.endDocument();
  }

  /**
   * Parse a CSS style attribute.
   */
  public void parseStyleAttribute(final Reader reader, String systemID, final CssErrorHandler err, final CssContentHandler doc) throws
      IOException,
      CssException
  {
    CssTokenIterator iter = scan(reader, systemID, err);
    doc.startDocument();
    while (iter.hasNext())
    {
      CssToken tk = iter.next();
			
			if(MATCH_SEMI.apply(tk)) 
			{
				continue; //starting with ';' is allowed, Issue 238
			}
      try
      {
			
        CssDeclaration decl = handleDeclaration(tk, iter, doc, err, true);
        if (decl != null)
        {
          doc.declaration(decl);
        }
        else
        {
          // #handleDeclaration has issued errors
          return;
        }
      }
      catch (PrematureEOFException te)
      {
        // The subroutines report premature EOF to ErrHandler
        // on occurrence; if the listener rethrows it will
        // be a CssException so we don't catch it here.
        break;
      }
    }
    doc.endDocument();
  }

  private CssTokenIterator scan(Reader reader, String systemID, CssErrorHandler err) throws
      IOException,
      CssException
  {

    final CssTokenList tokens = new CssTokenList();

    new CssScanner(reader, systemID, err, new CssTokenConsumer()
    {
      public void add(final CssToken token)
      {
        tokens.add(token);
      }
    }, messages.getLocale()).scan();

    return tokens.iterator(FILTER_S_CMNT); // default filter
  }

  /**
   * With the start token expected to be the first token of a selector group,
   * create and issue the group, then invoke handleDeclarationBlock. At exit
   * the last token returned from the iterator is expected to be '}'.
   */
  private void handleRuleSet(CssToken start, final CssTokenIterator iter, final CssContentHandler doc,
      CssErrorHandler err) throws
      CssException
  {

    char errChar = '{';
    try
    {
      List<CssSelector> selectors = handleSelectors(start, iter, err);
      errChar = '}';
      if (selectors == null)
      {
        // handleSelectors() has issued errors, we forward
        iter.next(MATCH_CLOSEBRACE);
        return;
      }
      if (debug)
      {
        checkState(iter.last.getChar() == '{');
        checkState(!selectors.isEmpty());
      }

      doc.selectors(selectors);

      handleDeclarationBlock(iter.next(), iter, doc, err);

      doc.endSelectors(selectors);

    }
    catch (NoSuchElementException nse)
    {
      err.error(new CssGrammarException(GRAMMAR_PREMATURE_EOF, iter.last.location,
          messages.getLocale(), "'" + errChar + "'"));
      throw new PrematureEOFException();
    }

    if (debug)
    {
      checkState(iter.last.getChar() == '}');
    }
  }

  /**
   * With start token being the first non-ignorable token inside the declaration
   * block, iterate issuing CssDeclaration objects until the block ends.
   */
  private void handleDeclarationBlock(CssToken start, CssTokenIterator iter,
      final CssContentHandler doc, CssErrorHandler err) throws
      CssException
  {

    while (true)
    {
      if (MATCH_CLOSEBRACE.apply(start))
      {
        return;
      }
      CssDeclaration decl = handleDeclaration(start, iter, doc, err, false);
      try
      {
        if (decl != null)
        {
          doc.declaration(decl);
          if (debug)
          {
            checkState(MATCH_SEMI_CLOSEBRACE.apply(iter.last));
          }

          // continue or return: we may be at "; next decl" or "}" or ";}"
          if (MATCH_CLOSEBRACE.apply(iter.last))
          {
            return;
          }
          else if (MATCH_SEMI.apply(iter.last) && MATCH_CLOSEBRACE.apply(iter.peek()))
          {
            iter.next();
            return;
          }
          else
          {
            if (debug)
            {
              checkState(MATCH_SEMI.apply(iter.last));
            }
            // we have ';', expect another decl
            start = iter.next(); // first token after ';'
          }

        }
        else
        {
          // #handleDeclaration returned null to signal error
          // #handleDeclaration has issued errors, we forward
          start = iter.next(MATCH_SEMI_CLOSEBRACE);
          if (MATCH_SEMI.apply(start))
          {
            start = iter.next();
          }
        }
      }
      catch (NoSuchElementException nse)
      {
        err.error(new CssGrammarException(GRAMMAR_PREMATURE_EOF, iter.last.location,
            messages.getLocale(), "';' " + messages.get("or") + " '}'"));
        throw new PrematureEOFException();
      }
    }
  }

  /**
   * With start expected to be an IDENT token representing the property name,
   * build the declaration and return after hitting ';' or '}'. On error,
   * issue to errhandler, return null, caller forwards.
   */
  private CssDeclaration handleDeclaration(CssToken name, CssTokenIterator iter,
      CssContentHandler doc, CssErrorHandler err, boolean isStyleAttribute) throws
      CssException
  {

    if (name.type != CssToken.Type.IDENT)
    {
      err.error(new CssGrammarException(GRAMMAR_EXPECTING_TOKEN, name.location,
          messages.getLocale(), name.getChars(), messages.get("a_property_name")));
      return null;
    }

    CssDeclaration declaration = new CssDeclaration(name.getChars(), name.location);

    try
    {
      if (!MATCH_COLON.apply(iter.next()))
      {
        err.error(new CssGrammarException(GRAMMAR_EXPECTING_TOKEN, name.location,
            messages.getLocale(), iter.last.getChars(), ":"));
        return null;
      }
    }
    catch (NoSuchElementException nse)
    {
      err.error(new CssGrammarException(GRAMMAR_PREMATURE_EOF, iter.last.location,
          messages.getLocale(), ":"));
      throw new PrematureEOFException();
    }

    try
    {
      while (true)
      {
        CssToken value = iter.next();
        if (MATCH_SEMI_CLOSEBRACE.apply(value))
        {
          if (declaration.components.size() < 1)
          {
            err.error(new CssGrammarException(GRAMMAR_EXPECTING_TOKEN, iter.last.location,
                messages.getLocale(), value.getChar(), messages.get("a_property_value")));
            return null;
          }
          else
          {
            return declaration;
          }
        }
        else
        {
          if (!handlePropertyValue(declaration, value, iter, isStyleAttribute))
          {
            err.error(new CssGrammarException(GRAMMAR_UNEXPECTED_TOKEN, iter.last.location,
                messages.getLocale(), iter.last.getChars()));
            return null;
          }
          else
          {
            if (isStyleAttribute && !iter.hasNext())
            {
              return declaration;
            }
          }
        }
      }
    }
    catch (NoSuchElementException nse)
    {
      err.error(new CssGrammarException(GRAMMAR_PREMATURE_EOF, iter.last.location,
          messages.getLocale(), "';' " + messages.get("or") + " '}'"));
      throw new PrematureEOFException();
    }

  }

  /**
   * Append property value components to declaration.value, return false if
   * fail with iter.last at the the token which caused the fail.
   */
  private boolean handlePropertyValue(CssDeclaration declaration, CssToken start,
      CssTokenIterator iter, boolean isStyleAttribute)
  {
    // we dont worry about EOF here, throw to caller
    while (true)
    {

      if (start.type == CssToken.Type.IMPORTANT)
      {
        declaration.important = true;
      }
      else
      {
        CssConstruct cc = CssConstructFactory.create(
            start, iter, MATCH_SEMI_CLOSEBRACE,
            ContextRestrictions.PROPERTY_VALUE);
        if (cc == null)
        {
          return false;
        }
        else
        {
          declaration.components.add(cc);
        }
      }

      //if isStyleAttribute, then parse as declaration-list grammar,
      //i.e. no braces
      if ((isStyleAttribute && !iter.hasNext())
          || (MATCH_SEMI.apply(iter.peek()))
          || (!isStyleAttribute && MATCH_CLOSEBRACE.apply(iter.peek())))
      {
        return declaration.components.size() > 0;
      }
      else
      {
        start = iter.next();
      }
    }
  }

  /**
   * With start inparam being the first significant token in a selector, build
   * the selector group (aka comma separated selectors), expected return when
   * iter.last is '{'. On error, issue to errorlistener, and return
   * (caller will forward).
   *
   * @return A syntactically valid CssSelector list, or null if fail.
   * @throws CssException
   */
  private List<CssSelector> handleSelectors(CssToken start, CssTokenIterator iter,
      CssErrorHandler err) throws
      CssException
  {

    List<CssSelector> selectors = Lists.newArrayList();
    boolean end = false;
    while (true)
    { // comma loop
      CssSelector selector = new CssSelector(start.location);
      while (true)
      { //combinator loop
        CssSimpleSelectorSequence seq = cssSelectorFactory.createSimpleSelectorSequence(start, iter,
            err);
        if (seq == null)
        {
          //errors already issued
          return null;
        }
        selector.components.add(seq);
        int idx = iter.index();
        start = iter.next();
        if (MATCH_OPENBRACE.apply(start))
        {
          end = true;
          break;
        }
        if (MATCH_COMMA.apply(start))
        {
          break;
        }

        CssSelectorCombinator comb = cssSelectorFactory.createCombinator(start, iter, err);
        if (comb != null)
        {
          selector.components.add(comb);
          start = iter.next();
        }
        else if (iter.list.get(idx + 1).type == CssToken.Type.S)
        {
          selector.components.add(new CssSelectorCombinator(' ', start.location));
        }
        else
        {
          err.error(new CssGrammarException(GRAMMAR_UNEXPECTED_TOKEN, iter.last.location,
              messages.getLocale(), iter.last.chars));
          return null;
        }
      } //combinator loop
      selectors.add(selector);
      if (end)
      {
        break;
      }
      if (debug)
      {
        checkState(MATCH_COMMA.apply(start));
      }
      start = iter.next();
    } // comma loop
    return selectors;
  }

  /**
   * With start token required to be an ATKEYWORD, collect at-rule parameters if
   * any, and if the at-rule has a block, invoke those handlers.
   */
  private void handleAtRule(CssToken start, CssTokenIterator iter, CssContentHandler doc,
      CssErrorHandler err) throws
      CssException
  {

    if (debug)
    {
      checkArgument(start.type == CssToken.Type.ATKEYWORD);
      checkArgument(iter.index() == iter.list.indexOf(start));
      checkArgument(iter.filter() == FILTER_S_CMNT);
    }

    CssAtRule atRule = new CssAtRule(start.getChars(), start.location);

    CssToken tk;
    try
    {
      while (true)
      {
        tk = iter.next();
        if (MATCH_SEMI_OPENBRACE.apply(tk))
        {
          // ';' or '{', expected end
          atRule.hasBlock = tk.getChar() == '{';
          break;
        }
        else
        {
          CssConstruct param = handleAtRuleParam(tk, iter, doc, err);
          if (param == null)
          {
            // issue error, forward, then return
            err.error(new CssGrammarException(GRAMMAR_UNEXPECTED_TOKEN, iter.last.location,
                messages.getLocale(), iter.last.chars));
            //skip to atrule closebrace, ignoring any inner blocks
            int stack = 0;
            while (true)
            {
              CssToken tok = iter.next();
              if (MATCH_SEMI.apply(tok) && stack == 0)
              {
                return; //a non-block at rule
              }
              else if (MATCH_OPENBRACE.apply(tok))
              {
                stack++;
              }
              else if (MATCH_CLOSEBRACE.apply(tok))
              {
                if (stack == 1)
                {
                  break;
                }
                stack--;
              }
            }
            return;
          }
          else
          {
            atRule.components.add(param);
          }
        }
      }
    }
    catch (NoSuchElementException nse)
    {
      // UAs required to close any open constructs on premature EOF
      doc.startAtRule(atRule);
      err.error(new CssGrammarException(GRAMMAR_PREMATURE_EOF, iter.last.location,
          messages.getLocale(), "';' " + messages.get("or") + " '{'"));
      doc.endAtRule(atRule.getName().get());
      throw new PrematureEOFException();
    }

    if (debug)
    {
      checkArgument(MATCH_SEMI_OPENBRACE.apply(iter.last));
      checkArgument(iter.filter() == FILTER_S_CMNT);
    }

    // ending up here only on expected end
    doc.startAtRule(atRule);
    if (atRule.hasBlock)
    {
      try
      {
        if (hasRuleSet(atRule, iter))
        {
          while (!MATCH_CLOSEBRACE.apply(iter.next()))
          {
            if (iter.last.type == CssToken.Type.ATKEYWORD)
            {
              handleAtRule(iter.last, iter, doc, err);
            }
            else
            {
              handleRuleSet(iter.last, iter, doc, err);
            }
          }
        }
        else
        {
          handleDeclarationBlock(iter.next(), iter, doc, err);
        }
      }
      catch (NoSuchElementException nse)
      {
        err.error(new CssGrammarException(GRAMMAR_PREMATURE_EOF, iter.last.location,
            messages.getLocale(), "'}'"));
        doc.endAtRule(atRule.name.get());
        throw new PrematureEOFException();
      }
    }
    doc.endAtRule(atRule.name.get());
  }

  /**
   * With inparam token being the first token of an atrule param, create the
   * construct and return it.
   *
   * @return A CssConstruct, or null if fail.
   */
  private CssConstruct handleAtRuleParam(CssToken start, CssTokenIterator iter,
      CssContentHandler doc, CssErrorHandler err)
  {
    return CssConstructFactory.create(start, iter, MATCH_SEMI_OPENBRACE,
        ContextRestrictions.ATRULE_PARAM);
  }

  /**
   * With iter.last at '{', discover the at-rule type. The
   * contents is a ruleset if '{' comes before ';' or '}'.
   */
  private boolean hasRuleSet(CssAtRule atRule, CssTokenIterator iter)
  {
    int debugIndex;
    if (debug)
    {
      checkArgument(iter.last.getChar() == '{');
      debugIndex = iter.index();
    }
    List<CssToken> list = iter.list;
    for (int i = iter.index() + 1; i < list.size(); i++)
    {
      CssToken tk = list.get(i);
      if (MATCH_OPENBRACE.apply(tk))
      {
        return true;
      }
      else if (MATCH_SEMI_CLOSEBRACE.apply(tk))
      {
        return false;
      }
    }
    if (debug)
    {
      checkState(iter.last.getChar() == '{');
      checkState(iter.index() == debugIndex);
    }
    return false;
  }

  static final class ContextRestrictions
  {

    /**
     * A context restriction for the contents of a function.
     */
    static final Predicate<CssConstruct> FUNCTION = new Predicate<CssConstruct>()
    {
      public boolean apply(CssConstruct cc)
      {
        checkNotNull(cc);
        return cc.type != CssConstruct.Type.ATRULE; //TODO;
      }
    };

    /**
     * A context restriction for at-rule parameters.
     */
    static final Predicate<CssConstruct> ATRULE_PARAM = new Predicate<CssConstruct>()
    {
      public boolean apply(CssConstruct cc)
      {
        checkNotNull(cc);
        return true; // atrule param space allows SYMBOL (aka DELIM) too
      }
    };

    /**
     * A context restriction for property values.
     */
    static final Predicate<CssConstruct> PROPERTY_VALUE = new Predicate<CssConstruct>()
    {
      public boolean apply(CssConstruct cc)
      {
        checkNotNull(cc);
        return cc.type != CssConstruct.Type.ATRULE; //TODO
      }
    };

    /**
     * A context restriction for the value segment of an attribute selector ([name+match+value]).
     */
    static final Predicate<CssConstruct> ATTRIBUTE_SELECTOR_VALUE = new Predicate<CssConstruct>()
    {
      public boolean apply(final CssConstruct cc)
      {
        checkNotNull(cc);
        return cc.type == CssConstruct.Type.KEYWORD || cc.type == CssConstruct.Type.STRING;
      }
    };

    /**
     * A context restriction for elements inside a functional pseudo.
     */
    static final Predicate<CssConstruct> PSEUDO_FUNCTIONAL = new Predicate<CssConstruct>()
    {
      public boolean apply(CssConstruct cc)
      {
        checkNotNull(cc);
        return cc.type == CssConstruct.Type.KEYWORD
            || cc.type == CssConstruct.Type.STRING
            || cc.type == CssConstruct.Type.QUANTITY
            || (cc.type == CssConstruct.Type.SYMBOL && cc.toCssString().equals("+"))
            || (cc.type == CssConstruct.Type.SYMBOL && cc.toCssString().equals("-"))
            ;
      }
    };

    /**
     * A context restriction for elements inside a negation pseudo.
     */
    static final Predicate<CssConstruct> PSEUDO_NEGATION = new Predicate<CssConstruct>()
    {
      public boolean apply(final CssConstruct cc)
      {
        checkNotNull(cc);
        return cc.type == CssConstruct.Type.TYPE_SELECTOR
            || cc.type == CssConstruct.Type.HASHNAME
            || cc.type == CssConstruct.Type.CLASSNAME
            || (cc.type == CssConstruct.Type.ATTRIBUTE_SELECTOR)
            || (cc.type == CssConstruct.Type.PSEUDO)
            ;
      }
    };
  }

}
