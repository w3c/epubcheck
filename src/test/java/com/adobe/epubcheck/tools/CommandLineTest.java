package com.adobe.epubcheck.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.tool.Checker;
import com.adobe.epubcheck.util.Messages;

public class CommandLineTest {

    private final Pattern usagePattern = Pattern.compile("^([\\s\\S]*\\n)?USAGE\\([\\s\\S]*$");
    private final Pattern warningPattern = Pattern.compile("^([\\s\\S]*\\n)?WARNING\\([\\s\\S]*$");
    private final Pattern errorPattern = Pattern.compile("^([\\s\\S]*\\n)?ERROR\\([\\s\\S]*$");

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private SecurityManager originalManager;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private final Messages messages = Messages.getInstance(Locale.ENGLISH);
    private Locale defaultLocale;

    @Before
    public void setUp()
    {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        this.originalManager = System.getSecurityManager();
        System.setSecurityManager(new NoExitSecurityManager());
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void tearDown()
    {
        Locale.setDefault(defaultLocale);
        System.setSecurityManager(this.originalManager);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Checks if for any reason the input arguments are null the return code is
     * set appropriately.
     */
    @Test
    public void nullPointerExceptionTest()
    {
        runCommandLineTest(1, (String[]) null);
    }

    /**
     * Testing the save function, using a directory with a correct epub structure it will
     * produce a correct packaged epub.
     */
    @Test
    public void archivingValidEPUBDirectoryTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("30-valid-test");
        File inputFile = new File(inputUrl.getPath());
        File out = new File(inputFile.getParent() + File.separator + "30-valid-test.epub");
        if (out.exists())
        {
            out.delete();
        }

        runCommandLineTest(0, "--mode", "exp", inputUrl.getPath(), "--save");

        runCommandLineTest(0, out.getAbsolutePath());

        if (out.exists())
        {
            out.delete();
        }
    }

    /**
     * Check if we can validate a single navigation file.
     */
    @Test
    public void singleNavigationFileTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("30-valid-test/OPS/nav.xhtml");
        runCommandLineTest(0, "-mode", "nav", inputUrl.getPath());
    }

    /**
     * Validate that testing a single navigation file without table of contents will
     * yield an error code.
     */
    @Test
    public void singleNavigationWithoutTableOfContentsTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("nav-no-toc.xhtml");
        runCommandLineTest(1, "-mode", "nav", inputUrl.getPath());
    }

    /**
     * This test runs the program without any arguments, expected is a message about that
     * arguments are required.
     */
    @Test
    public void noArgumentsTest()
    {
        runCommandLineTest(1);
        assertEquals("Command output not as expected", messages.get("argument_needed"), errContent.toString().trim());
    }

    /**
     * Verify that an epub with a strange casing will generate a warning.
     */
    @Test
    public void extensionTest1()
    {
        URL inputUrl = CommandLineTest.class.getResource("wrong_extension.ePub");
        runCommandLineTest(0, inputUrl.getPath());
        assertTrue(
                "Warning PKG-016 should be present when file has an incorrect extension",
                errContent.toString().contains("WARNING(PKG-016)")
        );
    }

    /**
     * Verify that a zipfile will inform the user that this is an uncommon extension.
     */
    @Test
    public void extensionTest2()
    {
        URL inputUrl = CommandLineTest.class.getResource("wrong_extension.zip");
        runCommandLineTest(0, inputUrl.getPath(), "--profile", "default");
        assertTrue(
                "Info PKG-024 should be present when file has an uncommon extension",
                errContent.toString().contains("INFO(PKG-024)")
        );
    }

    /**
     * Verify that an epub without extension works without any exceptions.
     */
    @Test
    public void extensionTest3()
    {
        URL inputUrl = CommandLineTest.class.getResource("wrong_extension");
        runCommandLineTest(0, inputUrl.getPath(), "--profile", "default");
    }

    /**
     * Verify that an unreadable EPUB file raises an error
     */
    @Test
    public void notfoundTest()
    {
        runCommandLineTest(1, "-mode", "nav", "-v", "3.0", "http://localhost:1234/notfound");
        assertTrue(errContent.toString().contains("FATAL(PKG-008)"));
    }

    /**
     * Validate that the -out parameter will generate a well formed xml output.
     *
     * @throws Exception Any parsing errors will be thrown as an exception.
     */
    @Test
    public void outputXMLReportTest() throws Exception
    {
        File tmpFile = File.createTempFile("test", ".xml");

        URL inputUrl = CommandLineTest.class.getResource("valid.epub");
        runCommandLineTest(0, inputUrl.getPath(), "-out", tmpFile.getAbsolutePath());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.parse(tmpFile);

        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
    }

    /**
     * Validate that the -out parameter will generate a well formed xml output for
     * unpacked epubs.
     *
     * @throws Exception Any parsing errors will be thrown as an exception.
     */
    @Test
    public void outputXMLModeExpandedReportTest() throws Exception
    {
        File tmpFile = File.createTempFile("test", ".xml");

        URL inputUrl = CommandLineTest.class.getResource("30-valid-test");
        runCommandLineTest(0, inputUrl.getPath(), "-mode", "exp", "-out", tmpFile.getAbsolutePath());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.parse(tmpFile);

        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
    }

    /**
     * Verify that no extra output is present when quiet mode is enabled.
     */
    @Test
    public void quietTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-warning-tester");
        runCommandLineTest(1, inputUrl.getPath(), "-mode", "exp", "--quiet", "--failonwarnings");

        assertEquals("Output should not be present", 0, outContent.size());
    }


    /**
     * Testing that no output is present when the flag quiet is set and that we get a correct xml
     * output report with the output flag.
     *
     * @throws Exception Any parsing errors will be thrown as an exception.
     */
    @Test
    public void quietRunWithOutputTest() throws Exception
    {
        File tmpFile = File.createTempFile("test", ".xml");

        URL inputUrl = CommandLineTest.class.getResource("valid.epub");
        runCommandLineTest(0, inputUrl.getPath(), "--quiet", "-out", tmpFile.getAbsolutePath());

        assertEquals("Output should not be present", 0, outContent.size());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.parse(tmpFile);

        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
    }

    /**
     * Testing that invalid options will return an invalid return code. We also need
     * to inform the user which option was invalid.
     */
    @Test
    public void invalidOptionTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("valid.epub");
        runCommandLineTest(1, inputUrl.getPath(), "--invalidoption");

        assertTrue(
            "Should contain the invalid option",
            errContent.toString().contains("--invalidoption")
        );
    }

    /**
     * When a translation is missing in that language it should fallback to the JVM default
     * localization language.
     */
    @Test
    public void missingTranslationShouldFallbackTest()
    {
        Locale temp = Locale.getDefault();
        Locale.setDefault(Locale.FRANCE);

        URL inputUrl = CommandLineTest.class.getResource("valid.epub");

        try
        {
            Checker.main(new String[] {inputUrl.getPath(), "--locale", "ar-eg"});
        }
        catch (NoExitSecurityManager.ExitException e)
        {
            assertEquals("Return code should be zero", 0, e.status);
        }

        assertTrue(
            "Valid Locale without translation should fallback to JVM default.",
            outContent.toString().contains("faites en utilisant")
        );
        Locale.setDefault(temp);
    }

    /**
     * When a translation is missing in that language it should fallback to the JVM default
     * localization language.
     */
    @Test
    public void localeShouldTranslateTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("valid.epub");
        runCommandLineTest(0, inputUrl.getPath(), "--locale", "fr-FR");

        assertTrue(
                "Valid Locale without translation should fallback to JVM default.",
                outContent.toString().contains("faites en utilisant")
        );
    }

    /**
     * When the user has forgot to specify the value for the locale parameter it should be
     * clear that it is missing from the command line.
     */
    @Test
    public void skippedLocaleShouldFailTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("valid.epub");
        runCommandLineTest(1, inputUrl.getPath(), "--locale", "--bad");

        assertTrue(
                "User should be informed about skipped locale",
                errContent.toString().contains("--bad")
        );
    }

    /**
     * When the user uses an incorrect locale the translation should fallback on the
     * JVM default language.
     */
    @Test
    public void incorrectLocaleShouldFailTest()
    {
        Locale.setDefault(Locale.FRANCE);

        URL inputUrl = CommandLineTest.class.getResource("valid.epub");

        try
        {
            Checker.main(new String[] {inputUrl.getPath(), "--locale", "foobar"});
        }
        catch (NoExitSecurityManager.ExitException e)
        {
            assertEquals("Return code should be zero", 0, e.status);
        }

        assertTrue(
                "Invalid Locale should use JVM default.",
                outContent.toString().contains("faites en utilisant")
        );
    }

    /**
     * When the locale parameter value is missing the missing_locale message should
     * be present in error output.
     */
    @Test
    public void missingLocaleShouldFailTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("valid.epub");
        runCommandLineTest(1, inputUrl.getPath(), "--locale");

        assertEquals(
                "Missing locale message should be present",
                messages.get("missing_locale"),
                errContent.toString().trim()
        );
    }


    /**
     * Running with the question mark argument, expected that we create some output where output contains
     * the version number.
     */
    @Test
    public void helpMessageTest1()
    {
        runCommandLineTest( 1, "-?");
        assertEquals("Command output not as expected", messages.get("no_file_specified"), errContent.toString().trim());
        String expected = String.format(messages.get("help_text").replaceAll("[\\s]+", " "), EpubCheck.version());
        String actual = outContent.toString();
        actual = actual.replaceAll("[\\s]+", " ");
        assertTrue("Help output isn't as expected", actual.contains(expected));
    }

    /**
     * Running with the single dash help argument, expected that we create some output where output contains
     * the version number.
     */
    @Test
    public void helpMessageTest2()
    {
        runCommandLineTest( 1, "-help");
        assertEquals("Command output not as expected", messages.get("no_file_specified"), errContent.toString().trim());
        String expected = String.format(messages.get("help_text").replaceAll("[\\s]+", " "), EpubCheck.version());
        String actual = outContent.toString();
        actual = actual.replaceAll("[\\s]+", " ");
        assertTrue("Help output isn't as expected", actual.contains(expected));
    }

    /**
     * Running with the double dash help argument, expected that we create some output where output contains
     * the version number.
     */
    @Test
    public void helpMessageTest3()
    {
        runCommandLineTest( 1, "--help");
        assertEquals("Command output not as expected", messages.get("no_file_specified"), errContent.toString().trim());
        String expected = String.format(messages.get("help_text").replaceAll("[\\s]+", " "), EpubCheck.version());
        String actual = outContent.toString();
        actual = actual.replaceAll("[\\s]+", " ");
        assertTrue("Help output isn't as expected", actual.contains(expected));
    }

    /**
     * Testing that the version command could be opened using double dash syntax.
     */
    @Test
    public void versionDisplayTest1()
    {
        runCommandLineTest(1, "--version");
        assertEquals("Command output not as expected", messages.get("no_file_specified"), errContent.toString().trim());
        String expected = String.format(messages.get("epubcheck_version_text").replaceAll("[\\s]+", " "), EpubCheck.version());
        String actual = outContent.toString();
        actual = actual.replaceAll("[\\s]+", " ");
        assertTrue("Help output isn't as expected", actual.contains(expected));
    }

    /**
     * Testing that the version command could be opened using single dash syntax.
     */
    @Test
    public void versionDisplayTest2()
    {
        runCommandLineTest(1, "-version");
        assertEquals("Command output not as expected", messages.get("no_file_specified"), errContent.toString().trim());
        String expected = String.format(messages.get("epubcheck_version_text").replaceAll("[\\s]+", " "), EpubCheck.version());
        String actual = outContent.toString();
        actual = actual.replaceAll("[\\s]+", " ");
        assertTrue("Help output isn't as expected", actual.contains(expected));
    }
    
    /**
     * Tests that the version message is displayed on the first line
     */
    @Test
    public void versionInfoComesFirst()
    {
      OutputStream out = new ByteArrayOutputStream();
      System.setOut(new PrintStream(out));
      System.setErr(new PrintStream(out));
      URL inputUrl = CommandLineTest.class.getResource("30-mimetype-invalid");
      runCommandLineTest(1, inputUrl.getPath(), "-mode", "exp");
      int versionMessageIndex = out.toString().indexOf("Validating using");
      int mimetypeErrorMessageIndex = out.toString().indexOf("mimetype");
      assertTrue("Version message should be in the output.", versionMessageIndex >= 0);
      assertTrue("PKG-007 should be in the output.", mimetypeErrorMessageIndex >= 0);
      assertTrue("Version message should be printed before PKG-007", versionMessageIndex < mimetypeErrorMessageIndex);
    }
    

    /**
     * This test checks if multiple output formats are chosen, expected is an error message instructing
     * user to choose either output formats but not both.
     */
    @Test
    public void conflictingOutputTest()
    {
        runCommandLineTest(1, "-o", "foo.xml", "-j", "bar.json");
        assertEquals("Command output not as expected", messages.get("output_type_conflict"), errContent.toString().trim());
    }

    /**
     * Checks if the usage messages are shown when the usage flag is set.
     */
    @Test
    public void severitiesUsageTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        runCommandLineTest(1, "-u", "--mode", "exp", inputUrl.getPath());

        assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        assertTrue("Warnings should be present", warningPattern.matcher(errContent.toString()).matches());
        assertTrue("Usage should be present", usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * Checks if the warning messages are shown when the warning flag is set,
     * but no usage messages.
     */
    @Test
    public void severitiesWarningTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        runCommandLineTest(1, "-w", "--mode", "exp", inputUrl.getPath());

        assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        assertTrue("Warnings should be present", warningPattern.matcher(errContent.toString()).matches());
        assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * Checks if the error messages are shown when the error flag is set, but no usage or
     * warning messages.
     */
    @Test
    public void severitiesErrorTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        runCommandLineTest(1, "-e", "--mode", "exp", inputUrl.getPath());

        assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        assertTrue("Warnings should not be present", !warningPattern.matcher(errContent.toString()).matches());
        assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * Checks that no error, warning or usage messages are shown when fatal flag is set.
     */
    @Test
    public void severitiesFatalTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        runCommandLineTest(0, "-f", "--mode", "exp", inputUrl.getPath());

        assertTrue("Errors should not be present", !errorPattern.matcher(errContent.toString()).matches());
        assertTrue("Warnings should not be present", !warningPattern.matcher(errContent.toString()).matches());
        assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * This test checks that we can override some error severity, messages and/or
     * suppress them all together.
     */
    @Test
    public void severitiesOverrideTest()
    {
        URL configUrl = CommandLineTest.class.getResource("severity_override.txt");
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");

        runCommandLineTest(
                1, "-c", configUrl.getPath(), "-u", "--mode", "exp",
                inputUrl.getPath()
        );

        assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        assertTrue("Warnings should not be present", !warningPattern.matcher(errContent.toString()).matches());
        assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());

        assertTrue(
                "Overridden message should be present",
                errContent.toString().contains("This is an overridden message"));
    }


    /**
     * Ensures that the right error code is present when the override configuration file
     * is missing.
     */
    @Test
    public void severitiesOverrideMissingFileTest()
    {
        URL configUrl = CommandLineTest.class.getResource(".");
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");


        runCommandLineTest(
                1, "-c", configUrl.getPath() + "/severity_override.missing_file",
                "-u", "--mode", "exp", inputUrl.getPath()
        );

        assertTrue(
                "Error CHK-001 should be present when file is missing",
                errContent.toString().contains("ERROR(CHK-001)")
        );
    }

    /**
     * Ensures that the right error code is present when the override configuration
     * contains a severity id that is not valid.
     */
    @Test
    public void severitiesOverrideBadIdTest()
    {
        URL configUrl = CommandLineTest.class.getResource("severity_override_bad_id.txt");
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");

        runCommandLineTest(
                1, "-c", configUrl.getPath(), "-u",
                "--mode", "exp", inputUrl.getPath()
        );

        assertTrue(
                "Error CHK-002 should be present when file contains a bad id",
                errContent.toString().contains("ERROR(CHK-002)")
        );
    }


    /**
     * Ensures that the right error code is present when the override configuration
     * contains a severity value that is not valid.
     */
    @Test
    public void severitiesOverrideBadSeverityTest()
    {
        URL configUrl = CommandLineTest.class.getResource("severity_override_bad_severity.txt");
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");

        runCommandLineTest(
                1, "-c", configUrl.getPath(), "-u",
                "--mode", "exp", inputUrl.getPath()
        );

        assertTrue(
                "Error CHK-003 should be present when file contains a bad severity",
                errContent.toString().contains("ERROR(CHK-003)")
        );
    }

    /**
     * Ensures that the right error code is present when the override configuration
     * contains a severity message that is not valid. (Incorrect number of parameters)
     */
    @Test
    public void severitiesOverrideBadMessageTest()
    {
        URL configUrl = CommandLineTest.class.getResource("severity_override_bad_message.txt");
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");

        runCommandLineTest(
                1, "-c", configUrl.getPath(), "-u",
                "--mode", "exp", inputUrl.getPath()
        );

        assertTrue(
                "Error CHK-004 should be present when file contains a bad message",
                errContent.toString().contains("ERROR(CHK-004)")
        );
    }

    /**
     * Ensures that the right error code is present when the override configuration
     * contains a severity suggestion that is not valid. (Incorrect number of parameters)
     */
    @Test
    public void severitiesOverrideBadSuggestionTest()
    {
        URL configUrl = CommandLineTest.class.getResource("severity_override_bad_suggestion.txt");
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");

        runCommandLineTest(
                1, "-c", configUrl.getPath(), "-u",
                "--mode", "exp", inputUrl.getPath()
        );

        assertTrue(
                "Error CHK-005 should be present when file contains a bad message",
                errContent.toString().contains("ERROR(CHK-005)")
        );
    }

    /**
     * This test contains warnings but should not fail. We expect the return code to be 0
     */
    @Test
    public void passOnWarningsTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-warning-tester");

        runCommandLineTest(
                0, "-u", "--mode", "exp", inputUrl.getPath()
        );
    }


    /**
     * This test contains warnings and the flag --failonwarnings will force warnings to
     * fail. We expect the return code to be 1.
     */
    @Test
    public void failOnWarningsTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-warning-tester");

        runCommandLineTest(
                1, "-u", "--mode", "exp",
                "--failonwarnings", inputUrl.getPath()
        );
    }

    /**
     * Create an json file output and validate that it parses as a correct json document.
     *
     * @throws Exception Throws an exception if the temp file can't be created.
     */
    @Test
    public void jsonFileTest() throws Exception
    {
        URL inputUrl = CommandLineTest.class.getResource("20-warning-tester");

        File tmpFile = File.createTempFile("test", ".json");
        runCommandLineTest(
                0, "--mode", "exp", inputUrl.getPath(),
                "-j", tmpFile.getAbsolutePath()
        );

        Object document = JSONValue.parse(new FileReader(tmpFile));
        assertNotNull("Incorrect json", document);

        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
    }


    /**
     * Create xml file output and validate that it parses as a correct XML document.
     *
     * @throws Exception Any parsing errors will be thrown as an exception.
     */
    @Test
    public void xmlFileTest() throws Exception
    {
        URL inputUrl = CommandLineTest.class.getResource("20-warning-tester");

        File tmpFile = File.createTempFile("test", ".xml");
        runCommandLineTest(
                0, "--mode", "exp", inputUrl.getPath(),
                "-o", tmpFile.getAbsolutePath()
        );

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.parse(tmpFile);

        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
    }

    /**
     * Create xmp file output and validate that it parses as a correct XML document.
     *
     * @throws Exception Any parsing errors will be thrown as an exception.
     */
    @Test
    public void xmpFileTest() throws Exception
    {
        URL inputUrl = CommandLineTest.class.getResource("20-warning-tester");

        File tmpFile = File.createTempFile("test", ".xmp");
        runCommandLineTest(
                0, "--mode", "exp", inputUrl.getPath(),
                "-x", tmpFile.getAbsolutePath()
        );

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.parse(tmpFile);

        if(tmpFile.exists())
        {
            tmpFile.delete();
        }
    }

    public static void runCommandLineTest(int expectedReturnCode, String... args)
    {
        int result = Integer.MAX_VALUE;
        try
        {
            Checker.main(args);
        }
        catch (NoExitSecurityManager.ExitException e)
        {
            result = e.status;
        }

        assertEquals("Return code", expectedReturnCode, result);
    }

}