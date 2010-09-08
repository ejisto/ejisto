/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.util;

import static ch.lambdaj.Lambda.extractProperty;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.ejisto.core.jetty.WebApplicationDescriptor;

public class IOUtils {

    private static final FileExtensionFilter jarFilter = new FileExtensionFilter(FileExtensionFilter.ALL_JARS, true);
    private static final FileExtensionFilter classesFilter = new FileExtensionFilter(FileExtensionFilter.ALL_CLASSES, true);

    public static void writeFile(InputStream inputStream, File baseDir, String filename) throws IOException {
        File dest = new File(baseDir, filename);
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
            throw new IOException("Unable to write file " + dest.getAbsolutePath());
        byte[] data = new byte[1024];
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dest));
        int readed = 0;
        while (inputStream.available() > 0) {
            readed = inputStream.read(data);
            stream.write(data, 0, readed);
        }
        stream.flush();
        stream.close();
    }

    public static void writeFile(byte[] content, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        FileChannel ch = fos.getChannel();
        ch.write(ByteBuffer.wrap(content));
        ch.close();
        fos.close();
    }
    
    public static byte[] readFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        FileChannel ch = fin.getChannel();
        ByteBuffer bb = ByteBuffer.allocate((int)ch.size());
        ch.read(bb);
        ch.close();
        fin.close();
        return bb.array();
    }

    public static String getFilenameWithoutExt(File file) {
        String filename = file.getName();
        return filename.substring(0, filename.lastIndexOf("."));
    }

    public static URL[] getClasspathEntries(String basePath, URL[] baseUrls) throws MalformedURLException {
        File base = new File(basePath);
        List<URL> entries = new ArrayList<URL>();
        for (URL url : baseUrls)
            entries.add(url);
        entries.add(new File(base, "WEB-INF/classes/").toURI().toURL());
        entries.addAll(toUrl(getAllFiles(base, jarFilter)));
        return entries.toArray(new URL[entries.size()]);
    }

    public static URL[] getClasspathEntries(String basePath) throws MalformedURLException {
        return getClasspathEntries(basePath, getSystemClasspathEntries());
    }

    public static URL[] getSystemClasspathEntries() throws MalformedURLException {
        String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++)
            urls[i] = new File(paths[i]).toURI().toURL();
        return urls;
    }

    public static List<File> getAllFiles(File dir, FileExtensionFilter fileExtensionFilter) {
        List<File> files = new ArrayList<File>();
        if (!dir.exists() || !dir.isDirectory())
            return files;
        File[] children = dir.listFiles(fileExtensionFilter);
        for (File file : children) {
            if (file.isDirectory())
                files.addAll(getAllFiles(file, fileExtensionFilter));
            else
                files.add(file);
        }
        return files;
    }

    public static List<URL> toUrl(List<File> in) throws MalformedURLException {
        if (in.isEmpty())
            return emptyList();
        List<URL> urls = new ArrayList<URL>(in.size());
        for (File file : in) {
            urls.add(file.toURI().toURL());
        }
        return urls;
    }

    public static Collection<String> findAllWebApplicationClasses(String basePath, WebApplicationDescriptor descriptor) throws IOException {
        HashSet<String> ret = new HashSet<String>();
        File webInf = new File(basePath, "WEB-INF");
        ret.addAll(findAllClassNamesInDirectory(new File(webInf, "classes")));
        ret.addAll(findAllClassNamesInJarDirectory(new File(webInf, "lib"), descriptor));
        return ret;
    }

    public static Collection<String> findAllClassNamesInDirectory(File directory) {
        List<String> pathnames = extractProperty(select(getAllFiles(directory, classesFilter), having(on(File.class).isDirectory(), equalTo(false))), "path");
        HashSet<String> ret = new HashSet<String>();
        int index = directory.getAbsolutePath().length();
        for (String path : pathnames) {
            ret.add(translatePath(path.substring(index + 1, path.length() - 6)));
        }
        return ret;
    }

    public static Collection<String> findAllClassNamesInJarDirectory(File directory, WebApplicationDescriptor descriptor) throws IOException {
        List<File> jars = getAllFiles(directory, jarFilter);
        HashSet<String> ret = new HashSet<String>();
        for (File file : jars) {
            if (!descriptor.isBlacklistedEntry(file.getName()))
                ret.addAll(findAllClassesInJarFile(file));
        }
        return ret;
    }

    public static Collection<String> findAllClassesInJarFile(File jarFile) throws IOException {
        ZipFile jar = new ZipFile(jarFile);
        ZipEntry entry;
        ArrayList<String> ret = new ArrayList<String>();
        for (Enumeration<? extends ZipEntry> entries = jar.entries(); entries.hasMoreElements();) {
            entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                ret.add(translatePath(entry.getName().substring(0, entry.getName().length() - 6)));
            }
        }
        return ret;
    }

    public static String translatePath(String in) {
        String separator = File.separator;
        return in.replaceAll(Pattern.quote(separator), ".");
    }

    public static boolean deleteFile(File file) {
        if (!file.isDirectory())
            return file.delete();
        File[] children = file.listFiles();
        for (File child : children) {
            if (!deleteFile(child))
                return false;
        }
        return file.delete();
    }
    
    public static String retrieveFilenameFromZipEntryPath(String filename) {
        return retrieveFilenameFromPath(filename, "/");
    }
    
    public static String retrieveFilenameFromPath(String filename, String fileSeparator) {
        return filename.substring(filename.lastIndexOf(fileSeparator) + 1);
    }
    
    public static String determineWebApplicationUri(WebApplicationDescriptor descriptor) {
    	return new StringBuilder("http://localhost:9000").append(descriptor.getContextPath()).append("/").toString();
    }

}
