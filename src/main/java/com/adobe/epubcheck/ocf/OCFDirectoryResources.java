package com.adobe.epubcheck.ocf;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.adobe.epubcheck.util.FeatureEnum;
import com.google.common.collect.ImmutableMap;

import io.mola.galimatias.URL;

public class OCFDirectoryResources implements OCFResources
{

  private final Path root;

  public OCFDirectoryResources(URL rootURL) throws IOException
  {
    try
    {
      this.root = Paths.get(rootURL.toJavaURI());
    } catch (URISyntaxException e)
    {
      throw new IllegalArgumentException("Cannot convert the container URL to a path", e);
    }
    if (!Files.isDirectory(root))
    {
      throw new IllegalArgumentException("The container root must be a directory");
    }
  }

  @Override
  public Iterator<OCFResource> iterator()
  {
    Iterator<Path> paths;
    try
    {
      paths = Files.walk(root).iterator();
      paths.next(); // skip the root itself
    } catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return new Iterator<OCFResource>()
    {

      @Override
      public boolean hasNext()
      {
        return paths.hasNext();
      }

      @Override
      public OCFResource next()
        throws NoSuchElementException
      {
        final Path path = paths.next();
        try
        {

          final BasicFileAttributes attributes = Files.readAttributes(path,
              BasicFileAttributes.class,
              NOFOLLOW_LINKS);

          if (attributes.isSymbolicLink())
          {
            throw new UncheckedIOException(new IOException("symbolic links are disallowed"));
          }

          // Build the properties map
          final Map<FeatureEnum, String> properties = ImmutableMap.<FeatureEnum, String> builder()
              .put(FeatureEnum.SIZE, String.valueOf(attributes.size()))
              .put(FeatureEnum.COMPRESSED_SIZE, String.valueOf(attributes.size()))
              .put(FeatureEnum.COMPRESSION_METHOD, "Stored")
              .put(FeatureEnum.SHA_256, getSHAHash(path))
              .put(FeatureEnum.CREATION_DATE, attributes.creationTime().toString())
              .build();

          return new OCFResource()
          {

            @Override
            public InputStream openStream()
              throws IOException
            {
              return Files.newInputStream(path);
            }

            @Override
            public boolean isFile()
            {
              return attributes.isRegularFile();
            }

            @Override
            public boolean isDirectory()
            {
              return attributes.isDirectory();
            }

            @Override
            public Map<FeatureEnum, String> getProperties()
            {
              return properties;
            }

            @Override
            public String getPath()
            {
              return StreamSupport
                  .stream(root.relativize(path).spliterator(), false)
                  .map(Path::toString).collect(Collectors.joining("/"));
            }

            @Override
            public String toString()
            {
              return getPath();
            }
          };
        } catch (IOException e)
        {
          throw new UncheckedIOException(e);
        }
      }
    };
  }

  @Override
  public void close()
    throws IOException
  {
    // No-op
  }

  private static String getSHAHash(Path path)
  {

    try (InputStream inputStream = Files.newInputStream(path))
    {
      return OCFResources.getSHAHash(inputStream);
    } catch (IOException e)
    {
      return "";
    }
  }

}
