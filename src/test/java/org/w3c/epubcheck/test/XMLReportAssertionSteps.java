package org.w3c.epubcheck.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.function.Supplier;

import javax.xml.transform.stream.StreamSource;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

public class XMLReportAssertionSteps
{

  private final TestReport report;
  private final Processor processor;
  private final XPathCompiler xpathCompiler;
  Supplier<XdmNode> xml = new Supplier<XdmNode>()
  {
    XdmNode xml = null;

    @Override
    public XdmNode get()
    {
      if (xml == null)
      {

        // Parse the XML report
        DocumentBuilder docBuilder = processor.newDocumentBuilder();
        try
        {
          xml = docBuilder.build(new StreamSource(new StringReader(report.getOutput())));
        } catch (SaxonApiException e)
        {
          assertThat(e.getMessage(), false);
        }
      }
      return xml;
    }
  };

  public XMLReportAssertionSteps(TestConfiguration configuration)
  {
    this.report = configuration.getReport();

    // Configure Saxon
    this.processor = new Processor(false);
    processor.getUnderlyingConfiguration()
        .setStandardErrorOutput(new PrintStream(new OutputStream()
        {
          // TODO when upgrading to Java 11, replace by
          // OutputStream#nullOutputStream()
          @Override
          public void write(int b)
            throws IOException
          {
          }
        }));
    xpathCompiler = processor.newXPathCompiler();
  }

  @Given("the default namespace is {string}")
  public void setDefaultNamespace(String namespace)
  {
    xpathCompiler.declareNamespace("", namespace);
  }

  @Then("the XML report is well-formed")
  public void xmlIsWellFormed()
  {
    // Parsing errors would be raised in the constructor already
  }

  @Then("(the )XPath (value of ){string} is true")
  public void xpathIsTrue(String xpath)
    throws SaxonApiException
  {
    assertThat(eval(xpath).effectiveBooleanValue(), is(true));
  }

  @Then("(the )XPath {string} exists")
  public void xpathExists(String xpath)
    throws SaxonApiException
  {
    assertThat(eval(xpath), is(not(emptyIterable())));

  }

  private XPathSelector eval(String xpath)
  {
    try
    {
      XPathSelector result = xpathCompiler.compile(xpath).load();
      result.setContextItem(xml.get());
      return result;
    } catch (SaxonApiException e)
    {
      throw new IllegalArgumentException(e);
    }
  }

}
