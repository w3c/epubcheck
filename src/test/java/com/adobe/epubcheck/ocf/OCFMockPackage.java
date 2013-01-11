package com.adobe.epubcheck.ocf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;

public class OCFMockPackage extends OCFPackage
{
    HashSet<String> dirEntries, mockEntries;
    File containerFile;
    int offset;
    
    public OCFMockPackage( String containerPath )
    {
    	URL containerURL = this.getClass().getResource(containerPath);
        containerFile = new File( containerURL!=null?containerURL.getPath():containerPath );
        offset = containerFile.getPath().length()+1;
        dirEntries = new HashSet<String>();
        mockEntries = new HashSet<String>();
        addEntries( containerFile );
    }
    
    
    private void addEntries( File parent )
    {
        File files[] = parent.listFiles();
        if (null != files) for (File file:files )
        {
            if (file.isFile())
                mockEntries.add( file.getPath().substring( offset ).replace(  '\\', '/' ) );
            else if (file.isDirectory() && !file.getName().startsWith( "." ))
            {
                dirEntries.add( file.getPath().substring( offset ).replace( '\\', '/' ) );
                addEntries( file );
            }
        }
    }
    
    @Override
    public boolean hasEntry( String name )
    {
        File entry = new File( containerFile, name );
        return entry.exists();
    }

    @Override
    public long getTimeEntry(String name) {
        return 1348240407L;
    }
    
    @Override
    public InputStream getInputStream( String name ) throws IOException
    {
        if (hasEntry( name ))
        {
            FileInputStream fis = new FileInputStream( new File( containerFile, name ));
            return fis;
        }
        else
        {
            throw new IOException( "An unknown file, " + name + " was requested" );
        } 
    }

    @Override
    public HashSet<String> getFileEntries() throws IOException
    {
        return mockEntries;
    }

    @Override
    public HashSet<String> getDirectoryEntries() throws IOException
    {
        return dirEntries;
    }

}
