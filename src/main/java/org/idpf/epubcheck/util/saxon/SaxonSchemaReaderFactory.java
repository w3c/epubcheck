package org.idpf.epubcheck.util.saxon;

import javax.xml.transform.TransformerFactory;

import com.thaiopensource.validate.schematron.SchematronSchemaReaderFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.sxpath.XPathStaticContext;
import net.sf.saxon.trans.SymbolicName;

public class SaxonSchemaReaderFactory extends SchematronSchemaReaderFactory
{
  public void initTransformerFactory(TransformerFactory factory)
  {
    super.initTransformerFactory(factory);
    factory.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
    SymbolicName.F lineNumberFn = new SymbolicName.F(LineNumberFunction.QNAME, 0);
    SymbolicName.F columnNumberFn = new SymbolicName.F(ColumnNumberFunction.QNAME, 0);
    SymbolicName.F systemIdFn = new SymbolicName.F(SystemIdFunction.QNAME, 0);
    if (factory instanceof TransformerFactoryImpl)
    {
      Configuration configuration = ((TransformerFactoryImpl) factory).getConfiguration();
      configuration.setErrorReporterFactory(config -> {
        return new ErrorReporter()
        {

          @Override
          public void report(XmlProcessingError error)
          {
            System.out.println(error.getMessage());

          }
        };
      });
      XPathStaticContext xpathContext = new IndependentContext(configuration);
      if (!xpathContext.getFunctionLibrary().isAvailable(lineNumberFn, 20))
      {
        configuration.registerExtensionFunction(new LineNumberFunction());
      }
      if (!xpathContext.getFunctionLibrary().isAvailable(columnNumberFn, 20))
      {
        configuration.registerExtensionFunction(new ColumnNumberFunction());
      }
      if (!xpathContext.getFunctionLibrary().isAvailable(systemIdFn, 20))
      {
        configuration.registerExtensionFunction(new SystemIdFunction());
      }
    }
  }
}
