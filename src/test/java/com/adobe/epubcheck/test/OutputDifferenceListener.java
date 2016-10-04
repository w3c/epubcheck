package com.adobe.epubcheck.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Node;

import com.adobe.epubcheck.util.outWriter;

/**
 * Created with IntelliJ IDEA.
 * User: apond
 * Date: 4/3/13
 * Time: 11:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class OutputDifferenceListener implements DifferenceListener
{
  private int skippedComparisons = 0;

  @Override
  public int differenceFound(Difference difference)
  {
    NodeDetail expectedNode = difference.getControlNodeDetail();
    NodeDetail actualNode = difference.getTestNodeDetail();
    if (expectedNode == null || actualNode == null) {
        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }
    String expectedXPath = expectedNode.getXpathLocation();
    String actualXPath = actualNode.getXpathLocation();
    if (expectedXPath == null || actualXPath == null) {
        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }
    if (!expectedXPath.equals(actualXPath))
    {
      return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }
    String expectedValue = expectedNode.getValue();
    expectedValue = expectedValue.replaceAll("\\s+", " ").trim();
    String actualValue = actualNode.getValue();
    actualValue = actualValue.replaceAll("\\s+", " ").trim();
    if (expectedValue.equals(actualValue))
    {
      return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
    }
    if (expectedXPath.equals("/jhove[1]/@release")
        || expectedXPath.equals("/jhove[1]/@date")
        || expectedXPath.equals("/jhove[1]/date[1]/text()[1]")
        || expectedXPath.equals("/jhove[1]/repInfo[1]/@uri")
        || expectedXPath.equals("/jhove[1]/repInfo[1]/version[1]/text()[1]")
        || expectedXPath.equals("/xmpmeta[1]/RDF[1]/Description[1]/hasEvent[1]/hasEventDateTime[1]/text()[1]")
        || expectedXPath.equals("/xmpmeta[1]/RDF[1]/Description[1]/hasEvent[1]/hasEventRelatedAgent[1]/hasAgentName[1]/text()[1]")
        )
    {
      return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
    }
    // Be more robust to the modification/locale of the text of the messages (the associated code is already tested)
    if (expectedXPath.startsWith("/jhove[1]/repInfo[1]/messages[1]/message[") && expectedXPath.endsWith("]/text()[1]"))
    {
        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
    }

    if (isDate(expectedValue))
    {
      return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
    }

    return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;

  }

  @Override
  public void skippedComparison(Node node, Node node1)
  {
    outWriter.printf("Skipped Comparison " + node.toString());
    skippedComparisons++;
  }

  public int getSkippedComparisons()
  {
    return this.skippedComparisons;
  }

  private boolean isDate(String value)
  {
    boolean result = true;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    try
    {
      format.parse(value);
    }
    catch (ParseException e)
    {
      result = false;
    }
    return result;
  }
}
