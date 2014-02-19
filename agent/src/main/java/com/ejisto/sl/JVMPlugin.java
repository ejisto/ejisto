package com.ejisto.sl;

import org.springsource.loaded.LoadtimeInstrumentationPlugin;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/14
 * Time: 3:28 PM
 */
public class JVMPlugin implements LoadtimeInstrumentationPlugin {

    private final ClassTransformer transformer;

    public JVMPlugin(ClassTransformer classTransformer) {
        transformer = classTransformer;
    }

    @Override
    public boolean accept(String slashedTypeName, ClassLoader classLoader, ProtectionDomain protectionDomain, byte[] bytes) {
        boolean accepted = initCompleted() && slashedTypeName != null &&
                transformer.isInstrumentableClass(toDottedClassName(slashedTypeName));
        if(slashedTypeName != null && slashedTypeName.startsWith("com/ejisto")) {
            System.out.printf("called accept for type %s, init complete: %s\n",
                              slashedTypeName, String.valueOf(initCompleted()));
        }
        return accepted;
    }

    @Override
    public byte[] modify(String slashedClassName, ClassLoader classLoader, byte[] bytes) {
        System.out.printf("transforming type %s\n", slashedClassName);
        try {
            return transformer.transform(classLoader, toDottedClassName(slashedClassName), null, null, bytes);
        } catch (IllegalClassFormatException e) {
            return bytes;
        }
    }

//    public static void initClassTransformer(ClassTransformer classTransformer) {
//        if(classTransformer != null) {
//            transformer = classTransformer;
//        }
//    }
//
//    public static void disableTransformer() {
//        transformer = null;
//    }

    private static boolean initCompleted() {
        return true;
        //return transformer != null;
    }

    private static String toDottedClassName(String slashedClassName) {
        return slashedClassName.replaceAll("/", ".");
    }
}
