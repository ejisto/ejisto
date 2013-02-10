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

package com.ejisto.util.converter;

import ch.lambdaj.function.convert.Converter;
import com.ejisto.modules.dao.entities.MockedField;

import static java.lang.String.format;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 1/15/11
 * Time: 6:46 PM
 */
public class MockedFieldDumpConverter implements Converter<MockedField, String> {

    private static final String DUMP = "INSERT INTO MOCKEDFIELDS(CONTEXTPATH,CLASSNAME,FIELDNAME,FIELDTYPE,FIELDVALUE,EXPRESSION,FIELDELEMENTTYPE, ACTIVE) VALUES('%s','%s','%s','%s',%s,%s,%s,%s);";

    @Override
    public String convert(MockedField field) {
        return format(DUMP, field.getContextPath(), field.getClassName(), field.getFieldName(), field.getFieldType(),
                      escapeRaw(field.getFieldValue()), escapeRaw(field.getExpression()),
                      escapeRaw(field.getFieldElementType()), field.isActive() ? "1" : "0");
    }

    private String escapeRaw(String in) {
        return in == null ? null : "'" + in.replaceAll("'", "''") + "'";
    }

}
