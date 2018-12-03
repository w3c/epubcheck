package org.idpf.epubcheck.pkg.ncx;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.api.AbstractEpubCheckTest;
import org.junit.Test;

import java.util.Collections;

public class NavigationTest extends AbstractEpubCheckTest
{
    public NavigationTest()
    {
        super("/org/idpf/epubcheck/pkg/ncx/");
    }

    /**
     * This test checks that we find paths that point to a local directory
     * that is not present should be found.
     */
    @Test
    public void validateBadPathInNCXTest()
    {
        Collections.addAll(expectedErrors, MessageId.RSC_005);
        testValidateDocument("20-invalid-badpath");
    }

    /**
     * This test checks that a incorrect type should be flagged.
     * Type used in this test is 'body'
     */
    @Test
    public void validateBadNcxPageTargetTypeTest()
    {
        Collections.addAll(expectedErrors, MessageId.RSC_005);
        testValidateDocument("20-invalid-pagetarget");
    }

    /**
     * This is a test to check that we allow more than one entry in a epub navigation index.
     * Also check that the allowed guide elements are present.
     */
    @Test
    public void validateMultipleEntries()
    {
        testValidateDocument("20-valid-dual");
    }

}
