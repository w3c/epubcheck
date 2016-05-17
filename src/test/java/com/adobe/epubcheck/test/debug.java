package com.adobe.epubcheck.test;

import com.adobe.epubcheck.tool.EpubChecker;
import com.adobe.epubcheck.util.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: apond
 * Date: 2/7/13
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class debug
{
  // Quick way to debug a one off epub file.
  // @Test
  public void run_local_epub_test()
  {
    String[] args = new String[3];
    String inputPath = "9780307272119_epub.v10.epub";
    args[0] = inputPath;
    args[1] = "-j";
    args[2] = inputPath + ".json";
    try
    {
      EpubChecker checker = new EpubChecker();
      int result = checker.run(args);
    }
    catch (Exception ex)
    {
      System.err.println(Messages.get("there_were_errors"));
      ex.printStackTrace();
    }
  }
}
