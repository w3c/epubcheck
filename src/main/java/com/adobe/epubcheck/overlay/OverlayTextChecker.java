package com.adobe.epubcheck.overlay;

import java.util.Map;
import java.util.HashMap;

public class OverlayTextChecker {

	private Map<String,String> refs;
	
    public OverlayTextChecker() {
    	refs = new HashMap<String,String>();
    }

    public boolean add(String ref, String overlay) {
      if (!refs.containsKey(ref)) {
        refs.put(ref, overlay);
        return true;
      }
      else if (!refs.get(ref).equalsIgnoreCase(overlay)) {
        return false;
      }
      return true;
    }
	
	public boolean isReferencedByOverlay(String path) {
      if (path == null || path.equals("")) {
        return false;
      }
      return refs.containsKey(path) ? true : false;
    }
	
	public boolean isCorrectOverlay(String path, String overlay) {
      return overlay.equalsIgnoreCase(refs.get(path)) ? true : false;
    }
}
