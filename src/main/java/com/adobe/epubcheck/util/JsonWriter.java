package com.adobe.epubcheck.util;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is used to create json output
 */
public class JsonWriter
{
  private ObjectMapper objectMapper;

  private JsonWriter(ObjectMapper objectMapper)
  {
    if (objectMapper == null)
    {
      throw new IllegalArgumentException("objectMapper argument is required.");
    }
    this.objectMapper = objectMapper;
  }

  public static JsonWriter createJsonWriter(boolean pretty)
  {
    JsonFactory jf = new JsonFactory();
    ObjectMapper om = new ObjectMapper(jf);
    om.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    om.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    om.configure(SerializationConfig.Feature.INDENT_OUTPUT, pretty);
    om.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
    om.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
    return new JsonWriter(om);
  }

  public void writeJson(Object content, OutputStream os)
      throws
      IOException
  {
    this.objectMapper.writeValue(os, content);
  }
}
