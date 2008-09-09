package com.thaiopensource.validate.schematron;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

class DraconianErrorListener implements ErrorListener {
  DraconianErrorListener() {
  }

  public void warning(TransformerException exception)
          throws TransformerException {
  }

  public void error(TransformerException exception)
          throws TransformerException {
    throw exception;
  }

  public void fatalError(TransformerException exception)
          throws TransformerException {
    throw exception;
  }
}
