package com.adobe.epubcheck.bdd.musts;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.tool.EpubChecker;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class General_VisibleVersionNumber {
    private String response = null;

    @When("^I run the application with \\\"([^\\\"]*)\\\"$")
    public void i_run_the_application_with_flag_version(String argument) throws Throwable {
        PrintStream outOrig = System.out;
        PrintStream errOrig = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int i) throws IOException {}
        }));
        EpubChecker epubChecker = new EpubChecker();
        int result = epubChecker.run(new String[]{ argument });
        response = baos.toString("UTF-8");
        assertEquals(1, result);
        System.setOut(outOrig);
        System.setErr(errOrig);
    }

    @Then("^The version number should be clearly visible on the first line$")
    public void result_should_be_EpubCheck_v_version() throws Throwable {
        String firstLine = response.split("\n")[0];
        assertEquals(firstLine, "EpubCheck v" + EpubCheck.version());
    }
}