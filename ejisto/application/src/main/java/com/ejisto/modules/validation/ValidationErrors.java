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

package com.ejisto.modules.validation;

import org.springframework.validation.AbstractErrors;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

import static com.ejisto.util.GuiUtils.getMessage;

public class ValidationErrors extends AbstractErrors {
    private static final long serialVersionUID = -1318814109330110578L;
    private String name;

    public ValidationErrors(String name) {
        this.name = name;
    }

    List<String> errors = new ArrayList<String>();

    @Override
    public String getObjectName() {
        return name;
    }

    @Override
    public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
        errors.add(getMessage(errorCode, errorArgs));
    }

    @Override
    public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
        errors.add(getMessage(errorCode, errorArgs));
    }

    @Override
    public void rejectValue(String field, String errorCode, String defaultMessage) {
        errors.add(getMessage(errorCode));
    }

    @Override
    public void addAllErrors(Errors errors) {

    }

    @Override
    public List<ObjectError> getGlobalErrors() {
        return null;
    }

    @Override
    public List<FieldError> getFieldErrors() {
        return null;
    }

    @Override
    public Object getFieldValue(String field) {
        return null;
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

}
