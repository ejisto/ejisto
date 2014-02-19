package com.ejisto.sl;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/14
 * Time: 3:46 PM
 */
public interface ClassTransformer extends ClassFileTransformer {

    boolean isInstrumentableClass(String className);
}
