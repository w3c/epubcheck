package org.idpf.epubcheck.pkg;

import com.adobe.epubcheck.api.EPUBProfile;
import com.adobe.epubcheck.messages.MessageId;
import org.idpf.epubcheck.api.AbstractEpubCheckTest;
import org.junit.Test;

import java.util.Collections;

public class PackageTest extends AbstractEpubCheckTest {
    public PackageTest()
    {
        super("/org/idpf/epubcheck/pkg/");
    }

    @Test
    public void testValidateEPUBMimetype()
    {
        Collections.addAll(expectedErrors, MessageId.PKG_007);
        testValidateDocument("20-invalid-mimetype");
    }

}