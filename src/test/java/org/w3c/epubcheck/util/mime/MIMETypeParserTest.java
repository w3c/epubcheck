package org.w3c.epubcheck.util.mime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.nullValue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.w3c.epubcheck.util.mime.MIMETypeParser.DefaultErrorHandler;
import org.w3c.epubcheck.util.mime.MIMETypeParser.ParseError;

public class MIMETypeParserTest
{

  private MIMETypeParser parser;
  private TestErrorHandler handler;

  @Before
  public void before()
  {
    handler = new TestErrorHandler();
    parser = new MIMETypeParser(handler);
  }

  @Test
  public void testDebug()
  {
  }

  @Test
  public void testBasic()
  {
    valid("type/subtype", "type/subtype");
  }

  @Test
  public void testParameters()
  {
    // Plain parameters
    valid("type/subtype;name=value", "type/subtype;name=value");
    valid("type/subtype;p1=v1;p2=v2", "type/subtype;p1=v1;p2=v2");
    // Quoted parameters
    valid("type/subtype;name=\"\"", "type/subtype;name=\"\"");
    valid("type/subtype;name=\"value\"", "type/subtype;name=value");
    valid("type/subtype;name=\"val ue\"", "type/subtype;name=\"val ue\"");
    valid("type/subtype;name=\"\\\"\"", "type/subtype;name=\"\\\"\"");
    valid("type/subtype;name=\"\\\\\"", "type/subtype;name=\"\\\\\"");
    // Mixed quoted and unquoted parameters
    valid("type/subtype;p1=\"v 1\";p2=v2", "type/subtype;p1=\"v 1\";p2=v2");
    valid("type/subtype;p1=v1;p2=\"v2\"", "type/subtype;p1=v1;p2=v2");
    // Duplicate parameters
    valid("type/subtype;p=v1;p=v2", "type/subtype;p=v1");
  }

  @Test
  public void testOptionalWhitespace()
  {
    valid("  type/subtype  ", "type/subtype");
    valid(" type/subtype", "type/subtype");
    valid(" type/subtype  ;  p1=v1  ", "type/subtype;p1=v1");
    valid(" type/subtype  ;  p1=v1  ;  p2=v2  ", "type/subtype;p1=v1;p2=v2");
  }

  @Test
  public void testLetterCase()
  {
    valid("Type/SUBTYPE", "type/subtype");
    valid(" type/subtype;PaRaM=VaLuE", "type/subtype;param=VaLuE");
    valid(" type/subtype;PaRaM=\"VaLuE\"", "type/subtype;param=VaLuE");
  }

  @Test
  public void testTypeFailures()
  {
    failure(null, ParseError.NULL, -1);
    failure("", ParseError.NO_TYPE, -1);
    failure("/", ParseError.NO_TYPE, 0);
    failure("/subtype", ParseError.NO_TYPE, 0);
    failure("  /subtype", ParseError.NO_TYPE, 2);
  }

  @Test
  public void testSubtypeFailures()
  {
    failure("type", ParseError.NO_SUBTYPE, 4);
    failure("type/", ParseError.NO_SUBTYPE, 5);
    failure("type/  ", ParseError.ILLEGAL_CODE_POINT, 5);
  }

  @Test
  public void testWhitespaceFailures()
  {
    failure("type /sub", ParseError.ILLEGAL_CODE_POINT, 4);
    failure("type/ sub", ParseError.ILLEGAL_CODE_POINT, 5);
  }

  @Test
  public void testCodePointsFailures()
  {
    failure("tyépe/sub", ParseError.ILLEGAL_CODE_POINT, 2);
    failure("type/s@b", ParseError.ILLEGAL_CODE_POINT, 6);
    failure("type/s b", ParseError.ILLEGAL_CODE_POINT, 7);
  }

  @Test
  public void testRecoverableMissingParamNameErrors()
  {
    error("type/sub;", "type/sub", ParseError.NO_PARAM_NAME, 9);
    error("type/sub;  ", "type/sub", ParseError.NO_PARAM_NAME, 11);
    error("type/sub;;p=v", "type/sub;p=v", ParseError.NO_PARAM_NAME, 9);
  }

  @Test
  public void testRecoverableMissingParamValueErrors()
  {
    error("type/sub;p", "type/sub", ParseError.NO_PARAM_VALUE, 10);
    error("type/sub;p;p=v", "type/sub;p=v", ParseError.NO_PARAM_VALUE, 10);
    error("type/sub;p=", "type/sub", ParseError.NO_PARAM_VALUE, 11);
    error("type/sub;p=;p=v", "type/sub;p=v", ParseError.NO_PARAM_VALUE, 11);
  }

  @Test
  public void testRecoverableMissingQuoteErrors()
  {
    error("type/sub;p=\"v", "type/sub;p=v", ParseError.END_QUOTE_MISSING, 13);
    error("type/sub;p=\" ", "type/sub;p=\" \"", ParseError.END_QUOTE_MISSING, 13);
    error("type/sub;p=\"", "type/sub;p=\"\"", ParseError.END_QUOTE_MISSING, 12);
    error("type/sub;p=\"\\", "type/sub;p=\"\\\\\"", ParseError.END_QUOTE_MISSING, 13);
  }

  @Test
  public void testRecoverableCodePointsErrors()
  {
    error("type/sub;p=v v", "type/sub;p=\"v v\"", ParseError.UNEXPECTED_CODE_POINT, 12);
    error("type/sub;p=v@v", "type/sub;p=\"v@v\"", ParseError.UNEXPECTED_CODE_POINT, 12);
    error("type/sub;p=v\"v", "type/sub;p=\"v\\\"v\"", ParseError.UNEXPECTED_CODE_POINT, 12);
    error("type/sub;p=v\\v", "type/sub;p=\"v\\\\v\"", ParseError.UNEXPECTED_CODE_POINT, 12);
    error("type/sub;p=\"v\"x", "type/sub;p=v", ParseError.UNEXPECTED_CODE_POINT, 14);
    error("type/sub;p=\"v\" x", "type/sub;p=v", ParseError.UNEXPECTED_CODE_POINT, 15);
  }

  @Test
  public void testRecoverableWhitespaceErrors()
  {
    error("type/sub;p = v", "type/sub", ParseError.UNEXPECTED_CODE_POINT, 10);
    error("type/sub;p= v", "type/sub;p=\" v\"", ParseError.UNEXPECTED_CODE_POINT, 11);
    error("type/sub;p ", "type/sub", ParseError.UNEXPECTED_CODE_POINT, 10);
    error("type/sub;p ;p=v", "type/sub;p=v", ParseError.UNEXPECTED_CODE_POINT, 10);
    error("type/sub;p= ", "type/sub;p=\"\"", ParseError.UNEXPECTED_CODE_POINT, 11);
    error("type/sub;p= ;p=v", "type/sub;p=\"\"", ParseError.UNEXPECTED_CODE_POINT, 11);
  }

  private void valid(String string, String expected)
  {
    handler.clear();
    MIMEType mimetype = parser.parse(string);
    assertThat(mimetype.toString(), is(expected));
    assertThat(handler.errors(), empty());
  }

  private void failure(String string, ParseError error, int position)
  {
    handler.clear();
    MIMEType mimetype = parser.parse(string);
    assertThat(mimetype, is(nullValue()));
    assertThat(handler.firstError(), is(error));
    assertThat(handler.firstPosition(), is(position));
  }

  private void error(String string, String expected, ParseError error, int position)
  {
    handler.clear();
    MIMEType mimetype = parser.parse(string);
    assertThat(mimetype.toString(), is(expected));
    assertThat(handler.firstError(), is(error));
    assertThat(handler.firstPosition(), is(position));
  }

  private static class TestErrorHandler extends DefaultErrorHandler
  {
    List<MIMETypeParser.ParseError> errors = new ArrayList<>();
    List<Integer> positions = new ArrayList<>();

    public void clear()
    {
      errors.clear();
      positions.clear();
    }

    public List<MIMETypeParser.ParseError> errors()
    {
      return errors;
    }

    public List<Integer> positions()
    {
      return positions;
    }

    public MIMETypeParser.ParseError firstError()
    {
      return (!errors.isEmpty()) ? errors().get(0) : null;
    }

    public int firstPosition()
    {
      return (!errors.isEmpty()) ? positions().get(0) : -2;
    }

    @Override
    public void error(ParseError error, int position)
    {
      errors.add(error);
      positions.add(position);
    }

    @Override
    public void failure(ParseError error, int position)
    {
      errors.add(error);
      positions.add(position);
    }

  }
}
