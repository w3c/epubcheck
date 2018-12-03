package org.idpf.epubcheck.pkg.cli;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.test.NoExitSecurityManager;
import com.adobe.epubcheck.test.CommonTestRunner;
import com.adobe.epubcheck.util.Messages;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.regex.Pattern;

public class CommandLineTest {

    private final Pattern usagePattern = Pattern.compile("^([\\s\\S]*\\n)?USAGE\\([\\s\\S]*$");
    private final Pattern warningPattern = Pattern.compile("^([\\s\\S]*\\n)?WARNING\\([\\s\\S]*$");
    private final Pattern errorPattern = Pattern.compile("^([\\s\\S]*\\n)?ERROR\\([\\s\\S]*$");

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    private SecurityManager originalManager;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private final Messages messages = Messages.getInstance();

    @Before
    public void setUp()
    {
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
        System.setSecurityManager(this.originalManager);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }


    /**
     * This test runs the program without any arguments, expected is a message about that
     * arguments are required.
     */
    @Test
    public void noArgumentsTest()
    {
        CommonTestRunner.runCustomTest("command_line", "empty", 1);
        Assert.assertEquals("Command output not as expected", messages.get("argument_needed"), errContent.toString().trim());
    }

    /**
     * Running with the question mark arguments, expected that we create some output where output contains
     * the version number.
     */
    @Test
    public void helpMessageTest()
    {
        CommonTestRunner.runCustomTest("command_line", "help", 1, true, "-?");
        Assert.assertEquals("Command output not as expected", messages.get("no_file_specified"), errContent.toString().trim());
        String expected = String.format(messages.get("help_text").replaceAll("[\\s]+", " "), EpubCheck.version());
        String actual = outContent.toString();
        actual = actual.replaceAll("[\\s]+", " ");
        Assert.assertTrue("Help output isn't as expected", actual.contains(expected));
    }

    /**
     * This test checks if multiple output formats are chosen, expected is an error message instructing
     * user to choose either output formats but not both.
     */
    @Test
    public void conflictingOutputTest()
    {
        CommonTestRunner.runCustomTest("command_line", "conflicting_output", 1, "-o", "foo.xml", "-j", "bar.json");
        Assert.assertEquals("Command output not as expected", messages.get("output_type_conflict"), errContent.toString().trim());
    }

    /**
     * Checks if the usage messages are shown when the usage flag is set.
     */
    @Test
    public void severitiesUsageTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        CommonTestRunner.runCustomTest(
                "severity", "severity_usage", 1,
                "-u", "--mode", "exp", inputUrl.getPath()
        );

        Assert.assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Warnings should be present", warningPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Usage should be present", usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * Checks if the warning messages are shown when the warning flag is set,
     * but no usage messages.
     */
    @Test
    public void severitiesWarningTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        CommonTestRunner.runCustomTest(
                "severity", "severity_warning", 1,
                "-w", "--mode", "exp", inputUrl.getPath()
        );
        Assert.assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Warnings should be present", warningPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * Checks if the error messages are shown when the error flag is set, but no usage or
     * warning messages.
     */
    @Test
    public void severitiesErrorTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        CommonTestRunner.runCustomTest(
                "severity", "severity_error", 1,
                "-e", "--mode", "exp", inputUrl.getPath()
        );

        Assert.assertTrue("Errors should be present", errorPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Warnings should not be present", !warningPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());
    }

    /**
     * Checks that no error, warning or usage messages are shown when fatal flag is set.
     */
    @Test
    public void severitiesFatalTest()
    {
        URL inputUrl = CommandLineTest.class.getResource("20-severity-tester");
        CommonTestRunner.runCustomTest(
                "severity", "severity_fatal", 0,
                "-f", "--mode", "exp", inputUrl.getPath()
        );
        Assert.assertTrue("Errors should not be present", !errorPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Warnings should not be present", !warningPattern.matcher(errContent.toString()).matches());
        Assert.assertTrue("Usage should not be present", !usagePattern.matcher(outContent.toString()).matches());
    }

}
