package org.w3c.epubcheck.test;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    tags = "@debug",
    features = "classpath:",
    strict = true,
    monochrome = true,
    plugin = { "pretty" })
public class DebugCucumberTest
{

}
