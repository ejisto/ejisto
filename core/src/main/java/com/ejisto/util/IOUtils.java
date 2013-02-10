/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2013 Celestino Bellone
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

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;
import com.ejisto.modules.repository.SettingsRepository;
import com.ejisto.util.visitor.CopyFileVisitor;
import com.ejisto.util.visitor.PrefixBasedCopyFileVisitor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static ch.lambdaj.Lambda.*;
import static com.ejisto.constants.StringConstants.*;
import static com.ejisto.util.visitor.PrefixBasedCopyFileVisitor.CopyType.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.hamcrest.Matchers.equalTo;

@Log4j
public final class IOUtils {

    public static final String COPIED_FILES_PREFIX = "ejisto__";
    private static final FileExtensionFilter jarFilter = new FileExtensionFilter(FileExtensionFilter.ALL_JARS, true);
    private static final String[] jarExtension = new String[]{"jar"};
    private static final String[] classExtension = new String[]{"class"};

    public static String copyFile(String filePath, File destDir) {
        File original = new File(filePath);
        return copyFile(original, destDir);
    }

    public static String copyFile(File original, File destDir) {
        try {
            File copy = new File(destDir, original.getName());
            FileUtils.copyFile(original, copy);
            return copy.getName();
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    public static void copyFullDirContent(String srcPath, String targetPath) {
        copyDirContent(new File(srcPath), new File(targetPath), null, null, INCLUDE_ALL);
    }

    public static void copyFilteredDirContent(File srcDir, File targetDir, String[] prefixes, String copiedFilesPrefix) {
        copyDirContent(srcDir, targetDir, prefixes, copiedFilesPrefix, INCLUDE_ONLY_MATCHING_RESOURCES);
    }

    public static void copyDirContentExcludingMatchingFiles(File srcDir, File targetDir, String[] prefixes) {
        copyDirContent(srcDir, targetDir, prefixes, null, EXCLUDE_MATCHING_RESOURCES);
    }

    private static void copyDirContent(File srcDir, File targetDir, String[] prefixes, final String copiedFilesPrefix,
                                       PrefixBasedCopyFileVisitor.CopyType copyType) {
        try {
            final Path srcRoot = srcDir.toPath();
            final Path targetRoot = targetDir.toPath();
            final List<String> prefixesList = prefixes != null ? asList(prefixes) : Collections.<String>emptyList();
            Files.walkFileTree(srcRoot,
                               new PrefixBasedCopyFileVisitor(
                                       new PrefixBasedCopyFileVisitor.CopyOptions(srcRoot, targetRoot, prefixesList,
                                                                                  copiedFilesPrefix, copyType)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFile(InputStream inputStream, File baseDir, String filename) throws IOException {
        File out = new File(baseDir, filename);
        if (!out.getParentFile().exists() && !out.getParentFile().mkdirs()) {
            throw new IOException("Unable to write file " + out.getAbsolutePath());
        }
        FileOutputStream outputStream = new FileOutputStream(out);
        org.apache.commons.io.IOUtils.copy(inputStream, outputStream);
        org.apache.commons.io.IOUtils.closeQuietly(outputStream);
    }

    public static byte[] readFile(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static String readInputStream(InputStream inputStream, String characterSet) throws IOException {
        byte[] read = org.apache.commons.io.IOUtils.toByteArray(inputStream);
        return new String(read, Charset.forName(characterSet));
    }

    public static String getFilenameWithoutExt(File file) {
        return getBaseName(file.getName());
    }

    public static List<WebApplicationDescriptorElement> getClasspathEntries(String basePath, URL[] baseUrls) throws MalformedURLException {
        File base = new File(basePath);
        List<WebApplicationDescriptorElement> elements = new ArrayList<>();
        for (URL url : baseUrls) {
            elements.add(new WebApplicationDescriptorElement(url.getPath()));
        }
        elements.addAll(toWebApplicationDescriptorElement(getAllFiles(base, jarExtension)));
        return elements;
    }

    public static List<WebApplicationDescriptorElement> getClasspathEntries(String basePath) throws MalformedURLException {
        return getClasspathEntries(basePath, getSystemClasspathEntries());
    }

    public static URL[] getSystemClasspathEntries() throws MalformedURLException {
        String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            urls[i] = new File(paths[i]).toURI().toURL();
        }
        return urls;
    }

    public static URL[] addServerLibs(URL[] entries, String serverLibDir) throws MalformedURLException {
        Collection<File> jars = getAllFiles(new File(serverLibDir), new String[]{"jar"});
        URL[] ret = new URL[entries.length + jars.size()];
        System.arraycopy(entries, 0, ret, 0, entries.length);
        int pos = entries.length;
        for (File jar : jars) {
            ret[pos++] = jar.toURI().toURL();
        }
        return ret;
    }

    private static Collection<File> getAllFiles(File dir, String[] extensions) {
        if (!dir.exists() || !dir.isDirectory()) {
            return emptyList();
        }
        return FileUtils.listFiles(dir, extensions, true);
    }

    public static List<String> listJarFiles(String dir) {
        List<String> files = new ArrayList<>();
        for (File file : getAllFiles(new File(dir), jarExtension)) {
            files.add(file.getAbsolutePath());
        }
        return files;
    }

    public static URL[] toUrlArray(WebApplicationDescriptor descriptor) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        File base = new File(descriptor.getInstallationPath(), "WEB-INF");
        File classes = new File(base, "classes");
        File lib = new File(base, "lib");
        if (classes.exists()) {
            urls.add(classes.toURI().toURL());
        }
        for (WebApplicationDescriptorElement element : descriptor.getClassPathElements()) {
            urls.add(new File(lib, element.getPath()).toURI().toURL());
        }
        return urls.toArray(new URL[urls.size()]);
    }

    public static List<WebApplicationDescriptorElement> toWebApplicationDescriptorElement(Collection<File> in) {
        if (in.isEmpty()) {
            return emptyList();
        }
        List<WebApplicationDescriptorElement> elements = new ArrayList<>(in.size());
        for (File file : in) {
            elements.add(new WebApplicationDescriptorElement(file.getName()));
        }
        return elements;
    }

    public static Collection<String> findAllWebApplicationClasses(String basePath, WebApplicationDescriptor descriptor) throws IOException {
        HashSet<String> ret = new HashSet<>();
        File webInf = new File(basePath, "WEB-INF");
        ret.addAll(findAllClassNamesInDirectory(new File(webInf, "classes")));
        ret.addAll(findAllClassNamesInJarDirectory(new File(webInf, "lib"), descriptor));
        return ret;
    }

    public static Collection<String> findAllClassNamesInDirectory(File directory) {
        List<String> pathNames = extractProperty(
                select(getAllFiles(directory, classExtension), having(on(File.class).isDirectory(), equalTo(false))),
                "path");
        HashSet<String> ret = new HashSet<>();
        int index = directory.getAbsolutePath().length();
        for (String path : pathNames) {
            ret.add(translatePath(path.substring(index + 1, path.length() - 6)));
        }
        return ret;
    }

    public static Collection<String> findAllClassNamesInJarDirectory(File directory, WebApplicationDescriptor descriptor) throws IOException {
        HashSet<String> ret = new HashSet<>();
        for (File file : getAllFiles(directory, jarExtension)) {
            if (!descriptor.isBlacklistedEntry(file.getName())) {
                ret.addAll(findAllClassesInJarFile(file));
            }
        }
        return ret;
    }

    public static Collection<String> findAllClassesInJarFile(File jarFile) throws IOException {
        ZipFile jar = new ZipFile(jarFile);
        ZipEntry entry;
        ArrayList<String> ret = new ArrayList<>();
        for (Enumeration<? extends ZipEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
            entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                ret.add(translatePath(entry.getName().substring(0, entry.getName().length() - 6), "/"));
            }
        }
        return ret;
    }

    public static String translatePath(String in, String separator) {
        return in.replaceAll(Pattern.quote(separator), ".");
    }

    public static String translatePath(String in) {
        return translatePath(in, File.separator);
    }

    public static boolean deleteFile(String path) {
        return deleteFile(new File(path));
    }

    public static boolean deleteFile(File file) {
        if (!file.isDirectory()) {
            return FileUtils.deleteQuietly(file);
        }
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static boolean emptyDir(File file) {
        Path directory = file.toPath();
        if(!Files.isDirectory(directory)) {
            return true;
        }
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if(exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                    return FileVisitResult.TERMINATE;
                }
            });
        } catch (IOException e) {
            IOUtils.log.error(format("error while trying to empty the directory %s", directory.toString()), e);
            return false;
        }
        return true;
    }

    public static void zipDirectory(File src, String outputFilePath) throws IOException {
        Path out = Paths.get(outputFilePath);
        if (Files.exists(out)) {
            Files.delete(out);
        }
        String filePath = out.toUri().getPath();
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem targetFs = FileSystems.newFileSystem(URI.create("jar:file:" + filePath), env)) {
            Files.walkFileTree(src.toPath(), new CopyFileVisitor(src.toPath(), targetFs.getPath("/")));
        }
    }

    public static void unzipFile(File src, String outputDirectory) throws IOException {
        Path out = Paths.get(outputDirectory);
        if (!Files.exists(out)) {
            Files.createDirectories(out);
        }
        Assert.isTrue(Files.isDirectory(out));
        try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("jar:file:" + src.getAbsolutePath()),
                                                               Collections.<String, Object>emptyMap())) {
            final Path srcRoot = fileSystem.getPath("/");
            Files.walkFileTree(srcRoot, new CopyFileVisitor(srcRoot, out));
        }
    }

    public static String retrieveFilenameFromPath(String filename) {
        return getName(filename);
    }

    public static String guessWebApplicationUri(WebApplicationDescriptor descriptor) {
        return guessWebApplicationUri(descriptor.getContextPath());
    }

    public static String guessWebApplicationUri(String contextPath) {
        return format("http://localhost:%s%s/",
                      SettingsRepository.getInstance().getSettingValue(DEFAULT_SERVER_PORT), contextPath);
    }

    public static int findFirstAvailablePort(int startPort) {
        for (int port = startPort, max = port + 100; port < max; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        throw new RuntimeException("Unable to find a port");
    }

    public static boolean isPortAvailable(int port) {
        try (DatagramSocket udp = new DatagramSocket(port);
             ServerSocket tcp = new ServerSocket(port)) {
            tcp.setReuseAddress(true);
            udp.setReuseAddress(true);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static void copyEjistoLibs(String[] prefixes, File targetDir) {
        StringBuilder path = new StringBuilder(System.getProperty("user.dir"));
        path.append(File.separator);
        path.append("lib");
        copyFilteredDirContent(new File(path.toString()), targetDir, prefixes, COPIED_FILES_PREFIX);
    }

    public static String getEjistoCoreClasspathEntry() {
        StringBuilder path = new StringBuilder(System.getProperty("user.dir"));
        path.append(File.separator);
        path.append("lib");
        path.append(File.separator);
        path.append("ejisto-core-");
        path.append(SettingsRepository.getInstance().getSettingValue(EJISTO_VERSION));
        path.append(".jar");
        return path.toString();
    }

    public static URL fileToUrl(File f) {
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    public static String getHttpInterfaceAddress() throws IOException {
        return format("http://%s:%s", getLocalAddress(), System.getProperty(HTTP_LISTEN_PORT.getValue()));
    }

}
