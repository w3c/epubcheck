package org.idpf.epubcheck.pkg.opf;

import org.idpf.epubcheck.api.AbstractEpubCheckTest;
import org.junit.Test;

public class PackageTest extends AbstractEpubCheckTest {
    public PackageTest()
    {
        super("/org/idpf/epubcheck/pkg/opf/");
    }

    @Test
    public void testValidateEPUBUidSpaces()
    {
        // ascertain that leading/trailing space in 2.0 id values is accepted
        // issue 163
        testValidateDocument("20-valid-uid-with-spaces", "20-valid-uid-with-spaces.txt");
    }
}
