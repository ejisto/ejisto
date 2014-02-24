package com.ejisto.core.classloading;

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.repository.MockedFieldsRepository;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    public ClassReloader(ClassTransformerImpl transformer,
                         MockedFieldsRepository mockedFieldsRepository) {
        this.transformer = transformer;
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void run() {
        try {
            LOGGER.debug("trying to reload recently created fields");
            final List<MockedField> fields = mockedFieldsRepository.getRecentlyCreatedFields();
            if(fields.isEmpty()) {
                LOGGER.debug("no field has been created. Exiting...");
                return;
            }
            fields.stream().collect(groupingBy(MockedField::getClassName))
                    .forEach(this::processClass);
        } catch (Exception e) {
            LOGGER.error("got exception during class reloading", e);
        }
    }

    private void processClass(String className, List<MockedField> fields) {
        try {
            transformer.addMissingProperties(className, fields);
        } catch (NotFoundException | CannotCompileException | IOException e) {
            LOGGER.error("got exception during class processing", e);
        }
    }
}
