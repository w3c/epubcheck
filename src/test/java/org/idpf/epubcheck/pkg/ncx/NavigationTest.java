package org.idpf.epubcheck.pkg.ncx;

import com.adobe.epubcheck.messages.MessageId;
import org.idpf.epubcheck.api.AbstractEpubCheckTest;
import org.junit.Test;

import java.util.Collections;

public class NavigationTest extends AbstractEpubCheckTest {
    public NavigationTest()
    {
        super("/org/idpf/epubcheck/pkg/ncx/");
    }

    /**
     * This test checks that we find paths that point to a local directory
     * that is not present should be found.
     */
    @Test
    public void ValidateBadPathInNCXTest()
    {
        Collections.addAll(expectedErrors, MessageId.RSC_005);
        testValidateDocument("20-invalid-badpath");
    }

    /**
     * This test checks that a incorrect type should be flagged.
     * Type used in this test is 'body'
     */
    @Test
    public void ValidateBadNcxPageTargetTypeTest()
    {
        Collections.addAll(expectedErrors, MessageId.RSC_005);
        testValidateDocument("20-invalid-pagetarget");
    }
}
