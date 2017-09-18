package org.idpf.epubcheck.util.saxon;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;

class Int64ValueSequence implements Sequence
{
  private Int64Value item;

  public Int64ValueSequence(Int64Value item)
  {
    this.item = item;
  }

  public Item head()
  {
    return item;
  }

  @Override
  public SequenceIterator iterate() throws
      XPathException
  {
    return item.iterate();
  }
}
