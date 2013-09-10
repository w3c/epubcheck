package com.adobe.epubcheck.test;

import junit.framework.Assert;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class jsonCompare
{
  public static void compareJsonFiles(File expected, File actual, ArrayList<String> ignoreFields) throws IOException
  {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode expectedRoot = mapper.readTree(expected);
    JsonNode actualRoot = mapper.readTree(actual);
    ArrayList<String> errors = new ArrayList<String>();
    compareJsonNodes(expectedRoot, actualRoot, "", ignoreFields, errors);

    Assert.assertEquals("Outputted json isn't as expected. \n" + join(errors, "\n"), 0, errors.size());
  }

  public static void compareJsonNodes(JsonNode expected, JsonNode actual, String currentFieldPath, ArrayList<String> ignoreFields, ArrayList<String> errors)
  {
    if (ignoreFields.contains(currentFieldPath.replaceAll("\\[\\d+\\]", "[]")))
    {
      return;
    }

    if (expected instanceof ObjectNode && actual instanceof ObjectNode)
    {
      compareObjectNodes((ObjectNode) expected, (ObjectNode) actual, currentFieldPath, ignoreFields, errors);
    }
    else if (expected instanceof ArrayNode && actual instanceof ArrayNode)
    {
      compareArrayNodes((ArrayNode) expected, (ArrayNode) actual, currentFieldPath, ignoreFields, errors);
    }
    else
    {
      String actualValue = (actual == null) ? "null" : actual.toString();
      actualValue = normalizeLineEndings(actualValue);
      String expectedValue = expected.toString();
      expectedValue = normalizeLineEndings(expectedValue);
      if (!expectedValue.equals(actualValue))
      {
        addError(errors, "Values do not match", currentFieldPath, expectedValue, actualValue);
      }
    }
  }

  public static void compareObjectNodes(ObjectNode expected, ObjectNode actual, String currentFieldPath, ArrayList<String> ignoreFields, ArrayList<String> errors)
  {
    if (expected.equals(actual))
    {
      return;
    }
    Iterator<Map.Entry<String, JsonNode>> expectedFields = expected.getFields();
    ArrayList<String> expectedFieldNames = new ArrayList<String>();

    while (expectedFields.hasNext())
    {
      Map.Entry<String, JsonNode> expectedField = expectedFields.next();
      String fieldName = expectedField.getKey();
      expectedFieldNames.add(fieldName);
      String path = currentFieldPath + "/" + fieldName;
      JsonNode expectedNode = expectedField.getValue();
      JsonNode actualNode = actual.get(fieldName);
      compareJsonNodes(expectedNode, actualNode, path, ignoreFields, errors);
    }
    Iterator<String> actualFields = actual.getFieldNames();
    ArrayList<String> actualFieldNames = new ArrayList<String>();
    while (actualFields.hasNext())
    {
      actualFieldNames.add(actualFields.next());
    }
    if (expectedFieldNames.size() != actualFieldNames.size())
    {
      Collections.sort(expectedFieldNames);
      Collections.sort(actualFieldNames);
      addError(errors, "The field names do no match for the object", currentFieldPath, join(expectedFieldNames, ","), join(actualFieldNames, ","));
    }
  }

  public static void compareArrayNodes(ArrayNode expected, ArrayNode actual, String currentFieldPath, ArrayList<String> ignoreFields, ArrayList<String> errors)
  {
    if (expected.equals(actual))
    {
      return;
    }
    int expectedIndex = 0;
    Iterator<JsonNode> expectedElements = expected.getElements();
    while (expectedElements.hasNext())
    {
      String path = currentFieldPath + "[" + expectedIndex + "]";
      JsonNode expectedElement = expectedElements.next();
      JsonNode actualElement = actual.get(expectedIndex);
      compareJsonNodes(expectedElement, actualElement, path, ignoreFields, errors);
      expectedIndex++;
    }

    int actualIndex = 0;
    Iterator<JsonNode> actualElements = actual.getElements();
    while (actualElements.hasNext())
    {
      actualElements.next();
      actualIndex++;
    }
    if (expectedIndex != actualIndex)
    {
      addError(errors, "The number of elements in the array do not match.", currentFieldPath, Integer.toString(expectedIndex), Integer.toString(actualIndex));
    }
  }

  public static void addError(ArrayList<String> errors, String message, String path, String expected, String actual)
  {
    errors.add(message + "(" + path + ")\nExpected:\n" + expected + "\nActual:\n" + actual + "\n");
  }

  public static String join(ArrayList<String> coll, String delimiter)
  {
    if (coll.isEmpty())
    {
      return "";
    }

    StringBuilder sb = new StringBuilder();

    for (String x : coll)
    {
      sb.append(x);
      sb.append(delimiter);
    }

    sb.delete(sb.length() - delimiter.length(), sb.length());

    return sb.toString();
  }

  private static String normalizeLineEndings(String value)
  {
    String result = value.replaceAll("\\\\r\\\\n", " ");
    result = result.replaceAll("\\\\n", " ");
    return result;

  }
}
