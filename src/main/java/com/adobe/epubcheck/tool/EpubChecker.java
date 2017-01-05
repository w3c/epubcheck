/*
 * Copyright (c) 2007 Adobe Systems Incorporated
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *    <AdobeIP#0000474>
 */

package com.adobe.epubcheck.tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.EpubCheckFactory;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.nav.NavCheckerFactory;
import com.adobe.epubcheck.opf.DocumentValidator;
import com.adobe.epubcheck.opf.DocumentValidatorFactory;
import com.adobe.epubcheck.opf.OPFCheckerFactory;
import com.adobe.epubcheck.opf.ValidationContext.ValidationContextBuilder;
import com.adobe.epubcheck.ops.OPSCheckerFactory;
import com.adobe.epubcheck.overlay.OverlayCheckerFactory;
import com.adobe.epubcheck.reporting.CheckingReport;
import com.adobe.epubcheck.util.Archive;
import com.adobe.epubcheck.util.DefaultReportImpl;
import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.FileResourceProvider;
import com.adobe.epubcheck.util.GenericResourceProvider;
import com.adobe.epubcheck.util.InvalidVersionException;
import com.adobe.epubcheck.util.Messages;
import com.adobe.epubcheck.util.OPSType;
import com.adobe.epubcheck.util.ReportingLevel;
import com.adobe.epubcheck.util.URLResourceProvider;
import com.adobe.epubcheck.util.XmlReportImpl;
import com.adobe.epubcheck.util.XmpReportImpl;
import com.adobe.epubcheck.util.outWriter;

public class EpubChecker
{

  static {
    /* fix #665 (window-less "Checker" gui app on Mac)
     * set -Djava.awt.headless=true programmatically as early as possible
     */
    System.setProperty("java.awt.headless", "true");
  }

  String path = null;
  String mode = null;
  EPUBProfile profile = null;
  EPUBVersion version = EPUBVersion.VERSION_3;
  boolean expanded = false;
  boolean keep = false;
  boolean jsonOutput = false;
  boolean xmlOutput = false;
  boolean xmpOutput = false;
  File fileOut;
  File listChecksOut;
  File customMessageFile;
  boolean listChecks = false;
  boolean useCustomMessageFile = false;
  boolean failOnWarnings = false;

  int reportingLevel = ReportingLevel.Info;

  private static final HashMap<OPSType, String> modeMimeTypeMap;

  static
  {
    HashMap<OPSType, String> map = new HashMap<OPSType, String>();

    map.put(new OPSType("xhtml", EPUBVersion.VERSION_2), "application/xhtml+xml");
    map.put(new OPSType("xhtml", EPUBVersion.VERSION_3), "application/xhtml+xml");

    map.put(new OPSType("svg", EPUBVersion.VERSION_2), "image/svg+xml");
    map.put(new OPSType("svg", EPUBVersion.VERSION_3), "image/svg+xml");

    map.put(new OPSType("mo", EPUBVersion.VERSION_3), "application/smil+xml");
    map.put(new OPSType("nav", EPUBVersion.VERSION_3), "application/xhtml+xml");
    modeMimeTypeMap = map;
  }

  private static final HashMap<OPSType, DocumentValidatorFactory> documentValidatorFactoryMap;
  private static final String E_PUB_CHECK_CUSTOM_MESSAGE_FILE = "ePubCheckCustomMessageFile";

  static
  {
    HashMap<OPSType, DocumentValidatorFactory> map = new HashMap<OPSType, DocumentValidatorFactory>();
    map.put(new OPSType(null, EPUBVersion.VERSION_2), EpubCheckFactory.getInstance());
    map.put(new OPSType(null, EPUBVersion.VERSION_3), EpubCheckFactory.getInstance());
    map.put(new OPSType("opf", EPUBVersion.VERSION_2), OPFCheckerFactory.getInstance());
    map.put(new OPSType("opf", EPUBVersion.VERSION_3), OPFCheckerFactory.getInstance());
    map.put(new OPSType("xhtml", EPUBVersion.VERSION_2), OPSCheckerFactory.getInstance());
    map.put(new OPSType("xhtml", EPUBVersion.VERSION_3), OPSCheckerFactory.getInstance());
    map.put(new OPSType("svg", EPUBVersion.VERSION_2), OPSCheckerFactory.getInstance());
    map.put(new OPSType("svg", EPUBVersion.VERSION_3), OPSCheckerFactory.getInstance());
    map.put(new OPSType("mo", EPUBVersion.VERSION_3), OverlayCheckerFactory.getInstance());
    map.put(new OPSType("nav", EPUBVersion.VERSION_3), NavCheckerFactory.getInstance());

    documentValidatorFactoryMap = map;
  }

  int validateFile(String path, EPUBVersion version, Report report, EPUBProfile profile)
  {
    GenericResourceProvider resourceProvider;

    if (path.startsWith("http://") || path.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(path);
    }
    else
    {
      File f = new File(path);
      if (f.exists())
      {
        resourceProvider = new FileResourceProvider(path);
      }
      else
      {
        System.err.println(String.format(Messages.get("file_not_found"), path));
        return 1;
      }
    }

    OPSType opsType = new OPSType(mode, version);

    DocumentValidatorFactory factory = documentValidatorFactoryMap.get(opsType);

    if (factory == null)
    {
      outWriter.println(Messages.get("display_help"));
      System.err.println(String.format(Messages.get("mode_version_not_supported"), mode, version));

      throw new RuntimeException(String.format(Messages.get("mode_version_not_supported"), mode,
          version));
    }

    DocumentValidator check = factory.newInstance(new ValidationContextBuilder().path(path)
        .report(report).resourceProvider(resourceProvider).mimetype(modeMimeTypeMap.get(opsType))
        .version(version).profile(profile).build());
    
    if (check.getClass() == EpubCheck.class)
    {
      int validationResult = ((EpubCheck) check).doValidate();
      if (validationResult == 0)
      {
        outWriter.println(Messages.get("no_errors__or_warnings"));
        return 0;
      }
      else if (validationResult == 1)
      {
        System.err.println(Messages.get("there_were_warnings"));
        return failOnWarnings ? 1 : 0;
      }
      System.err.println(Messages.get("there_were_errors"));
      return 1;
    }
    else
    {
      boolean validationResult = check.validate();
      if (validationResult)
      {
        outWriter.println(Messages.get("no_errors__or_warnings"));
        return 0;
      }
      else if (report.getWarningCount() > 0 && report.getFatalErrorCount() == 0 && report.getErrorCount() == 0)
      {
        System.err.println(Messages.get("there_were_warnings"));
        return failOnWarnings ? 1 : 0;
      }
      else
      {
        System.err.println(Messages.get("there_were_errors"));
        return 1;
      }
    }
  }

  int validateEpubFile(String path, EPUBVersion version, Report report)
  {
    GenericResourceProvider resourceProvider;

    if (path.startsWith("http://") || path.startsWith("https://"))
    {
      resourceProvider = new URLResourceProvider(path);
    }
    else
    {
      File f = new File(path);
      if (f.exists())
      {
        resourceProvider = new FileResourceProvider(path);
      }
      else
      {
        System.err.println(String.format(Messages.get("file_not_found"), path));
        return 1;
      }
    }

    OPSType opsType = new OPSType(mode, version);

    DocumentValidatorFactory factory = documentValidatorFactoryMap.get(opsType);

    if (factory == null)
    {
      outWriter.println(Messages.get("display_help"));
      System.err.println(String.format(Messages.get("mode_version_not_supported"), mode, version));

      throw new RuntimeException(String.format(Messages.get("mode_version_not_supported"), mode,
          version));
    }

    DocumentValidator check = factory.newInstance(
        new ValidationContextBuilder().path(path)
        .report(report).resourceProvider(resourceProvider).mimetype(modeMimeTypeMap.get(opsType))
        .version(version).profile(profile).build());

    boolean validationResult = check.validate();
    if (validationResult)
    {
      outWriter.println(Messages.get("no_errors__or_warnings"));
      return 0;
    }
    else if (report.getWarningCount() > 0 && report.getFatalErrorCount() == 0 && report.getErrorCount() == 0)
    {
      System.err.println(Messages.get("there_were_warnings"));
      return failOnWarnings ? 1 : 0;
    }
    else
    {
      System.err.println(Messages.get("there_were_errors"));
      return 1;
    }
  }

  public int run(String[] args)
  {
    Report report = null;
    int returnValue = 1;
    try
    {
      if (processArguments(args))
      {
        report = createReport();
        report.initialize();
        if (listChecks)
        {
          dumpMessageDictionary(report);
          return 0;
        }
        if (useCustomMessageFile)
        {
          report.setCustomMessageFile(customMessageFile.getAbsolutePath());
        }
        returnValue = processFile(report);
        int returnValue2 = report.generate();
        if (returnValue == 0)
        {
          returnValue = returnValue2;
        }
      }
    } catch (Exception ignored)
    {
      returnValue = 1;
    } finally
    {
      printEpubCheckCompleted(report);
    }
    return returnValue;
  }

  private void printEpubCheckCompleted(Report report)
  {
    if(report != null) {
      StringBuilder messageCount = new StringBuilder();
      if(reportingLevel <= ReportingLevel.Fatal) {
        messageCount.append(Messages.get("messages") + ": ");
        messageCount.append(String.format(Messages.get("counter_fatal"), report.getFatalErrorCount()));
      }
      if(reportingLevel <= ReportingLevel.Error) {
        messageCount.append(" / " + String.format(Messages.get("counter_error"), report.getErrorCount()));
      }
      if(reportingLevel <= ReportingLevel.Warning) {
        messageCount.append(" / " + String.format(Messages.get("counter_warn"), report.getWarningCount()));
      }
      if(reportingLevel <= ReportingLevel.Info) {
        messageCount.append(" / " + String.format(Messages.get("counter_info"), report.getInfoCount()));
      }
      if(reportingLevel <= ReportingLevel.Usage) {
        messageCount.append(" / " + String.format(Messages.get("counter_usage"), report.getUsageCount()));
      }
      if(messageCount.length() > 0) {
        messageCount.append("\n");
        outWriter.println(messageCount);
      }
    }
    outWriter.println(Messages.get("epubcheck_completed"));
    outWriter.setQuiet(false);
  }

  private void dumpMessageDictionary(Report report)
    throws IOException
  {
    OutputStreamWriter fw = null;
    try
    {
      if (listChecksOut != null)
      {
        fw = new FileWriter(listChecksOut);
      }
      else
      {
        fw = new OutputStreamWriter(System.out);
      }
      report.getDictionary().dumpMessages(fw);
    } catch (Exception e)
    {
      if (listChecksOut != null)
      {
        System.err.println(String.format(Messages.get("error_creating_config_file"),
            listChecksOut.getAbsoluteFile()));
      }
      System.err.println(e.getMessage());
    } finally
    {
      if (fw != null)
      {
        try
        {
          fw.close();
        } catch (IOException ignored)
        {
        }
      }
    }
  }

  private Report createReport()
    throws IOException
  {
    Report report;
    if (listChecks)
    {
      report = new DefaultReportImpl("none");
    }
    else if (jsonOutput)
    {
      report = new CheckingReport(path, (fileOut == null) ? null : fileOut.getPath());
    }
    else if (xmlOutput)
    {
      PrintWriter pw = null;
      if (fileOut == null)
      {
        pw = new PrintWriter(System.out, true);
      }
      else
      {
        pw = new PrintWriter(fileOut, "UTF-8");
      }
      report = new XmlReportImpl(pw, path, EpubCheck.version());
    }
    else if (xmpOutput)
    {
      PrintWriter pw = null;
      if (fileOut == null)
      {
        pw = new PrintWriter(System.out, true);
      }
      else
      {
        pw = new PrintWriter(fileOut, "UTF-8");
      }
      report = new XmpReportImpl(pw, path, EpubCheck.version());
    }
    else
    {
      report = new DefaultReportImpl(path);
    }
    report.setReportingLevel(this.reportingLevel);
    if (useCustomMessageFile)
    {
      report.setOverrideFile(customMessageFile);
    }

    return report;
  }

  public int processEpubFile(String[] args)
  {
    Report report = null;
    int returnValue = 1;
    try
    {
      if (processArguments(args))
      {
        report = createReport();
        report.initialize();
        if (listChecks)
        {
          dumpMessageDictionary(report);
          return 0;
        }
        if (useCustomMessageFile)
        {
          report.setCustomMessageFile(customMessageFile.getAbsolutePath());
        }
        returnValue = processEpubFile(report);
        int returnValue2 = report.generate();
        if (returnValue == 0)
        {
          returnValue = returnValue2;
        }
      }
    } catch (Exception ignored)
    {
      returnValue = 1;
    } finally
    {
      printEpubCheckCompleted(report);
    }
    return returnValue;
  }

  int processEpubFile(Report report)
  {
    report.info(null, FeatureEnum.TOOL_NAME, "epubcheck");
    report.info(null, FeatureEnum.TOOL_VERSION, EpubCheck.version());
    report.info(null, FeatureEnum.TOOL_DATE, EpubCheck.buildDate());
    int result;

    try
    {
      if (!expanded)
      {
        if (mode != null)
        {
          report.info(null, FeatureEnum.EXEC_MODE,
              String.format(Messages.get("single_file"), mode, version.toString(), profile));
        }
        result = validateFile(path, version, report, profile);
      }
      else
      {
        System.err.println(Messages.get("error_processing_unexpanded_epub"));
        return 1;
      }

      return result;
    } catch (Throwable e)
    {
      e.printStackTrace();
      return 1;
    } finally
    {
      report.close();
    }
  }

  private int processFile(Report report)
  {
    report.info(null, FeatureEnum.TOOL_NAME, "epubcheck");
    report.info(null, FeatureEnum.TOOL_VERSION, EpubCheck.version());
    report.info(null, FeatureEnum.TOOL_DATE, EpubCheck.buildDate());
    int result = 0;

    try
    {
      if (expanded)
      {
        // check existance of path (fix #525)
        File f = new File(path);
        if (!f.exists())
        {
          System.err.println(String.format(Messages.get("directory_not_found"), path));
          return 1;
        }

        Archive epub;
        try
        {
          epub = new Archive(path, true);
        } catch (RuntimeException ex)
        {
          System.err.println(Messages.get("there_were_errors"));
          return 1;
        }

        epub.createArchive();
        report.setEpubFileName(epub.getEpubFile().getAbsolutePath());
        EpubCheck check = new EpubCheck(epub.getEpubFile(), report, profile);
        int validationResult = check.doValidate();
        if (validationResult == 0)
        {
          outWriter.println(Messages.get("no_errors__or_warnings"));
          result = 0;
        }
        else if (validationResult == 1)
        {
          System.err.println(Messages.get("there_were_warnings"));
          result = failOnWarnings ? 1 : 0;
        }
        else if (validationResult >= 2)
        {
          System.err.println(Messages.get("there_were_errors"));
          result = 1;
        }

        if (keep)
        {
          if ((report.getErrorCount() > 0) || (report.getFatalErrorCount() > 0))
          {
            // keep if valid or only warnings
            System.err.println(Messages.get("deleting_archive"));
            epub.deleteEpubFile();
          }
        }
        else
        {
          epub.deleteEpubFile();
        }
      }
      else
      {
        if (mode != null)
        {
          report.info(null, FeatureEnum.EXEC_MODE,
              String.format(Messages.get("single_file"), mode, version.toString(), profile));
        }
        result = validateFile(path, version, report, profile);
      }

      return result;
    } catch (Throwable e)
    {
      e.printStackTrace();
      return 1;
    } finally
    {
      report.close();
    }
  }

  /**
   * This method iterates through all of the arguments passed to main to find
   * accepted flags and the name of the file to check. This method returns the
   * last argument that ends with ".epub" (which is assumed to be the file to
   * check) Here are the currently accepted flags: <br>
   * <br>
   * -? or -help = display usage instructions <br>
   * -v or -version = display tool version number
   *
   * @param args
   *          String[] containing arguments passed to main
   * @return the name of the file to check
   */
  private boolean processArguments(String[] args)
  {
    // Exit if there are no arguments passed to main
    if (args.length < 1)
    {
      System.err.println(Messages.get("argument_needed"));
      return false;
    }

    setCustomMessageFileFromEnvironment();
    for (int i = 0; i < args.length; i++)
    {
      if (args[i].equals("--version") || args[i].equals("-version") || args[i].equals("-v"))
      {
        if (i + 1 < args.length)
        {
          ++i;
          if (args[i].equals("2.0") || args[i].equals("2"))
          {
            version = EPUBVersion.VERSION_2;
          }
          else if (args[i].equals("3.0") || args[i].equals("3"))
          {
            version = EPUBVersion.VERSION_3;
          }
          else
          {
            outWriter.println(Messages.get("display_help"));
            throw new RuntimeException(new InvalidVersionException(
                InvalidVersionException.UNSUPPORTED_VERSION));
          }
        }
        else
        {
          outWriter.println(Messages.get("display_help"));
          throw new RuntimeException(Messages.get("version_argument_expected"));
        }
      }
      else if (args[i].equals("--mode") || args[i].equals("-mode") || args[i].equals("-m"))
      {
        if (i + 1 < args.length)
        {
          mode = args[++i];
          expanded = mode.equals("exp");
        }
        else
        {
          outWriter.println(Messages.get("display_help"));
          throw new RuntimeException(Messages.get("mode_argument_expected"));
        }
      }
      else if (args[i].equals("--profile") || args[i].equals("-profile") || args[i].equals("-p"))
      {
        if (i + 1 < args.length)
        {
          String profileStr = args[++i];
          try
          {
            profile = EPUBProfile.valueOf(profileStr.toUpperCase(Locale.ROOT));
          } catch (IllegalArgumentException e)
          {
            System.err.println(Messages.get("mode_version_ignored", profileStr));
            profile = EPUBProfile.DEFAULT;
          }
        }
        else
        {
          outWriter.println(Messages.get("display_help"));
          throw new RuntimeException(Messages.get("profile_argument_expected"));
        }
      }
      else if (args[i].equals("--save") || args[i].equals("-save") || args[i].equals("-s"))
      {
        keep = true;
      }
      else if (args[i].equals("--out") || args[i].equals("-out") || args[i].equals("-o"))
      {
        if ((args.length > (i + 1)) && !(args[i + 1].startsWith("-")))
        {
          fileOut = new File(args[++i]);
        }
        else if ((args.length > (i + 1)) && (args[i + 1].equalsIgnoreCase("-")))
        {
          fileOut = null;
          i++;
        }
        else
        {
          File pathFile = new File(path);
          if (pathFile.isDirectory())
          {
            fileOut = new File(pathFile.getAbsoluteFile().getParentFile(), pathFile.getName()
                + "check.xml");
          }
          else
          {
            fileOut = new File(path + "check.xml");
          }
        }
        xmlOutput = true;
      }
      else if (args[i].equals("--json") || args[i].equals("-json") || args[i].equals("-j"))
      {
        if ((args.length > (i + 1)) && !(args[i + 1].startsWith("-")))
        {
          fileOut = new File(args[++i]);
        }
        else if ((args.length > (i + 1)) && (args[i + 1].equalsIgnoreCase("-")))
        {
          fileOut = null;
          i++;
        }
        else
        {
          File pathFile = new File(path);
          if (pathFile.isDirectory())
          {
            fileOut = new File(pathFile.getAbsoluteFile().getParentFile(), pathFile.getName()
                + "check.json");
          }
          else
          {
            fileOut = new File(path + "check.json");
          }
        }
        jsonOutput = true;
      }
      else if (args[i].equals("--xmp") || args[i].equals("-xmp") || args[i].equals("-x"))
      {
        if ((args.length > (i + 1)) && !(args[i + 1].startsWith("-")))
        {
          fileOut = new File(args[++i]);
        }
        else if ((args.length > (i + 1)) && (args[i + 1].equalsIgnoreCase("-")))
        {
          fileOut = null;
          i++;
        }
        else
        {
          File pathFile = new File(path);
          if (pathFile.isDirectory())
          {
            fileOut = new File(pathFile.getAbsoluteFile().getParentFile(), pathFile.getName()
                + "check.xmp");
          }
          else
          {
            fileOut = new File(path + "check.xmp");
          }
        }
        xmpOutput = true;
      }
      else if (args[i].equals("--info") || args[i].equals("-i"))
      {
        reportingLevel = ReportingLevel.Info;
      }
      else if (args[i].equals("--fatal") || args[i].equals("-f"))
      {
        reportingLevel = ReportingLevel.Fatal;
      }
      else if (args[i].equals("--error") || args[i].equals("-e"))
      {
        reportingLevel = ReportingLevel.Error;
      }
      else if (args[i].equals("--warn") || args[i].equals("-w"))
      {
        reportingLevel = ReportingLevel.Warning;
      }
      else if (args[i].equals("--usage") || args[i].equals("-u"))
      {
        reportingLevel = ReportingLevel.Usage;
      }
      else if (args[i].equals("--quiet") || args[i].equals("-q"))
      {
        outWriter.setQuiet(true);
      }
      else if (args[i].startsWith("--failonwarnings"))
      {
        String fw = args[i].substring("--failonwarnings".length());
        failOnWarnings = (fw.compareTo("-") != 0);
      }
      else if (args[i].equals("--redir") || args[i].equals("-r"))
      {
        if (i + 1 < args.length)
        {
          fileOut = new File(args[++i]);
        }
      }
      else if (args[i].equals("--customMessages") || args[i].equals("-c"))
      {
        if (i + 1 < args.length)
        {
          String fileName = args[i + 1];
          if ("none".compareTo(fileName.toLowerCase(Locale.ROOT)) == 0)
          {
            customMessageFile = null;
            useCustomMessageFile = false;
            ++i;
          }
          else if (!fileName.startsWith("-"))
          {
            customMessageFile = new File(fileName);
            useCustomMessageFile = true;
            ++i;
          }
          else
          {
            System.err.println(String.format(Messages.get("expected_message_filename"), fileName));
            displayHelp();
            return false;
          }
        }
      }
      else if (args[i].equals("--listChecks") || args[i].equals("-l"))
      {
        if (i + 1 < args.length)
        {
          if (!args[i + 1].startsWith("-"))
          {
            listChecksOut = new File(args[++i]);
          }
          else
          {
            listChecksOut = null;
          }
        }
        listChecks = true;
      }
      else if (args[i].equals("--help") || args[i].equals("-help") || args[i].equals("-h")
          || args[i].equals("-?"))
      {
        displayHelp(); // display help message
      }
      else
      {
        if (path == null)
        {
          path = args[i];
        }
        else
        {
          System.err.println(String.format(Messages.get("unrecognized_argument"), args[i]));
          displayHelp();
          return false;
        }
      }
    }

    if ((xmlOutput && xmpOutput) || (xmlOutput && jsonOutput) || (xmpOutput && jsonOutput))
    {
      System.err.println(Messages.get("output_type_conflict"));
      return false;
    }
    if (path != null)
    {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < path.length(); i++)
      {
        if (path.charAt(i) == '\\')
        {
          sb.append('/');
        }
        else
        {
          sb.append(path.charAt(i));
        }
      }
      path = sb.toString();
    }

    if (path == null)
    {
      if (listChecks)
      {
        return true;
      }
      else
      {
        System.err.println(Messages.get("no_file_specified"));
        return false;
      }
    }
    else if (path.matches(".+\\.[Ee][Pp][Uu][Bb]"))
    {
      if (mode != null || version != EPUBVersion.VERSION_3)
      {
        System.err.println(Messages.get("mode_version_ignored"));
        mode = null;
      }
    }
    else if (mode == null)
    {
      outWriter.println(Messages.get("mode_required"));
      return false;
    }

    return true;
  }

  private void setCustomMessageFileFromEnvironment()
  {
    Map<String, String> env = System.getenv();
    String customMessageFileName = env.get(E_PUB_CHECK_CUSTOM_MESSAGE_FILE);
    if (customMessageFileName != null && customMessageFileName.length() > 0)
    {
      File f = new File(customMessageFileName);
      if (f.exists())
      {
        customMessageFile = f;
        useCustomMessageFile = true;
      }
    }
  }

  /**
   * This method displays a short help message that describes the command-line
   * usage of this tool
   */
  private static void displayHelp()
  {
    outWriter.println(String.format(Messages.get("help_text"), EpubCheck.version()));
  }
}
