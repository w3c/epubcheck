package org.w3c.epubcheck.util.url;

import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.epubcheck.constants.MIMEType;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import io.mola.galimatias.URL;
import io.mola.galimatias.URLUtils;
import net.sf.saxon.om.NameChecker;

/**
 * Represents a URL fragment, after parsing micro-syntaxes.
 */
public class URLFragment
{

  /**
   * Represents a non-existent fragment, for which {@link #exists()} returns
   * <code>false</code>
   */
  public static final URLFragment NONE = new URLFragment(new Parser().parse(null, null));

  private final String fragment;
  private final String scheme;
  private final String id;
  private final boolean isMediaFragment;
  private final boolean isValid;

  private URLFragment(Parser parser)
  {
    this.fragment = parser.fragment;
    this.id = Strings.nullToEmpty(parser.id);
    this.scheme = Strings.nullToEmpty(parser.scheme);
    this.isMediaFragment = parser.isMediaFragment;
    this.isValid = parser.isValid;
  }

  /**
   * Returns the element ID represented by this fragment if this is an ID-based
   * fragment, or the empty string otherwise.
   * 
   * @return an element ID or the empty string.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Returns the scheme represented by this fragment if this is an scheme-based
   * fragment, or the empty string otherwise.
   * 
   * @return a scheme name or the empty string.
   */
  public String getScheme()
  {
    return scheme;
  }

  /**
   * @return <code>true</code> iff the URL from which this was parsed had a
   *           fragment.
   */
  public boolean exists()
  {
    return fragment != null;
  }

  /**
   * @return <code>true</code> iff this fragment is the empty string or
   *           represents a non-existent fragment.
   */
  public boolean isEmpty()
  {
    return fragment == null || fragment.isEmpty();
  }

  /**
   * @return <code>true</code> iff this fragment is valid according to its
   *           target MIME type.
   */
  public boolean isValid()
  {
    return isValid;
  }

  /**
   * @return <code>true</code> iff this fragment is a media fragment.
   */
  public boolean isMediaFragment()
  {
    return isMediaFragment;
  }

  @Override
  /**
   * @return the full fragment string.
   */
  public String toString()
  {
    return fragment;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(fragment);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    URLFragment other = (URLFragment) obj;
    return Objects.equals(fragment, other.fragment);
  }

  /**
   * Parse the fragment of the given URL, according to the rules defined for the
   * given MIME type.
   * 
   * If the URL has no fragment, returns {@link #NONE}
   * 
   * <h2>HTML types "application/xhtml+xml" and "text/html"</h2>
   *
   * <p>
   * The following fragment patterns are supported:
   * </p>
   * 
   * <ul>
   * <li>regular ID-based fragments (`#name`)</li>
   * <li>scheme-based fragments (`#name(something)`)</li>
   * <li>media fragments (`#name=value`, with name one of
   * `t|xywh|track|id|xyn|xyr`</li>
   * <li>fragment directives (`#name:~:text=range`)</li>
   * </ul>
   * 
   * <p>
   * Note that this deviates from the HTML standard in the following way:
   * </p>
   * 
   * <ul>
   * <li>HTML does not define specific logic for scheme-based or media
   * fragments, which must be treated like any other IDs. However, EPUB makes
   * use of them notably for EPUB CFI or region-based navigation.</li>
   * <li>Fragment directives (as used in text fragments), is an incubating
   * standard (at the time of writing) and is likely not well supported by
   * reading system, but its syntax is specific enough to lower the risk of
   * false-positive.</li>
   * </ul>
   * 
   * <h2>SVG type "image/svg+xml"</h2>
   *
   * <p>
   * The following fragment patterns are supported:
   * </p>
   * 
   * <ul>
   * <li>shorthand bare form names (<code>#name</code>). Validation checks that
   * the name is an XML NCName.</li>
   * <li>SVG view specification (<code>#svgView(…)</code>). Validation currently
   * does not look into the parenthesis content.</li>
   * <li>basic media fragments (<code>#xywh=0,0,50,50</code>). Validation checks
   * the syntax of spatial and temporal dimensions.</li>
   * </ul>
   * 
   * <h2>Other type</h2>
   * 
   * <p>
   * Any other type is assumed to be XML. The following fragment patterns are
   * supported:
   * </p>
   * 
   * <ul>
   * <li>shorthand bare form names (<code>#name</code>). Validation checks that
   * the name is an XML NCName.</li>
   * <li>scheme-based fragments (`#name(something)`). No validation of the
   * scheme name or syntax.</li>
   * </ul>
   * 
   * @param url
   *          a URL
   * @param mimetype
   *          the MIME type of the URL target
   * @return a parsed fragment (cannot be <code>null</code>)
   */
  public static URLFragment parse(URL url, String mimetype)
  {
    if (url == null || url.fragment() == null)
    {
      return NONE;
    }
    else
    {
      return new URLFragment(new Parser().parse(url.fragment(), mimetype));
    }
  }

  /**
   * Parse the fragment of the given URL, according to the default rules (XML
   * MIME type), see {@link URLFragment#parse(URL, String)}.
   * 
   * @param url
   *          a URL
   * @return a parsed fragment (cannot be<code>null</code>)
   */
  public static URLFragment parse(URL url)
  {
    return parse(url, "");
  }

  private static final class Parser
  {
    private String fragment;
    private String scheme;
    private String id;
    private boolean isMediaFragment = false;
    private boolean isValid = true;

    /*
     * Parse the fragment, by dispatching to a type-specific method.
     * 
     * Note (2022): parsing would likely be more efficient if implemented as a
     * state parser instead of using regex-based string matching.
     */
    private Parser parse(String fragment, String mimetype)
    {
      this.fragment = fragment;
      if (fragment != null)
      {
        switch (MIMEType.get(mimetype))
        {
        case SVG:
          parseSVGFragment(fragment);
          break;
        case HTML:
        case XHTML:
          parseHTMLFragment(fragment);
          break;
        default:
          parseXMLFragment(fragment);
          break;
        }
      }
      return this;
    }

    private static final Pattern SCHEME_BASED = Pattern.compile("(\\w+)\\(.*\\)");
    private static final Pattern MEDIA_FRAGMENT = Pattern
        .compile("(t|xywh|track|id|xyn|xyr)=[^&]+(&[^&=]+=[^&]+)*");

    // Parses an XML fragment identifier
    private void parseXMLFragment(String fragment)
    {
      Matcher matcher;
      // Schema based
      if ((matcher = SCHEME_BASED.matcher(fragment)).matches())
      {
        this.scheme = matcher.group(1);
      }
      // ID fragment
      else
      {
        this.id = URLUtils.percentDecode(fragment);
        this.isValid = NameChecker.isValidNCName(id);
      }
    }

    /*
     * Parses an HTML fragment identifier
     */
    private void parseHTMLFragment(String fragment)
    {
      Matcher matcher;
      // strip fragment directive
      // see https://wicg.github.io/scroll-to-text-fragment/
      int index;
      if ((index = fragment.indexOf(":~:")) > -1)
      {
        fragment = fragment.substring(0, index);
      }
      // scheme-based fragment
      if ((matcher = SCHEME_BASED.matcher(fragment)).matches())
      {
        this.scheme = matcher.group(1);
      }
      // media fragment
      else if ((matcher = MEDIA_FRAGMENT.matcher(fragment)).matches())
      {
        this.isMediaFragment = true;
      }
      // ID fragment
      else
      {
        this.id = URLUtils.percentDecode(fragment);
      }
    }

    /*
     * Parses an SVG fragment identifier, see:
     * https://www.w3.org/TR/SVG/linking.html#SVGFragmentIdentifiersDefinitions
     */
    private void parseSVGFragment(String fragment)
    {

      if (fragment.isEmpty()) return;

      // Split the fragment into &-separated components
      Iterator<String> components = Splitter.on('&').split(fragment).iterator();
      String first = components.next();

      // SVG view specification
      if (first.startsWith("svgView("))
      {
        // check the SVG view is well-formed
        isValid = parseSVGView(first);
        // check optional remaining components are well-formed time segments
        while (isValid && components.hasNext())
        {
          isValid = parseTimeSegment(components.next());
        }
      }
      // Temporal media fragment
      else if (first.startsWith("t="))
      {
        isMediaFragment = true;
        // check the first component is a well-formed time segment
        isValid = parseTimeSegment(first);
        // check optional remaining components are well-formed space segments
        while (isValid && components.hasNext())
        {
          isValid = parseSpaceSegment(components.next());
        }
      }
      // Spatial media fragment
      else if (first.startsWith("xywh="))
      {
        isMediaFragment = true;
        // check the first component is a well-formed space segment
        isValid = parseSpaceSegment(first);
        // check optional remaining components are well-formed time segments
        while (isValid && components.hasNext())
        {
          isValid = parseTimeSegment(components.next());
        }
      }
      else if (first.contains("="))
      {
        isValid = false;
      }
      // Shorthand bare name
      else
      {
        // Record the ID, percent-decoded
        this.id = URLUtils.percentDecode(first);
        // check validity of the ID
        this.isValid = NameChecker.isValidNCName(id);
        // check optional remaining components are well-formed time segments
        while (isValid && components.hasNext())
        {
          isValid = parseTimeSegment(components.next());
        }
      }
    }

    private static final Pattern SVGVIEW = Pattern.compile("svgView\\(.+\\)");

    private boolean parseSVGView(String string)
    {
      return isValid = SVGVIEW.matcher(string).matches();
    }

    private static final Pattern SPATIAL = Pattern
        .compile("xywh=(pixel:|percent:)?\\d+,\\d+,\\d+,\\d+");

    private boolean parseSpaceSegment(String string)
    {
      return isValid = SPATIAL.matcher(string).matches();
    }

    private static final Pattern TEMPORAL = Pattern
        .compile("t=(?:npt:)?(?:([0-9.:]+)(?:,([0-9.:]+))?|,([0-9.:]+))");
    private static final Pattern NPTTIME = Pattern
        .compile("((\\d+)|([0-5]\\d:[0-5]\\d)|(\\d+:[0-5]\\d:[0-5]\\d))(\\.\\d*)?");

    private boolean parseTimeSegment(String string)
    {
      Matcher matcher = TEMPORAL.matcher(string);
      if (isValid = matcher.matches())
      {
        int i = 1;
        while (isValid && i <= matcher.groupCount())
        {
          isValid = matcher.group(i) == null || NPTTIME.matcher(matcher.group(i)).matches();
          i++;
        }
      }
      return isValid;
    }
  }

}
