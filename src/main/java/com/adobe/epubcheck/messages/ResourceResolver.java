package com.adobe.epubcheck.messages;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import java.io.*;
import java.net.URL;
import java.util.*;

public class ResourceResolver {

    private static class MyPropertyResourceBundle extends PropertyResourceBundle {

        MyPropertyResourceBundle(Reader reader) throws IOException {
            super(reader);
        }

        MyPropertyResourceBundle(URL url) throws IOException {
            this(new BufferedReader(
                    new InputStreamReader(url.openStream(), Charsets.UTF_8)));
        }

        void setMyParent(MyPropertyResourceBundle resourceBundle){
            setParent(resourceBundle);
        }

    }

    private static final ResourceResolver INSTANCE = new ResourceResolver();

    public static ResourceResolver getInstance() {
        return INSTANCE;
    }

    private ResourceResolver() {
    }

    public List<URL> resource2Url(String resource, Locale locale) {
        String path = resource.replaceAll("\\.", "/");
        List<URL> result = flatResource2Url(path, locale);
        if (result.isEmpty()) {
            result = flatResource2Url("/" + path, locale);
        }
        if (result.isEmpty()) {
            result = flatResource2Url(resource, locale);
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("Can't find resource for " + resource + " and " + locale);
        }
        return result;
    }

    private List<URL> flatResource2Url(String resource, Locale locale) {
        final List<URL> result = new ArrayList<>();
        if (locale != null) {
            if (!"".equals(locale.getCountry())) {
                addTo(result, from(resource + "_" + locale.toString() + ".properties"));
            }
            final String lang = locale.getLanguage();
            if (!"".equals(lang)) {
                addTo(result, from(resource + "_" + locale.getLanguage() + ".properties"));
            }
        }
        // fallback to default locale (only if locale not found)
        Locale dft = Locale.getDefault();
        if (result.isEmpty()) {
            if (!"".equals(locale.getCountry())) {
                addTo(result, from(resource + "_" + dft.toString() + ".properties"));
            }
            final String lang2 = dft.getLanguage();
            if (!"".equals(lang2)) {
                addTo(result, from(resource + "_" + lang2 + ".properties"));
            }
        }
        // fallback to default bundle (unconditionally)
        addTo(result, from(resource + ".properties"));
        return result;
    }

    private static void addTo(List<URL> list, URL toAdd) {
        if (toAdd != null) {
            if (!list.contains(toAdd)) {
                list.add(toAdd);
            }
        }
    }

    private URL from(String resourceName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ResourceResolver.class.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl.getResource(resourceName);
    }

    public static PropertyResourceBundle toResourceBundle(List<URL> list) throws IOException {
        MyPropertyResourceBundle result = null;
        for (URL url : Lists.reverse(list)) {
            final MyPropertyResourceBundle last = result;
            result = new MyPropertyResourceBundle(url);
            if (last != null) {
                result.setMyParent(last);
            }
        }
        return result;
    }
}
