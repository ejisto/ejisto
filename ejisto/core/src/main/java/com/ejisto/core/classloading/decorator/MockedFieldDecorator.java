/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.core.classloading.decorator;

import com.ejisto.modules.dao.entities.ComplexValuesAware;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.Delegate;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.springframework.util.StringUtils;

import static com.ejisto.core.classloading.util.ReflectionUtils.detach;
import static com.ejisto.modules.repository.ClassPoolRepository.getRegisteredClassPool;

@ToString
@EqualsAndHashCode
@Log4j
public class MockedFieldDecorator implements MockedField {
    private static final String[] COMPLEX_TYPES = {"java.util.Collection", "java.util.Map"};
    @Delegate(excludes = ComplexValuesAware.class)
    private MockedField target;

    public MockedFieldDecorator(MockedField target) {
        this.target = target;
    }

    public MockedFieldDecorator() {
        this(new MockedFieldImpl());
    }

    @Override
    public boolean isSimpleValue() {
        CtClass clazz = null;
        CtClass targetClazz = null;
        try {
            ClassPool cp = getRegisteredClassPool(target.getContextPath());
            targetClazz = cp.get(target.getFieldType());
            for (String complexType : COMPLEX_TYPES) {
                clazz = cp.get(complexType);
                if (targetClazz.subtypeOf(clazz)) return false;
                clazz.detach();
            }
            return true;
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);//should never happens
        } catch (Exception e) {
            log.error("cannot check if field " + target.getFieldName() + " is simple type", e);
            return true;
        } finally {
            detach(clazz, targetClazz);
        }
    }

    @Override
    public String getCompleteDescription() {
        return new StringBuilder(getFieldName()).append(" [").append(getCompleteFieldType()).append("]: ").append(
                evaluateFieldValue()).toString();
    }

    @Override
    public String getCompleteFieldType() {
        if (StringUtils.hasText(target.getFieldElementType()))
            return target.getFieldType() + "<" + cleanFieldElementType(target.getFieldElementType()) + ">";
        return target.getFieldType();
    }

    private String cleanFieldElementType(String fieldElementType) {
        String[] path = fieldElementType.split("\\.");
        return path[path.length - 1];
    }

    private String evaluateFieldValue() {
        if (isSimpleValue()) return getFieldValue();
        return "**expression**";
    }

}
