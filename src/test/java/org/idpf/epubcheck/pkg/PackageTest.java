package org.idpf.epubcheck.pkg;

import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.api.AbstractEpubCheckTest;
import org.junit.Test;

import java.util.Collections;

public class PackageTest extends AbstractEpubCheckTest
{
    public PackageTest()
    {
        super("/org/idpf/epubcheck/pkg/");
    }

    /**
     * This test will check that error is set if mimetype in the main package
     * has a incorrect value.
     */
    @Test
    public void validateEPUBMimetypeTest()
    {
        Collections.addAll(expectedErrors, MessageId.PKG_007);
        testValidateDocument("20-invalid-mimetype");
    }

}