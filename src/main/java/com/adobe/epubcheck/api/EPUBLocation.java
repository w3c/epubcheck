package com.adobe.epubcheck.api;

import java.io.File;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.adobe.epubcheck.ocf.OCFContainer;
import com.adobe.epubcheck.opf.ValidationContext;
import com.adobe.epubcheck.util.JsonWriter;
import com.adobe.epubcheck.util.PathUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.mola.galimatias.URL;

public final class EPUBLocation implements Comparable<EPUBLocation>
{

  public static EPUBLocation of(ValidationContext context)
  {
    Preconditions.checkArgument(context != null, "context is null");
    return EPUBLocation.of(context.url, context.container.orNull());
  }

  public static EPUBLocation of(File file)
  {
    return EPUBLocation.of(URL.fromJavaURI(file.toURI()), null);
  }

  public static EPUBLocation of(URL url, OCFContainer container)
  {
    Preconditions.checkArgument(url != null, "URL is null");
    String path;
    if (container != null)
    {
      path = container.relativize(url);
    }
    else if ("file".equals(url.scheme()) && url.authority() == null)
    {
      path = PathUtil.removeWorkingDirectory(url.path());
    }
    else
    {
      path = url.toString();
    }
    return new EPUBLocation(url, path, -1, -1, null);
  }

  public EPUBLocation at(int line, int column)
  {
    return new EPUBLocation(url, path, line, column, context.orNull());
  }

  public EPUBLocation context(Object context)
  {
    return new EPUBLocation(url, path, line, column, context);
  }

  public final URL url;
  @JsonProperty
  public final String path;
  @JsonProperty
  public final int line;
  @JsonProperty
  public final int column;
  // FIXME 2022 - use String (possibly empty) instead of Optional<String>
  @JsonProperty
  @JsonSerialize(using = JsonWriter.OptionalJsonSerializer.class)
  public final Optional<String> context;

  private EPUBLocation(URL url, String path, int line, int column, Object context)
  {
    assert url != null;
    assert path != null;
    this.line = (line < 0) ? -1 : line;
    this.column = (column < 0) ? -1 : column;
    this.context = (context == null) ? Optional.absent() : Optional.of(context.toString());
    this.path = path;
    this.url = url;
  }

  public String getPath()
  {
    return this.path;
  }

  public int getLine()
  {
    return this.line;
  }

  public int getColumn()
  {
    return this.column;
  }

  public Optional<String> getContext()
  {
    return this.context;
  }

  @Override
  public String toString()
  {
    return path + "[" + line + "," + column + "]";
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == this)
    {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass())
    {
      return false;
    }

    EPUBLocation other = (EPUBLocation) obj;
    return !(this.getContext() == null && other.getContext() != null)
        && this.getPath().equals(other.getPath()) && this.getLine() == other.getLine()
        && this.getColumn() == other.getColumn()
        && (this.getContext() == null || this.getContext().equals(other.getContext()));
  }

  @Override
  public int compareTo(EPUBLocation o)
  {
    int comp = safeCompare(this.path, o.path);
    if (comp != 0)
    {
      return comp;
    }

    comp = line - o.line;
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }

    comp = column - o.column;
    if (comp != 0)
    {
      return comp < 0 ? -1 : 1;
    }
    comp = safeCompare(context.orNull(), o.context.orNull());
    if (comp != 0)
    {
      return comp;
    }

    return 0;
  }

  private int safeCompare(String a, String b)
  {
    if (a == null && b != null) return -1;
    if (a != null && b == null) return 1;
    if (a == null /* && b == null */) return 0;
    return a.compareTo(b);
  }
}
