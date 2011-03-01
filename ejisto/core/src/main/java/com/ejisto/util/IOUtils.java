/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
 *
 * Ejisto is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ejisto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ejisto.util;

import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static ch.lambdaj.Lambda.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;

public class IOUtils {

    private static final FileExtensionFilter jarFilter = new FileExtensionFilter(FileExtensionFilter.ALL_JARS, true);
    private static final FileExtensionFilter classesFilter = new FileExtensionFilter(FileExtensionFilter.ALL_CLASSES, true);

    public static String copyFile(String filePath, File destDir) {
        try {
            File original = new File(filePath);
            File newFile = new File(destDir, original.getName());
            if (newFile.exists()) return newFile.getName();
            writeFile(new FileInputStream(original), destDir, original.getName());
            return original.getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFile(InputStream inputStream, File baseDir, String filename) throws IOException {
        File dest = new File(baseDir, filename);
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
            throw new IOException("Unable to write file " + dest.getAbsolutePath());
        byte[] data = new byte[1024];
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dest));
        int readed;
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
        ByteBuffer bb = ByteBuffer.allocate((int) ch.size());
        ch.read(bb);
        ch.close();
        fin.close();
        return bb.array();
    }

    public static String getFilenameWithoutExt(File file) {
        String filename = file.getName();
        return filename.substring(0, filename.lastIndexOf("."));
    }

    public static List<WebApplicationDescriptorElement> getClasspathEntries(String basePath, URL[] baseUrls) throws MalformedURLException {
        File base = new File(basePath);
        List<WebApplicationDescriptorElement> elements = new ArrayList<WebApplicationDescriptorElement>();
        for (URL url : baseUrls)
            elements.add(new WebApplicationDescriptorElement(url.getPath()));
        //elements.add(new File(base, "WEB-INF/classes/").toURI().toURL());
        elements.addAll(toWebApplicationDescriptorElement(getAllFiles(base, jarFilter)));
        return elements;
    }

    public static List<WebApplicationDescriptorElement> getClasspathEntries(String basePath) throws MalformedURLException {
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

    public static URL[] toUrlArray(WebApplicationDescriptor descriptor) throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        File base = new File(descriptor.getInstallationPath(), "WEB-INF");
        File classes = new File(base, "classes");
        File lib = new File(base, "lib");
        if (classes.exists()) urls.add(classes.toURI().toURL());
        for (WebApplicationDescriptorElement element : descriptor.getClassPathElements()) {
            urls.add(new File(lib, element.getPath()).toURI().toURL());
        }
        return urls.toArray(new URL[urls.size()]);
    }

    public static List<WebApplicationDescriptorElement> toWebApplicationDescriptorElement(List<File> in) throws MalformedURLException {
        if (in.isEmpty())
            return emptyList();
        List<WebApplicationDescriptorElement> elements = new ArrayList<WebApplicationDescriptorElement>(in.size());
        for (File file : in) {
            elements.add(new WebApplicationDescriptorElement(file.getName()));
        }
        return elements;
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

    public static boolean zipDirectory(String path, String destFilePath) {
        try {
            File src  = new File(path);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFilePath));
            zipDirectory(path, src, out);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void zipDirectory(String base, File src, ZipOutputStream out) throws IOException {
        if(src.isDirectory()) {
            for (File file : src.listFiles()) zipDirectory(base, file, out);
        } else {
            ZipEntry entry = new ZipEntry(src.getPath().substring(base.length()));
            out.putNextEntry(entry);
            out.write(readFile(src));
        }
    }

    public static String retrieveFilenameFromZipEntryPath(String filename) {
        return retrieveFilenameFromPath(filename, "/");
    }

    public static String retrieveFilenameFromPath(String filename, String fileSeparator) {
        return filename.substring(filename.lastIndexOf(fileSeparator) + 1);
    }

    public static String guessWebApplicationUri(WebApplicationDescriptor descriptor) {
        return new StringBuilder("http://localhost:1706").append(descriptor.getContextPath()).append("/").toString();
    }

}
