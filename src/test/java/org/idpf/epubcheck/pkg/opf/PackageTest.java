package org.idpf.epubcheck.pkg.opf;

import org.idpf.epubcheck.api.AbstractEpubCheckTest;
import org.junit.Test;

public class PackageTest extends AbstractEpubCheckTest
{
    public PackageTest()
    {
        super("/org/idpf/epubcheck/pkg/opf/");
    }

    /**
     * This test will validate that extra spaces either trailing or leading the
     * string of an unique ID should be acceptable.
     *
     * Look at issue 163
     */
    @Test
    public void ValidateUniqueIDWithSpacesTest()
    {
        testValidateDocument("20-valid-uid-with-spaces", "20-valid-uid-with-spaces.txt");
    }
}
