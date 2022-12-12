package org.w3c.epubcheck;

import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Streams;

import io.cucumber.java.ParameterType;

public final class SharedParameterTypes
{

  @ParameterType("\".*?\"")
  /**
   * Splits a string of key/value pairs into an {@link ImmutableListMultimap}.
   * Key/value pairs are separated with ';', key and values are separated with
   * '=', multiple values are separated with ','.
   * 
   * <p>
   * Note: no validation is performed. Callers must make sure the string is well
   * formed.
   * </p>
   * 
   * @param mapping
   *        a string specifying key/value pairs
   * @return a parsed map
   */
  public ImmutableListMultimap<String, String> multimap(String mapping)
  {
    return Splitter.on(";").withKeyValueSeparator('=')
        .split(mapping.substring(1, mapping.length() - 1)).entrySet().stream()
        .collect(ImmutableListMultimap.flatteningToImmutableListMultimap(Map.Entry::getKey,
            e -> Streams.stream(Splitter.on(',').split(e.getValue()))));
  }

}
