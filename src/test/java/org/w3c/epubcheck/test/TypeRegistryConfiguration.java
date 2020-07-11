package org.w3c.epubcheck.test;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.util.List;
import java.util.Locale;

import org.hamcrest.Matcher;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.EPUBVersion;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.Transformer;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableRowTransformer;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer
{

  private static Function<String, MessageId> TO_ID = new Function<String, MessageId>()
  {

    @Override
    public MessageId apply(String input)
    {
      return MessageId.valueOf(input.replace('-', '_'));
    }

  };
  private static Function<String, Severity> TO_SEVERITY = new Function<String, Severity>()
  {

    @Override
    public Severity apply(String input)
    {
      if ("fatal error".equals(input)) input = "fatal";
      return Severity.valueOf(input.toUpperCase(Locale.ENGLISH));
    }

  };

  private static Function<String, EPUBVersion> TO_VERSION = new Function<String, EPUBVersion>()
  {

    @Override
    public EPUBVersion apply(String version)
    {
      Preconditions.checkNotNull(version);
      if (version.equals("3") || version.startsWith("3.")) return EPUBVersion.VERSION_3;
      if (version.equals("2") || version.startsWith("2.")) return EPUBVersion.VERSION_2;
      return EPUBVersion.Unknown;
    }
  };

  @Override
  @SuppressWarnings("rawtypes")
  public void configureTypeRegistry(TypeRegistry typeRegistry)
  {

    typeRegistry
        .defineDataTableType(new DataTableType(Matcher.class, new TableRowTransformer<Matcher>()
        {

          @Override
          public Matcher transform(List<String> row)
            throws Throwable
          {
            return (row.size() == 1) ? hasProperty("id", equalTo(TO_ID.apply(row.get(0))))
                : both(hasProperty("id", equalTo(TO_ID.apply(row.get(0)))))
                    .and(hasProperty("message", containsString(row.get(1))));

          }
        }));

    typeRegistry.defineParameterType(new ParameterType<>("checkerMode",
        "?i:(full publication|((Media Overlays|Navigation|Package|SVG Content|XHTML Content) Document))",
        ExecutionSteps.CheckerMode.class, new Transformer<ExecutionSteps.CheckerMode>()
        {

          @Override
          public ExecutionSteps.CheckerMode transform(String string)
            throws Throwable
          {
            switch (string.toLowerCase(Locale.ENGLISH))
            {
            case "full publication":
              return ExecutionSteps.CheckerMode.EPUB;
            case "media overlays document":
              return ExecutionSteps.CheckerMode.MEDIA_OVERLAYS_DOC;
            case "navigation document":
              return ExecutionSteps.CheckerMode.NAVIGATION_DOC;
            case "package document":
              return ExecutionSteps.CheckerMode.PACKAGE_DOC;
            case "svg content document":
              return ExecutionSteps.CheckerMode.SVG_CONTENT_DOC;
            case "xhtml content document":
              return ExecutionSteps.CheckerMode.XHTML_CONTENT_DOC;
            default:
              throw new IllegalArgumentException("Unknown file type: " + string);
            }
          }
        }));

    typeRegistry.defineParameterType(
        new ParameterType<>("locale", ".*?", Locale.class, new Transformer<Locale>()
        {

          @Override
          public Locale transform(String string)
            throws Throwable
          {
            try
            {
              return Locale.forLanguageTag(string);
            } catch (NullPointerException e)
            {
              throw new IllegalArgumentException("Couldn’t set locale: " + string, e);
            }
          }
        }));

    typeRegistry.defineParameterType(new ParameterType<>("messageId", "[A-Z]{3}-[0-9]{3}[a-z]?",
        MessageId.class, new Transformer<MessageId>()
        {

          @Override
          public MessageId transform(String string)
            throws Throwable
          {
            return TO_ID.apply(string);
          }
        }));

    typeRegistry.defineParameterType(
        new ParameterType<>("profile", ".*?", EPUBProfile.class, new Transformer<EPUBProfile>()
        {

          @Override
          public EPUBProfile transform(String string)
            throws Throwable
          {
            try
            {
              return EPUBProfile.valueOf(string.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e)
            {
              throw new IllegalArgumentException("Unknown EPUBCheck profile: " + string, e);
            }
          }
        }));

    typeRegistry.defineParameterType(new ParameterType<>("severity",
        "?i:(fatal error|error|warning|usage|info)", Severity.class, new Transformer<Severity>()
        {

          @Override
          public Severity transform(String string)
            throws Throwable
          {
            return TO_SEVERITY.apply(string);
          }
        }));

    typeRegistry.defineParameterType(new ParameterType<>("version", "\\d(?:\\.\\d)?(?:\\.\\d)?",
        EPUBVersion.class, new Transformer<EPUBVersion>()
        {

          @Override
          public EPUBVersion transform(String string)
            throws Throwable
          {
            return TO_VERSION.apply(string);
          }
        }));
  }

  @Override
  public Locale locale()
  {
    return Locale.ENGLISH;
  }
}