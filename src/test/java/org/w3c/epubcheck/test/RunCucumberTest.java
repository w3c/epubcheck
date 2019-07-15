package org.w3c.epubcheck.test;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "classpath:",
    strict = true,
    monochrome = true,
    plugin = { "pretty" })
public class RunCucumberTest
{

}
