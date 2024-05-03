package com.adobe.epubcheck.util;

import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Optional;

/**
 * This is used to create json output
 */
public class JsonWriter{

    public static class OptionalJsonSerializer extends JsonSerializer<Optional<String>> {
    @Override
    public void serialize(Optional<String> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
      jgen.writeString(value.orNull());
    }
  }

  private final ObjectMapper objectMapper;

  private JsonWriter(ObjectMapper objectMapper) {
    if (objectMapper == null) {
      throw new IllegalArgumentException("objectMapper argument is required.");
    }
    this.objectMapper = objectMapper;
  }

  public static JsonWriter createJsonWriter(boolean pretty) {
    JsonFactory jf = new JsonFactory();
    ObjectMapper om = new ObjectMapper(jf);
    om.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    om.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
    om.configure(SerializationFeature.INDENT_OUTPUT, pretty);
    om.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return new JsonWriter(om);
  }

  public void writeJson(Object content, PrintWriter pw) throws IOException {
    this.objectMapper.writeValue(pw, content);
  }
}
