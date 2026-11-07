package com.adobe.epubcheck.overlay;

import java.util.Map;

import com.google.common.base.Preconditions;

import io.mola.galimatias.URL;

import java.util.concurrent.ConcurrentHashMap;

public final class OverlayTextChecker
{

  private final Map<URL, String> docToOverlayMap = new ConcurrentHashMap<>();

  public boolean registerOverlay(URL contentDocURL, String overlayID)
  {
    Preconditions.checkArgument(contentDocURL != null);
    Preconditions.checkArgument(overlayID != null);
    String previous = docToOverlayMap.putIfAbsent(contentDocURL, overlayID);
    return previous == null || overlayID.equals(previous);
  }

  public boolean isReferencedByOverlay(URL contentDocURL)
  {
    if (contentDocURL == null)
    {
      return false;
    }
    return docToOverlayMap.containsKey(contentDocURL);
  }

  public boolean isCorrectOverlay(URL contentDocURL, String overlayID)
  {
    return overlayID.equals(docToOverlayMap.get(contentDocURL));
  }
}
