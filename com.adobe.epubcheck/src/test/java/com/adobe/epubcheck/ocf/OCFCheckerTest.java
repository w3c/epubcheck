/* 
  Copyright-Only Dedication (based on United States law)
  
  The person or persons who have associated their work with this
  document (the "Dedicators") hereby dedicate whatever copyright they
  may have in the work of authorship herein (the "Work") to the
  public domain.
  
  Dedicators make this dedication for the benefit of the public at
  large and to the detriment of Dedicators' heirs and successors.
  Dedicators intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights
  under copyright law, whether vested or contingent, in the Work.
  Dedicators understand that such relinquishment of all rights
  includes the relinquishment of all rights to enforce (by lawsuit
  or otherwise) those copyrights in the Work.
  
  Dedicators recognize that, once placed in the public domain, the
  Work may be freely reproduced, distributed, transmitted, used,
  modified, built upon, or otherwise exploited by anyone for any
  purpose, commercial or non-commercial, and in any way, including
  by methods that have not yet been invented or conceived.
*/

package com.adobe.epubcheck.ocf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.adobe.epubcheck.util.EPUBVersion;
import com.adobe.epubcheck.util.ValidationReport;

public class OCFCheckerTest
{

    private ValidationReport testOcfPackage( String fileName, EPUBVersion version )
    {
        OCFPackage ocf = new OCFMockPackage( fileName );

        ValidationReport testReport = new ValidationReport( fileName, String.format(
                "Package is being checked as ePub version %s", version.toString()));
        
        OCFChecker checker = new OCFChecker( ocf, testReport, version );
  
        checker.runChecks();
        
        return testReport;
    }

    /**
     * Not a test of the OCFChecker, just a sanity check to be sure the Mock Package
     * provider is working.
     */
    @Test
    public void invalidPath()
    {
        ValidationReport testReport = testOcfPackage( "/non-existent/", EPUBVersion.VERSION_2 );
        assertEquals(1, testReport.getErrorCount());
    }
    
    @Test
    public void testLoremBasic20()
    {
        ValidationReport testReport = testOcfPackage( "/20/expanded/valid/lorem/lorem-basic/", 
                            EPUBVersion.VERSION_2 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount())
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 2.0"));
        assertTrue(testReport.hasInfoMessage("[unique identifier] urn:uuid:550e8400-e29b-41d4-a716-4466674412314"));
    }        

    @Test
    public void testLoremBasic30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-basic/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
                || 0 != testReport.getExceptionCount() 
                || 0 != testReport.getWarningCount())
                System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremBasic30Against20()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-basic/", 
                            EPUBVersion.VERSION_2 );
        if (   0 == testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 1 != testReport.getWarningCount())
            System.out.println( testReport );
        assertTrue(testReport.getErrorCount()>0);
        assertEquals(1, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasWarningMessage("Validating the EPUB against version 2.0 but detected version 3.0."));
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
        assertTrue(testReport.hasInfoMessage("[unique identifier] urn:uuid:550e8400-e29b-41d4-a716-4466674412314"));
    } 
    
    @Test
    public void testLoremBasic20Against30()
    {
    	ValidationReport testReport = testOcfPackage( "/20/expanded/valid/lorem/lorem-basic/", 
    			EPUBVersion.VERSION_3 );
    	if (   0 == testReport.getErrorCount() 
    			|| 0 != testReport.getExceptionCount() 
    			|| 1 != testReport.getWarningCount())
    		System.out.println( testReport );
    	assertTrue(testReport.getErrorCount()>0);
    	assertEquals(1, testReport.getWarningCount());
    	assertEquals(0, testReport.getExceptionCount());
    	assertTrue(testReport.hasWarningMessage("Validating the EPUB against version 3.0 but detected version 2.0."));
        assertTrue(testReport.hasInfoMessage("[format version] 2.0"));
        assertTrue(testReport.hasInfoMessage("[unique identifier] urn:uuid:550e8400-e29b-41d4-a716-4466674412314"));
    } 
    
    @Test
    public void testLoremBasic30Switch()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-basic-switch/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremAudio30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-audio/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremBindings30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-bindings/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremForeign30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-foreign/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremLink30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-link/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremFallbacks30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-object-fallbacks/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremPoster30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-poster/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremSVG30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-svg/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremHyperlink30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-svg-hyperlink/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        

    @Test
    public void testLoremWasteland30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/valid/wasteland-basic/", 
                                            EPUBVersion.VERSION_3 );
        if (   0 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertEquals(0, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }        
    
    
    @Test
    public void testLoremMultipleRenditions20()
    {
    	ValidationReport testReport = testOcfPackage( "/20/expanded/valid/lorem-xrenditions-2ops/", 
    			EPUBVersion.VERSION_2 );
    	if (   0 != testReport.getErrorCount() 
    			|| 0 != testReport.getExceptionCount() 
    			|| 1 != testReport.getWarningCount())
    		System.out.println( testReport );
    	assertEquals(0, testReport.getErrorCount());
    	assertEquals(1, testReport.getWarningCount());
    	assertEquals(0, testReport.getExceptionCount());
    }
    
    @Test
    public void testLoremMultipleRenditionsSingleOPF20()
    {
    	ValidationReport testReport = testOcfPackage( "/20/expanded/valid/lorem-xrenditions-1ops/", 
    			EPUBVersion.VERSION_2 );
    	if (   0 != testReport.getErrorCount() 
    			|| 0 != testReport.getExceptionCount() 
    			|| 0 != testReport.getWarningCount())
    		System.out.println( testReport );
    	assertEquals(0, testReport.getErrorCount());
    	assertEquals(0, testReport.getWarningCount());
    	assertEquals(0, testReport.getExceptionCount());
    }
    
    @Test
    public void testLoremMultipleRenditions30()
    {
    	ValidationReport testReport = testOcfPackage( "/30/expanded/valid/lorem-xrenditions/", 
    			EPUBVersion.VERSION_3 );
    	if (   0 != testReport.getErrorCount() 
    			|| 0 != testReport.getExceptionCount() 
    			|| 0 != testReport.getWarningCount())
    		System.out.println( testReport );
    	assertEquals(0, testReport.getErrorCount());
    	assertEquals(0, testReport.getWarningCount());
    	assertEquals(0, testReport.getExceptionCount());
    	assertTrue(testReport.hasInfoMessage("[EPUB renditions count] 2"));
    }

    // The following tests should all fail, as they point to invalid ePubs
    @Test
    public void testInvalidLoremBasic30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-basic-switch/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertTrue( testReport.errorList.get( 0 ).message.contains( 
                "This file should declare in opf the property: mathml" ));
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }

    @Test
    public void testInvalidLoremBindings30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-bindings/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount())
            System.out.println( testReport );
        assertTrue( testReport.errorList.get( 0 ).message.contains( 
                "Object element doesn't provide fallback" ));
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
   }

    @Test
    public void testInvalidLoremForeign30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-foreign/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertTrue( testReport.errorList.get( 0 ).message.contains( 
                "This file should declare in opf the property: remote-resources" ));
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }

    @Test
    public void testInvalidLoremMimetype30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-mimetype/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );
        assertTrue( testReport.errorList.get( 0 ).message.contains( 
                "Mimetype file should contain only the string \"application/epub+zip\"." ));
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }

    @Test
    public void testInvalidLoremPoster30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-poster/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount()
            )
            System.out.println( testReport );

        assertTrue( testReport.errorList.get( 0 ).message.contains( 
                "Video poster must have core media image type" ) );
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }

    @Test
    public void testInvalidLoremRNG30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-xhtml-rng-1/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount())
                System.out.println( testReport );
        assertTrue( testReport.errorList.get( 0 ).message.contains( 
            "element \"epub:x\" not allowed here" ));
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }

    @Test
    public void testInvalidLoremSCH30()
    {
        ValidationReport testReport = testOcfPackage( "/30/expanded/invalid/lorem-xhtml-sch-1/", 
                                            EPUBVersion.VERSION_3 );
        if (   1 != testReport.getErrorCount() 
            || 0 != testReport.getExceptionCount() 
            || 0 != testReport.getWarningCount())
                System.out.println( testReport );
        assertTrue( testReport.errorList.get( 0 ).message.contains( 
                "The dfn element must not appear inside dfn elements" ));
        assertEquals(1, testReport.getErrorCount());
        assertEquals(0, testReport.getWarningCount());
        assertEquals(0, testReport.getExceptionCount());
        assertTrue(testReport.hasInfoMessage("[format version] 3.0"));
    }

}
