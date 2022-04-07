package com.adobe.epubcheck.overlay;

import java.util.Map;

import com.google.common.base.Preconditions;

import io.mola.galimatias.URL;

import java.util.HashMap;

public final class OverlayTextChecker
{

  private final Map<URL, String> docToOverlayMap = new HashMap<>();

  public boolean registerOverlay(URL contentDocURL, String overlayID)
  {
    Preconditions.checkArgument(contentDocURL != null);
    Preconditions.checkArgument(overlayID != null);
    if (!docToOverlayMap.containsKey(contentDocURL))
    {
      docToOverlayMap.put(contentDocURL, overlayID);
      return true;
    }
    else
    {
      // TODO check if case must really be ignored
      return overlayID.equalsIgnoreCase(docToOverlayMap.get(contentDocURL));
    }
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
    // TODO check if case must really be ignored
    return overlayID.equalsIgnoreCase(docToOverlayMap.get(contentDocURL));
  }
}
