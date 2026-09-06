package org.w3c.epubcheck.util.mime;

import org.w3c.epubcheck.util.infra.CodePoints;

/**
 * Serializes the MIME type as specified in
 * https://mimesniff.spec.whatwg.org/#serializing-a-mime-type
 * 
 * Conforming to living standard update: 17 July 2026
 */
public final class MIMETypeSerializer
{

  private MIMETypeSerializer()
  {
  }

  public static String serialize(MIMEType mimetype)
  {
    if (mimetype == null) return "";
    if (mimetype.parameters().isEmpty())
    {
      return mimetype.essence();
    }
    else
    {
      StringBuilder result = new StringBuilder();
      result.append(mimetype.essence());
      mimetype.parameters().entrySet().stream()
          .forEachOrdered(p -> {
            result.append(';').append(p.getKey()).append('=');
            if (p.getValue().isEmpty())
            {
              result.append("\"\"");
            }
            else if (p.getValue().codePoints()
                .anyMatch(c -> !CodePoints.isHTTPTokenCodePoint(c)))
            {
              result.append('"');
              p.getValue().codePoints()
                  .forEach(c -> {
                    if (c == '"' || c == '\\')
                      result.append('\\');
                    result.appendCodePoint(c);
                  });
              result.append('"');
            }
            else
            {
              result.append(p.getValue());
            }
          });
      return result.toString();
    }
  }
}
