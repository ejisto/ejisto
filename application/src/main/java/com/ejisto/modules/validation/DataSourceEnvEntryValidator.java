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

package com.ejisto.modules.validation;

import com.ejisto.modules.dao.entities.JndiDataSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class DataSourceEnvEntryValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return JndiDataSource.class.isAssignableFrom(clazz);
    }

    public void validateAll(List<JndiDataSource> entries, Errors errors) {
        for (JndiDataSource entry : entries) {
            validate(entry, errors);
        }
    }

    @Override
    public void validate(Object target, Errors errors) {
        validate((JndiDataSource) target, errors);
    }

    private void validate(JndiDataSource entry, Errors errors) {
        boolean res = checkFields(entry.getDriverClassName(), entry.getName(), entry.getDriverJarPath(),
                                  String.valueOf(entry.getMaxActive()), String.valueOf(entry.getMaxIdle()),
                                  String.valueOf(entry.getMaxWait()), entry.getPassword(), entry.getType(),
                                  entry.getUrl(), entry.getUsername(), entry.getDriverClassName());
        if (!res) {
            errors.rejectValue("dataSource", "datasource.env.entry.notvalid", new Object[]{entry.getName()}, "error");
        }
    }

    private boolean checkFields(String... fields) {
        for (String field : fields) {
            if (!isNotEmpty(field)) {
                return false;
            }
        }
        return true;
    }
}
