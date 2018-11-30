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

    @Test
    public void testValidateEPUBBadPathInNCX()
    {
        Collections.addAll(expectedErrors, MessageId.RSC_005);
        testValidateDocument("20-invalid-badpath");
    }

    @Test
    public void testValidateEPUBBadNcxPageTargetType()
    {
        Collections.addAll(expectedErrors, MessageId.RSC_005);
        testValidateDocument("20-invalid-pagetarget");
    }
}
