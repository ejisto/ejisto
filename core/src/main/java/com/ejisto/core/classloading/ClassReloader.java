package com.ejisto.core.classloading;

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.modules.web.util.MockedFieldsJSONUtil;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springsource.loaded.ReloadableType;
import org.springsource.loaded.TypeDescriptor;
import org.springsource.loaded.TypeRegistry;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ejisto.constants.StringConstants.EJISTO_CLASS_TRANSFORMER_CATEGORY;
import static java.util.stream.Collectors.groupingBy;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/23/14
 * Time: 9:19 PM
 */
public class ClassReloader implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(EJISTO_CLASS_TRANSFORMER_CATEGORY.getValue());
    private final ClassTransformerImpl transformer;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final Queue<MockedField> pendingFields = new ConcurrentLinkedQueue<>();
    private final boolean inMemoryReload;
    private final Consumer<CtClass> ctClassConsumer;

    public ClassReloader(ClassTransformerImpl transformer,
                         MockedFieldsRepository mockedFieldsRepository,
                         boolean inMemoryReload,
                         String classOutputPath) {
        this.transformer = transformer;
        this.mockedFieldsRepository = mockedFieldsRepository;
        this.inMemoryReload = inMemoryReload;
        if(inMemoryReload) {
            Objects.requireNonNull(classOutputPath);
        }
        ctClassConsumer = initClassConsumer(classOutputPath);
    }

    @Override
    public void run() {
        try {
            final TypeRegistry typeRegistry = TypeRegistry.getTypeRegistryFor(
                    Thread.currentThread().getContextClassLoader());
            final List<MockedField> fields = mockedFieldsRepository.getRecentlyCreatedFields();
            fields.addAll(getAllPendingFields());
            if(fields.isEmpty()) {
                return;
            }
            fields.stream()
                    .map(f -> Pair.of(f.getClassName(), f.getContextPath()))
                    .distinct()
                    .forEach(p -> processClass(p.getLeft(), p.getRight(), typeRegistry));
        } catch (Exception e) {
            LOGGER.error("got exception during class reloading", e);
        }
    }

    private void processClass(String className, String contextPath, TypeRegistry typeRegistry) {
        try {
            final List<MockedField> fields = mockedFieldsRepository.load(contextPath, className);
            if(inMemoryReload) {
                LOGGER.trace("inMemoryReload active, triggering SpringLoaded reload process");
                reloadDirectly(className, typeRegistry, fields);
            } else {
                LOGGER.trace("inMemoryReload turned off, writing file to the disk");
                transformer.addMissingProperties(null, className, fields, ctClassConsumer);
            }
        } catch (NotFoundException | CannotCompileException | IOException e) {
            LOGGER.error("got exception during class processing", e);
        }
    }

    private void reloadDirectly(String className, TypeRegistry typeRegistry, List<MockedField> fields) throws CannotCompileException, NotFoundException, IOException {
        final ReloadableType reloadableType = typeRegistry.getReloadableType(className.replaceAll("\\.", "/"), true);
        if(reloadableType != null) {
            final byte[] newVersion = transformer.addMissingProperties(reloadableType.getBytesLoaded(), className, fields);
            tryToReloadClass(reloadableType, className, newVersion);
        } else {
            pendingFields.offer(fields.get(0));
        }
    }

    private void tryToReloadClass(ReloadableType reloadableType, String className, byte[] newVersion) {
        LOGGER.info(String.format("reloading type %s", className));
        reloadableType.loadNewVersion(newVersion);
    }

    private List<MockedField> getAllPendingFields() {
        List<MockedField> fields = new ArrayList<>();
        MockedField pending;
        while((pending = pendingFields.poll()) != null) {
            fields.add(pending);
        }
        return fields;
    }

    private static Consumer<CtClass> initClassConsumer(String classOutputPath) {
        return clazz -> {
            try {
                clazz.writeFile(classOutputPath);
            } catch (CannotCompileException | IOException e) {
                LOGGER.error("unable to write class file", e);
            }
        };
    }
}
