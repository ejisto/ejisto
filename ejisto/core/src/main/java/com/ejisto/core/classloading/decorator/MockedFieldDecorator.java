/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2011  Celestino Bellone
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

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.MockedFieldImpl;
import javassist.ClassPool;
import javassist.CtClass;
import org.springframework.util.StringUtils;

import static com.ejisto.core.classloading.util.ReflectionUtils.detach;
import static com.ejisto.modules.repository.ClassPoolRepository.getRegisteredClassPool;

public class MockedFieldDecorator implements MockedField {

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
            clazz = cp.get("java.util.Collection");
            targetClazz = cp.get(target.getFieldType());
            return !targetClazz.subtypeOf(clazz);
        } catch (Exception e) {
            return true;
        } finally {
            detach(clazz, targetClazz);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("MockedFieldDecorator[target: ").append(target.toString()).append("]").toString();
    }

    //delegated methods
    @Override
    public long getId() {
        return target.getId();
    }

    @Override
    public void setId(long id) {
        target.setId(id);
    }

    @Override
    public String getContextPath() {
        return target.getContextPath();
    }

    @Override
    public void setContextPath(String contextPath) {
        target.setContextPath(contextPath);
    }

    @Override
    public String getClassName() {
        return target.getClassName();
    }

    @Override
    public void setClassName(String className) {
        target.setClassName(className);
    }

    @Override
    public String getFieldName() {
        return target.getFieldName();
    }

    @Override
    public void setFieldName(String fieldName) {
        target.setFieldName(fieldName);
    }

    @Override
    public String getFieldType() {
        return target.getFieldType();
    }

    @Override
    public void setFieldType(String fieldType) {
        target.setFieldType(fieldType);
    }

    @Override
    public String getFieldValue() {
        return target.getFieldValue();
    }

    @Override
    public void setFieldValue(String fieldValue) {
        target.setFieldValue(fieldValue);
    }

    @Override
    public boolean isActive() {
        return target.isActive();
    }

    @Override
    public void setActive(boolean active) {
        target.setActive(active);
    }

    @Override
    public String getExpression() {
        return target.getExpression();
    }

    @Override
    public void setExpression(String expression) {
        target.setExpression(expression);
    }

    @Override
    public String getComparisonKey() {
        return target.getComparisonKey();
    }

    @Override
    public String getPackageName() {
        return target.getPackageName();
    }

    @Override
    public String getClassSimpleName() {
        return target.getClassSimpleName();
    }

    @Override
    public String getFieldElementType() {
        return target.getFieldElementType();
    }

    @Override
    public void setFieldElementType(String fieldElementType) {
        target.setFieldElementType(fieldElementType);
    }

    @Override
    public String getCompleteDescription() {
        return new StringBuilder(getFieldName()).append(" [").append(getCompleteFieldType()).append("]: ").append(getFieldValue())
                .toString();
    }

    @Override
    public String getCompleteFieldType() {
        if (StringUtils.hasText(target.getFieldElementType()))
            return target.getFieldType() + "<" + target.getFieldElementType() + ">";
        return target.getFieldType();
    }

}
