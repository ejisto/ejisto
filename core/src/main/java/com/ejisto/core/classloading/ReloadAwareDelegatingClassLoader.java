package com.ejisto.core.classloading;

import com.ejisto.sl.ClassTransformer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/15/14
 * Time: 2:03 PM
 */
public class ReloadAwareDelegatingClassLoader extends ClassLoader {

    private final URL[] classesBasePath;
    private final AtomicReference<ClassLoader> actualClassLoaderContainer = new AtomicReference<>();
    private final ClassTransformer classTransformer;

    public ReloadAwareDelegatingClassLoader(ClassLoader parentLoader,
                                            String classesBasePath,
                                            ClassTransformer classTransformer) throws MalformedURLException {
        super(parentLoader);
        this.classesBasePath = new URL[] {Paths.get(classesBasePath).toUri().toURL()};
        this.actualClassLoaderContainer.set(createActualClassLoader());
        this.classTransformer = classTransformer;
    }



    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(classTransformer.isInstrumentableClass(name)) {
            return getActualClassLoader().loadClass(name);
        } else {
            return loadFromContextClassLoader(name);
        }
    }

    private Class<?> loadFromContextClassLoader(String name) throws ClassNotFoundException {
        return getParent().loadClass(name);
    }

    public void resetWebAppClassLoader() {
        actualClassLoaderContainer.set(createActualClassLoader());
    }

    private ClassLoader createActualClassLoader() {

        final ClassLoader parent = new ClassLoader() {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if(classTransformer.isInstrumentableClass(name)) {
                    throw new ClassNotFoundException("must be loaded from child classLoader!");
                }
                return loadFromContextClassLoader(name);
            }
        };

        return new URLClassLoader(this.classesBasePath, parent);
    }

    private ClassLoader getActualClassLoader() {
        return actualClassLoaderContainer.get();
    }

}
