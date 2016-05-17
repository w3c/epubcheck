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

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode;
import org.idpf.epubcheck.util.css.CssExceptions.CssException;
import org.idpf.epubcheck.util.css.CssExceptions.CssGrammarException;
import org.idpf.epubcheck.util.css.CssParser.ContextRestrictions;
import org.idpf.epubcheck.util.css.CssTokenList.CssTokenIterator;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.idpf.epubcheck.util.css.CssExceptions.CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN;
import static org.idpf.epubcheck.util.css.CssToken.Matchers.*;
import static org.idpf.epubcheck.util.css.CssTokenList.Filters.FILTER_NONE;

/**
 * CSS grammar components.
 *
 * @author mgylling
 */
public class CssGrammar
{

  /**
   * Abstract base for all CssConstructs.
   */
  public static abstract class CssConstruct
  {
    final CssLocation location;
    final Type type;

    public CssConstruct(final Type type, final CssLocation location)
    {
      this.location = checkNotNull(location);
      this.type = checkNotNull(type);
    }

    public final CssLocation getLocation()
    {
      return location;
    }

    public final Type getType()
    {
      return type;
    }

    public abstract String toCssString();

    public enum Type
    {
      //atomics
      STRING,
      KEYWORD,
      COMBINATOR,         //space, plus, gt, tilde
      ATTRIBUTE_MATCH,       // "~=", "|=", "^=","$=" "*="
      HASHNAME,          //#ident
      CLASSNAME,          //.ident
      QUANTITY,
      URANGE,
      URI,
      SYMBOL,            //a single char, eg operators
      TYPE_SELECTOR,        //ns|E, *|E, |E, E, *
      PSEUDO,            //element and class

      //composed
      ATRULE,
      FUNCTION,
      DECLARATION,
      SELECTOR,
      SIMPLE_SELECTOR_SEQ,
      ATTRIBUTE_SELECTOR,      //[...]
      SCOPEDGROUP,        //(...) and [...], the latter when not an attr selector segment


    }
  }

  /*********************************************
   *
   *    atomics
   *
   ********************************************/

  /**
   * A CssConstruct that is composed by a single token.
   */
  static abstract class CssAtomicConstruct extends CssConstruct
  {
    final String value;

    public CssAtomicConstruct(final Type type, final String value, final CssLocation location)
    {
      super(type, location);
      this.value = checkNotNull(value);
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this).addValue(value).toString();
    }

    @Override
    public String toCssString()
    {
      return value;
    }

  }

  /**
   * A CSS string. The returned value does not include start and end quotes.
   */
  public final static class CssString extends CssAtomicConstruct
  {
    public CssString(final String value, final CssLocation location)
    {
      super(Type.STRING, value, location);
    }

    @Override
    public final String toCssString()
    {
      return "'" + value + "'";
    }
  }

  /**
   * A CSS keyword.
   */
  public final static class CssKeyword extends CssAtomicConstruct
  {
    public CssKeyword(final String value, final CssLocation location)
    {
      super(Type.KEYWORD, value, location);
    }
  }

  /**
   * A CSS hash name
   */
  public final static class CssHashName extends CssAtomicConstruct
  {
    public CssHashName(final String value, final CssLocation location)
    {
      super(Type.HASHNAME, value, location);
    }
  }

  /**
   * A CSS class name
   */
  public final static class CssClassName extends CssAtomicConstruct
  {
    public CssClassName(final String value, final CssLocation location)
    {
      super(Type.CLASSNAME, value, location);
    }
  }

  /**
   * A CSS unicode range
   */
  public final static class CssUnicodeRange extends CssAtomicConstruct
  {
    public CssUnicodeRange(final String value, final CssLocation location)
    {
      super(Type.URANGE, value, location);
    }
  }

  /**
   * A CSS URI.
   */
  public final static class CssURI extends CssAtomicConstruct
  {
    public CssURI(final String value, final CssLocation location)
    {
      super(Type.URI, value, location);
    }

    /**
     * The URI string itself (leading/trailing whitespace+quotes stripped, url function removed)
     */
    public String toUriString()
    {
      StringBuilder builder = new StringBuilder();
      boolean inStart = false;

      for (int i = 0; i < value.length(); i++)
      {
        if (i > 3 && i < value.length() - 1)
        {
          char ch = value.charAt(i);
          if (CssScanner.QUOTES.matches(ch)
              || CssScanner.WHITESPACE.matches(ch))
          {
            if (inStart)
            {
              builder.append(ch);
            }
          }
          else
          {
            inStart = true;
            builder.append(ch);
          }
        }
      }

      while (true)
      {
        if (builder.length() == 0)
        {
          break;
        }
        int index = builder.length() - 1;
        char ch = builder.charAt(index);
        if (CssScanner.QUOTES.matches(ch)
            || CssScanner.WHITESPACE.matches(ch))
        {
          builder.deleteCharAt(index);
        }
        else
        {
          break;
        }
      }

      return builder.toString();
    }
  }

  /**
   * A CSS single-character symbol (e.g. operator) that is not
   * in the range of IDENT/KEYWORD and not '{', '}' or ';'. The value
   * is interned.
   */
  public final static class CssSymbol extends CssAtomicConstruct
  {
    public CssSymbol(final String value, final CssLocation location)
    {
      super(Type.SYMBOL, value.intern(), location);
      checkArgument(value.length() == 1);
    }
  }

  /**
   * A CSS selector combinator (space, plus, greater or tilde). The value is interned.
   */
  public final static class CssSelectorCombinator extends CssAtomicConstruct
  {
    final CssSelectorCombinator.Type subType;

    public CssSelectorCombinator(final char value, final CssLocation location)
    {
      super(CssConstruct.Type.COMBINATOR, String.valueOf(value).intern(), location);

      switch (value)
      {
        case ' ':
          this.subType = Type.DESCENDANT;
          break;
        case '>':
          this.subType = Type.CHILD;
          break;
        case '+':
          this.subType = Type.ADJACENT_SIBLING;
          break;
        case '~':
          this.subType = Type.GENERAL_SIBLING;
          break;
        default:
          throw new IllegalStateException();
      }
    }

    public final CssSelectorCombinator.Type getCombinatorType()
    {
      return subType;
    }

    public enum Type
    {
      DESCENDANT,     //S
      CHILD,        //>
      ADJACENT_SIBLING,  //+
      GENERAL_SIBLING  //~
    }
  }

  /**
   * An attribute match selector ('=', '~=', '|=', '^=', '$=', '*=')
   */
  public final static class CssAttributeMatchSelector extends CssAtomicConstruct
  {
    final CssAttributeMatchSelector.Type subType;

    public CssAttributeMatchSelector(final String value,
        final CssAttributeMatchSelector.Type type, final CssLocation location)
    {
      super(CssConstruct.Type.ATTRIBUTE_MATCH, value, location);
      this.subType = type;
    }

    public final CssAttributeMatchSelector.Type getAttributeMatchType()
    {
      return subType;
    }

    public enum Type
    {
      EQUALS,       //"="
      INCLUDES,       //"~="
      DASHMATCH,       //"|="
      PREFIXMATCH,     //"^="
      SUFFIXMATCH,     //"$="
      SUBSTRINGMATCH   //"*="
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this).addValue(subType.name()).addValue(value).toString();
    }
  }

  /**
   * A type or universal ('*') selector, possibly with namespace bindings.
   */
  public static class CssTypeSelector extends CssAtomicConstruct
  {
    public CssTypeSelector(final String value, final CssLocation location)
    {
      super(Type.TYPE_SELECTOR, value, location);
    }
  }

  /**
   * A CSS quantity.
   */
  public final static class CssQuantity extends CssAtomicConstruct
  {
    final CssQuantity.Unit subType;

    public CssQuantity(final String value, final CssQuantity.Unit subtype, final CssLocation location)
    {
      super(CssConstruct.Type.QUANTITY, value, location);
      this.subType = checkNotNull(subtype);
    }

    public final CssQuantity.Unit getUnit()
    {
      return subType;
    }

    public enum Unit
    {
      DIMEN,
      PERCENTAGE,
      LENGTH,
      EMS,
      EXS,
      ANGLE,
      TIME,
      FREQ,
      RESOLUTION,
      NUMBER,
      INTEGER,
      REMS
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this).addValue(subType.name()).addValue(value).toString();
    }
  }


  /*********************************************
   *
   *    composed
   *
   ********************************************/

  /**
   * A CssConstruct that is composed a list of atomic and/or composed CssConstructs,
   * and optionally a name.
   */
  static abstract class CssComposedConstruct extends CssConstruct
  {
    final List<CssConstruct> components;
    final Optional<String> name;

    public CssComposedConstruct(final Type type, final String name, final CssLocation location)
    {
      super(type, location);
      this.components = Lists.newArrayList();
      this.name = name != null ? Optional.of(name) : absent;
    }

    public CssComposedConstruct(final Type type, final CssLocation location)
    {
      this(type, null, location);
    }

    /**
     * Get the components. The list may be empty but is never null.
     */
    public List<CssConstruct> getComponents()
    {
      return components;
    }

    public Optional<String> getName()
    {
      return name;
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this)
          .addValue(type)
          .addValue(name.isPresent() ? name.get() : "")
          .addValue(components.isEmpty() ? "" : Joiner.on(" ").join(components))
          .toString();
    }

    final Optional<String> absent = Optional.absent();
  }

  /**
   * An attribute selector ([name] or [name, match, ident/string] )
   */
  public final static class CssAttributeSelector extends CssComposedConstruct
  {

    public CssAttributeSelector(final CssLocation location)
    {
      super(Type.ATTRIBUTE_SELECTOR, location);
    }

    @Override
    public String toCssString()
    {
      StringBuilder sb = new StringBuilder().append('[');
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString());
      }
      return sb.append(']').toString();
    }

  }

  /**
   * A CSS function. The function name is interned, and ASCII characters in the name
   * are guaranteed to be lower case.
   */
  public final static class CssFunction extends CssComposedConstruct
  {
    public CssFunction(final String name, final CssLocation location)
    {
      super(Type.FUNCTION, Ascii.toLowerCase(checkNotNull(name)).intern(), location);
    }

    @Override
    public String toCssString()
    {
      StringBuilder sb = new StringBuilder().append(name.get()).append('(');
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString());
      }
      return sb.append(')').toString();
    }

  }

  /**
   * A CSS at-rule. The at-rule name is interned, and ASCII characters in the name
   * are guaranteed to be lowercase.
   */
  public final static class CssAtRule extends CssComposedConstruct
  {
    boolean hasBlock;

    public CssAtRule(final String name, final CssLocation location)
    {
      super(Type.ATRULE, checkNotNull(
          Ascii.toLowerCase(name).intern()), location);
    }

    public final boolean hasBlock()
    {
      return hasBlock;
    }

    @Override
    public String toCssString()
    {
      // Note that semi or braceblock is not included in the return
      StringBuilder sb = new StringBuilder().append(name.get()).append(' ');
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString()).append(' '); //TODO get smarter re space, joint helper function
      }
      return sb.deleteCharAt(sb.length() - 1).toString();
    }

  }

  /**
   * A CSS declaration. The property name is interned, and ASCII characters in the name
   * are guaranteed to be lowercase.
   */
  public final static class CssDeclaration extends CssComposedConstruct
  {
    boolean important = false;

    public CssDeclaration(final String name, final CssLocation location)
    {
      super(Type.DECLARATION, Ascii.toLowerCase(name).intern(), location);
    }

    /**
     * Get the state of the important flag
     */
    public final boolean getImportant()
    {
      return important;
    }

    @Override
    public String toCssString()
    {
      StringBuilder sb = new StringBuilder().append(name.get()).append(" : ");
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString()).append(' '); //TODO get smarter re space, joint helper function
      }
      if (getImportant())
      {
        sb.append("!important ");
      }
      return sb.deleteCharAt(sb.length() - 1).append(" ;").toString();
    }
  }

  /**
   * A scoped group, aka (...) and [...].
   */
  public final static class CssScopedGroup extends CssComposedConstruct
  {
    final CssScopedGroup.Type subType;

    public CssScopedGroup(final CssScopedGroup.Type type, final CssLocation location)
    {
      super(CssConstruct.Type.SCOPEDGROUP, location);
      this.subType = type;
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this)
          .addValue(type)
          .addValue(subType)
          .addValue(Joiner.on(" ").join(components))
          .toString();
    }

    public final CssScopedGroup.Type getGroupType()
    {
      return subType;
    }

    @Override
    public String toCssString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append(subType == Type.PAREN ? '(' : '[');
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString()).append(' '); //TODO get smarter re space, joint helper function
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append(subType == Type.PAREN ? ')' : ']');
      return sb.toString();
    }

    public enum Type
    {
      PAREN,
      BRACKET
    }

  }

  /**
   * A CSS pseudo selector (pseudo-element and pseudo-class).
   */
  public final static class CssPseudoSelector extends CssComposedConstruct
  {
    final CssPseudoSelector.Type subType;
    CssFunction function = null; //may remain null
    String name = null;      //may remain null

    public CssPseudoSelector(final CssPseudoSelector.Type type, final CssLocation location)
    {
      super(CssConstruct.Type.PSEUDO, location);
      this.subType = type;
    }

    /*
       * If this pseudo selector is a functional_pseudo,
       * return the function name, else :(:)ident.
       */
    @Override
    public final Optional<String> getName()
    {
      if (function != null)
      {
        return function.getName();
      }
      return Optional.of(name);
    }

    @Override
    public String toString()
    {
      return Objects.toStringHelper(this)
          .addValue(getSubType().name())
          .addValue(getName().get())
          .addValue(function != null ? Joiner.on(" ").join(function.components) : "")
          .toString();
    }

    /**
     * If this pseudo selector is a functional_pseudo,
     * return it as a function, else return null.
     */
    public final CssFunction getFunction()
    {
      return function;
    }

    public final CssPseudoSelector.Type getSubType()
    {
      return subType;
    }

    @Override
    public String toCssString()
    {
      if (function != null)
      {
        return function.toCssString();
      }
      return name;
    }

    public enum Type
    {
      PSEUDO_ELEMENT,
      PSEUDO_CLASS,
    }

  }

  /**
   * A CSS selector
   */
  public final static class CssSelector extends CssComposedConstruct
  {

    public CssSelector(final CssLocation location)
    {
      super(Type.SELECTOR, location);
    }

    /**
     * Get the list of selector constructs, consisting of at least one
     * CssSimpleSelectorSequence, possibly followed by (CssSelectorCombinator,
     * CssSimpleSelectorSequence)*
     */
    public final List<CssConstruct> getComponents()
    {
      return super.getComponents();
    }

    @Override
    public String toCssString()
    {
      StringBuilder sb = new StringBuilder();
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString());
      }
      return sb.toString();
    }

  }

  /**
   * A CSS simple selector sequence
   * <q>A sequence of simple selectors is a chain of simple selectors that are not
   * separated by a combinator. It always begins with a type selector or a universal
   * selector. No other type selector or universal selector is allowed in the sequence.
   * A simple selector is either a type selector, universal selector, attribute selector,
   * class selector, ID selector, or pseudo-class.</q>
   */
  public final static class CssSimpleSelectorSequence extends CssComposedConstruct
  {

    public CssSimpleSelectorSequence(final CssLocation location)
    {
      super(Type.SIMPLE_SELECTOR_SEQ, location);
    }

    @Override
    public String toCssString()
    {
      StringBuilder sb = new StringBuilder();
      for (CssConstruct cc : components)
      {
        sb.append(cc.toCssString());
      }
      return sb.toString();
    }

  }

  /**
   * ******************************************
   * <p/>
   * helpers
   * <p/>
   * ******************************************
   */

  static final class CssSelectorConstructFactory
  {

    /**
     * Create a simple selector sequence. If creation fails,
     * errors are issued, and null is returned.
     *
     * @throws CssException
     */
    public static CssSimpleSelectorSequence createSimpleSelectorSequence(final CssToken start,
        final CssTokenIterator iter, final CssErrorHandler err) throws
        CssException
    {

      CssSimpleSelectorSequence seq = new CssSimpleSelectorSequence(start.location);
      CssConstruct seqItem = createSimpleSelector(start, iter, err);
      if (seqItem == null)
      {
        //errors already issued
        return null;
      }

      seq.components.add(seqItem);

      CssToken next = iter.peek(FILTER_NONE);
      while (next.type != CssToken.Type.S
          && !MATCH_COMMA.apply(next)
          && !MATCH_OPENBRACE.apply(next)
          && !MATCH_COMBINATOR_CHAR.apply(next))
      {
        seqItem = createSimpleSelector(iter.next(FILTER_NONE), iter, err);
        if (seqItem == null)
        {
          //errors already issued
          return null;
        }
        seq.components.add(seqItem);
        next = iter.peek(FILTER_NONE);
      }
      return seq;
    }

    /**
     * Create one item in a simple selector sequence. If creation fails,
     * errors are issued, and null is returned. On return, the iterator
     * will return the next token after the constructs last token.
     */
    static CssConstruct createSimpleSelector(final CssToken start, final CssTokenIterator iter,
        final CssErrorHandler err) throws
        CssException
    {

      // type and universal selector; ns|E, *|E, |E, E
      if (start.type == CssToken.Type.IDENT || MATCH_STAR_PIPE.apply(start))
      {
        //note, we bundle universal selector in CssTypeSelector
        return createTypeSelector(start, iter, err);

        // hashname #{name}
      }
      else if (start.type == CssToken.Type.HASHNAME)
      {
        return new CssHashName(start.getChars(), start.location);

        // classname .{name}
      }
      else if (start.type == CssToken.Type.CLASSNAME)
      {
        return new CssClassName(start.getChars(), start.location);

        //attribute selector [...]
      }
      else if (MATCH_OPENSQUAREBRACKET.apply(start))
      {
        return createAttributeSelector(iter.next(), iter, err);

        //pseudo selector
      }
      else if (MATCH_COLON.apply(start))
      {
        return createPseudoSelector(start, iter, err);

			//keyframes percentage 
			} else if(start.type == CssToken.Type.QNTY_PERCENTAGE) {	
				//note, for now, "from" and "to" keywords become type selectors above, 
				//this handles only the percentage TODO FIX				
				CssSelector sel = new CssSelector(start.location);
				sel.components.add(new CssQuantity(start.chars, CssQuantity.Unit.PERCENTAGE, start.location));
				return sel;
      }
      else
      {
				
        err.error(new CssGrammarException(GRAMMAR_UNEXPECTED_TOKEN, start.location, start.chars));
        return null;
      }

    }

    /**
     * Create a combinator. Note that this method does not support the S combinator.
     * This method also returns null without issuing errors
     */
    static CssSelectorCombinator createCombinator(final CssToken start,
        final CssTokenIterator iter, final CssErrorHandler err)
    {
      char symbol;
      if (start.type == CssToken.Type.CHAR)
      {
        char ch = start.getChar();
        if (ch == '>')
        {
          symbol = ch;
        }
        else if (ch == '+')
        {
          symbol = ch;
        }
        else if (ch == '~')
        {
          symbol = ch;
        }
        else
        {
          return null;
        }
      }
      else
      {
        return null;

      }
      return new CssSelectorCombinator(symbol, start.location);
    }

    static CssPseudoSelector createPseudoSelector(final CssToken start,
        final CssTokenIterator iter, final CssErrorHandler err) throws
        CssException
    {

      CssPseudoSelector.Type type;
      CssPseudoSelector cps;

      StringBuilder sb = new StringBuilder();
      sb.append(start.getChars());
      CssToken next = iter.next(FILTER_NONE);
      if (MATCH_COLON.apply(next))
      {
        type = CssPseudoSelector.Type.PSEUDO_ELEMENT;
        sb.append(next.getChars());
        next = iter.next(FILTER_NONE);
      }
      else
      {
        type = CssPseudoSelector.Type.PSEUDO_CLASS;
      }
      cps = new CssPseudoSelector(type, start.location);

      if (next.type == CssToken.Type.IDENT)
      {
        sb.append(next.getChars());
        cps.name = sb.toString();

      }
      else if (next.type == CssToken.Type.FUNCTION)
      {

        //need to get the colons into the name so clone and mod the token
        CssToken tk = new CssToken(next.type, next.location,
            sb.toString() + next.chars, next.errors.isPresent() ? next.errors.get() : null);

        //general functional pseudos and negation pseudos have different contentmodels
        //functional: [ [ PLUS | '-' | DIMENSION | NUMBER | STRING | IDENT ] S* ]+
        //pseudo: type_selector | universal | HASH | class | attrib | pseudo

        CssConstruct func;
        if (Ascii.toLowerCase(next.getChars()).startsWith("not"))
        {
          func = createNegationPseudo(tk, iter, err);
        }
        else
        {
          func = createFunctionalPseudo(tk, iter, MATCH_OPENBRACE, err);
        }

        if (func == null)
        {
          err.error(new CssGrammarException(
              CssErrorCode.GRAMMAR_UNEXPECTED_TOKEN, iter.last.location, iter.last.chars,
              next.getChars()));
          return null;
        }
        cps.function = (CssFunction) func;
      }
      return cps;
    }

    static CssConstruct createFunctionalPseudo(final CssToken start,
        final CssTokenIterator iter, final Predicate<CssToken> limit,
        final CssErrorHandler err)
    {

      String name = start.getChars().substring(0, start.getChars().length() - 1);
      CssFunction function = new CssFunction(name, start.location);

      CssToken tk = iter.next();
      while (!MATCH_CLOSEPAREN.apply(tk))
      {
        if (limit.apply(tk))
        {
          return null;
        }
        CssConstruct cc = CssConstructFactory.create(tk, iter, limit, ContextRestrictions.PSEUDO_FUNCTIONAL);
        if (cc == null)
        {
          return null;
        }
        else
        {
          function.components.add(cc);
        }
        tk = iter.next();
      }
      return function;
    }

    static CssConstruct createNegationPseudo(final CssToken start,
        final CssTokenIterator iter, final CssErrorHandler err) throws
        CssException
    {

      String name = start.getChars().substring(0, start.getChars().length() - 1);

      CssFunction negation = new CssFunction(name, start.location);

      CssToken tk = iter.next();
      CssConstruct cc = CssSelectorConstructFactory.createSimpleSelector(tk, iter, err);
      if (cc == null || !ContextRestrictions.PSEUDO_NEGATION.apply(cc))
      {
        return null;
      }
      else
      {
        negation.components.add(cc);
        iter.next();
      }
      return negation;
    }

    static CssAttributeSelector createAttributeSelector(final CssToken start,
        final CssTokenIterator iter, final CssErrorHandler err) throws
        CssException
    {

      CssAttributeSelector cas = new CssAttributeSelector(start.location);

      CssTypeSelector cts = createTypeSelector(start, iter, err);
      if (cts == null)
      {
        //factory method has issued errors
        return null;
      }
      cas.components.add(cts);

      CssToken next = iter.next(); // ']' or string matcher
      if (!MATCH_CLOSESQUAREBRACKET.apply(next))
      {
        if (MATCH_ATTRIBUTE_SELECTOR_MATCHERS.apply(next))
        {
          CssAttributeMatchSelector casm = createAttributeMatchSelector(next, iter, err);
          cas.components.add(casm);

          next = iter.next();
          CssConstruct val = CssConstructFactory.create(next, iter, MATCH_CLOSESQUAREBRACKET,
              ContextRestrictions.ATTRIBUTE_SELECTOR_VALUE);
          if (val != null)
          {
            cas.components.add(val);
          }
          else
          {
            err.error(new CssGrammarException(
                CssErrorCode.GRAMMAR_EXPECTING_TOKEN, next.location, next.chars,
                Messages.get("a_string_or_dentifier")));
            return null;
          }
          iter.next(); // ']'
        }
        else
        {
          err.error(new CssGrammarException(
              CssErrorCode.GRAMMAR_EXPECTING_TOKEN, next.location, next.chars,
              Messages.get("an_attribute_value_matcher")));
          return null;
        }
      }
      return cas;
    }

    static CssAttributeMatchSelector createAttributeMatchSelector(final CssToken tk,
        final CssTokenIterator iter, final CssErrorHandler err)
    {
      CssAttributeMatchSelector.Type type;
      switch (tk.type)
      {
        case INCLUDES:
          type = CssAttributeMatchSelector.Type.INCLUDES;
          break;
        case DASHMATCH:
          type = CssAttributeMatchSelector.Type.DASHMATCH;
          break;
        case PREFIXMATCH:
          type = CssAttributeMatchSelector.Type.PREFIXMATCH;
          break;
        case SUFFIXMATCH:
          type = CssAttributeMatchSelector.Type.SUFFIXMATCH;
          break;
        case SUBSTRINGMATCH:
          type = CssAttributeMatchSelector.Type.SUBSTRINGMATCH;
          break;
        default:
          type = CssAttributeMatchSelector.Type.EQUALS;
          break;
      }
      return new CssAttributeMatchSelector(tk.getChars(), type, tk.location);
    }

    private static CssTypeSelector createTypeSelector(final CssToken start,
        final CssTokenIterator iter, final CssErrorHandler err) throws
        CssException
    {

      if (start.type != CssToken.Type.IDENT && !MATCH_STAR_PIPE.apply(start))
      {
        err.error(new CssGrammarException(
            CssErrorCode.GRAMMAR_EXPECTING_TOKEN, start.location,
            start.getChars(), Messages.get("a_type_or_universal_selector")));
        return null;
      }

      StringBuilder sb = new StringBuilder();
      sb.append(start.getChars());

      if (MATCH_PIPE.apply(start))
      {
        //|E
        CssToken next = iter.peek(FILTER_NONE);
        if (next.type != CssToken.Type.IDENT)
        {
          err.error(new CssGrammarException(
              CssErrorCode.GRAMMAR_EXPECTING_TOKEN, next.location,
              next.getChars(), Messages.get("a_type_or_universal_selector")));
          return null;
        }
        else
        {
          sb.append(iter.next().getChars());
        }
      }
      else if (MATCH_PIPE.apply(iter.peek(FILTER_NONE)))
      {
        //ns|E, *|E,
        sb.append(iter.next().getChars());
        CssToken next = iter.next(FILTER_NONE);
        if (next.type != CssToken.Type.IDENT && !MATCH_STAR.apply(next))
        {
          err.error(new CssGrammarException(
              CssErrorCode.GRAMMAR_EXPECTING_TOKEN, start.location,
              next.getChars(), Messages.get("a_type_or_universal_selector")));
          return null;
        }
        else
        {
          sb.append(next.getChars());
        }
      }
      else if (iter.peek(FILTER_NONE).type == CssToken.Type.IDENT)
      {
        sb.append(iter.next().getChars());
      }

      return new CssTypeSelector(sb.toString(), start.location);
    }
  }

  static final class CssConstructFactory
  {

    static CssConstruct create(final CssToken start, final CssTokenIterator iter,
        final Predicate<CssToken> limit, final Predicate<CssConstruct> permitted)
    {

      CssTokenTransform transform = transformerMappings.get(start.type);
      return transform == null ? null : transform.build(start, iter, limit, permitted);

    }


    static final CssTokenTransform BUILDER_FUNCTION = new CssTokenTransform()
    {
      public CssFunction build(CssToken start, CssTokenIterator iter,
          Predicate<CssToken> limit, Predicate<CssConstruct> permitted)
      {

        String name = start.getChars().substring(0, start.getChars().length() - 1);
        CssFunction function = new CssFunction(name, start.location);

        CssToken tk = iter.next();
        while (!MATCH_CLOSEPAREN.apply(tk))
        {
          if (limit.apply(tk))
          {
            return null;
          }
          CssConstruct cc = CssConstructFactory.create(tk, iter, limit,
              ContextRestrictions.FUNCTION);
          if (cc == null || !permitted.apply(cc))
          {
            return null;
          }
          else
          {
            function.components.add(cc);
          }
          tk = iter.next();
        }
        return function;
      }

    };

    private static final CssTokenTransform BUILDER_ATOMIC = new CssTokenTransform()
    {
      public CssConstruct build(final CssToken start, final CssTokenIterator iter,
          final Predicate<CssToken> limit, final Predicate<CssConstruct> permitted)
      {

        CssConstruct.Type type = genericTypeMappings.get(start.type);
        CssConstruct cc;

        switch (type)
        {
          case KEYWORD:
            cc = new CssKeyword(start.getChars(), start.location);
            break;
          case URI:
            cc = new CssURI(start.getChars(), start.location);
            break;
          case STRING:
            cc = new CssString(start.getChars(), start.location);
            break;
          case URANGE:
            cc = new CssUnicodeRange(start.getChars(), start.location);
            break;
          case HASHNAME:
            cc = new CssHashName(start.getChars(), start.location);
            break;
          case CLASSNAME:
            cc = new CssClassName(start.getChars(), start.location);
            break;
          default:
            throw new IllegalStateException("CssTokenTransform BUILDER_ATOMIC");
        }

        return permitted.apply(cc) ? cc : null;

      }
    };

    private static final CssTokenTransform BUILDER_CHAR = new CssTokenTransform()
    {
      public CssConstruct build(CssToken start, CssTokenIterator iter,
          Predicate<CssToken> limit, Predicate<CssConstruct> permitted)
      {
        CssConstruct ret;

        char chr = start.getChar();
        if (chr == '{' || chr == '}' || chr == ';')
        {
          return null;
        }
        else if (chr == '(' || chr == '[')
        {
          ret = BUILDER_SCOPEDGROUP.build(start, iter, limit, permitted);
        }
        else
        {
          ret = new CssSymbol(start.chars, start.location);
        }

        if (ret == null)
        {
          return null;
        }
        return permitted.apply(ret) ? ret : null;
      }
    };

    /**
     * Builder for general scoped groups aka (...) and [...]
     */
    static final CssTokenTransform BUILDER_SCOPEDGROUP = new CssTokenTransform()
    {
      public CssConstruct build(CssToken start, CssTokenIterator iter,
          Predicate<CssToken> limit, Predicate<CssConstruct> permitted)
      {

        CssScopedGroup.Type type;
        Predicate<CssToken> end;


        if (MATCH_OPENPAREN.apply(start))
        {
          type = CssScopedGroup.Type.PAREN;
          end = MATCH_CLOSEPAREN;
        }
        else if (MATCH_OPENSQUAREBRACKET.apply(start))
        {
          type = CssScopedGroup.Type.BRACKET;
          end = MATCH_CLOSESQUAREBRACKET;
        }
        else
        {
          throw new IllegalStateException();
        }

        CssScopedGroup group = new CssScopedGroup(type, start.location);

        CssToken tk = iter.next();
        while (!end.apply(tk))
        {
          if (limit.apply(tk))
          {
            return null;
          }
          CssConstruct cc = CssConstructFactory.create(tk, iter, limit,
              ContextRestrictions.FUNCTION);
          if (cc == null || !permitted.apply(cc))
          {
            return null;
          }
          else
          {
            group.components.add(cc);
          }
          tk = iter.next();
        }
        return group;
      }

    };

    private static final CssTokenTransform BUILDER_QNTY = new CssTokenTransform()
    {
      public CssConstruct build(final CssToken start, final CssTokenIterator iter,
          final Predicate<CssToken> limit, final Predicate<CssConstruct> permitted)
      {

        CssQuantity cq = new CssQuantity(start.chars, quantityMappings.get(start.type),
            start.location);

        return permitted.apply(cq) ? cq : null;

      }
    };

    private static final Map<CssToken.Type, CssTokenTransform> transformerMappings
        = new ImmutableMap.Builder<CssToken.Type, CssTokenTransform>()
        .put(CssToken.Type.FUNCTION, BUILDER_FUNCTION)
        .put(CssToken.Type.CHAR, BUILDER_CHAR)
        .put(CssToken.Type.IDENT, BUILDER_ATOMIC)
        .put(CssToken.Type.URI, BUILDER_ATOMIC)
        .put(CssToken.Type.STRING, BUILDER_ATOMIC)
        .put(CssToken.Type.AND, BUILDER_ATOMIC)
        .put(CssToken.Type.NOT, BUILDER_ATOMIC)
        .put(CssToken.Type.ONLY, BUILDER_ATOMIC)
        .put(CssToken.Type.URANGE, BUILDER_ATOMIC)
        .put(CssToken.Type.HASHNAME, BUILDER_ATOMIC)
        .put(CssToken.Type.CLASSNAME, BUILDER_ATOMIC)
        .put(CssToken.Type.QNTY_ANGLE, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_DIMEN, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_REMS, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_EMS, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_EXS, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_FREQ, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_LENGTH, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_PERCENTAGE, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_RESOLUTION, BUILDER_QNTY)
        .put(CssToken.Type.QNTY_TIME, BUILDER_QNTY)
        .put(CssToken.Type.NUMBER, BUILDER_QNTY)
        .put(CssToken.Type.INTEGER, BUILDER_QNTY)
        .build();

    /* Map used by BUILDER_GENERIC to get type */
    private static final Map<CssToken.Type, CssConstruct.Type> genericTypeMappings
        = new ImmutableMap.Builder<CssToken.Type, CssConstruct.Type>()
        .put(CssToken.Type.IDENT, CssConstruct.Type.KEYWORD)
        .put(CssToken.Type.URI, CssConstruct.Type.URI)
        .put(CssToken.Type.STRING, CssConstruct.Type.STRING)
        .put(CssToken.Type.AND, CssConstruct.Type.KEYWORD)
        .put(CssToken.Type.NOT, CssConstruct.Type.KEYWORD)
        .put(CssToken.Type.ONLY, CssConstruct.Type.KEYWORD)
        .put(CssToken.Type.URANGE, CssConstruct.Type.URANGE)
        .put(CssToken.Type.HASHNAME, CssConstruct.Type.HASHNAME)
        .put(CssToken.Type.CLASSNAME, CssConstruct.Type.CLASSNAME)
        .build();

    /* Map used by BUILDER_QNTY to get subtype */
    private static final Map<CssToken.Type, CssQuantity.Unit> quantityMappings
        = new ImmutableMap.Builder<CssToken.Type, CssQuantity.Unit>()
        .put(CssToken.Type.QNTY_ANGLE, CssQuantity.Unit.ANGLE)
        .put(CssToken.Type.QNTY_DIMEN, CssQuantity.Unit.DIMEN)
        .put(CssToken.Type.QNTY_REMS, CssQuantity.Unit.REMS)
        .put(CssToken.Type.QNTY_EMS, CssQuantity.Unit.EMS)
        .put(CssToken.Type.QNTY_EXS, CssQuantity.Unit.EXS)
        .put(CssToken.Type.QNTY_FREQ, CssQuantity.Unit.FREQ)
        .put(CssToken.Type.QNTY_LENGTH, CssQuantity.Unit.LENGTH)
        .put(CssToken.Type.QNTY_PERCENTAGE, CssQuantity.Unit.PERCENTAGE)
        .put(CssToken.Type.QNTY_RESOLUTION, CssQuantity.Unit.RESOLUTION)
        .put(CssToken.Type.QNTY_TIME, CssQuantity.Unit.TIME)
        .put(CssToken.Type.NUMBER, CssQuantity.Unit.NUMBER)
        .put(CssToken.Type.INTEGER, CssQuantity.Unit.INTEGER)
        .build();

    interface CssTokenTransform
    {
      CssConstruct build(CssToken start, CssTokenIterator iter, Predicate<CssToken> limit,
          Predicate<CssConstruct> permitted);
    }
  }
}
