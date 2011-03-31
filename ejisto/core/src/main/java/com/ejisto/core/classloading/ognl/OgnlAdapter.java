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

package com.ejisto.core.classloading.ognl;

import com.ejisto.core.classloading.proxy.EjistoProxyFactory;
import com.ejisto.modules.dao.entities.MockedField;
import ognl.OgnlContext;
import ognl.OgnlRuntime;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

import static ognl.Ognl.compileExpression;
import static ognl.Ognl.setValue;

public class OgnlAdapter implements InitializingBean {
    private OgnlContext ognlContext;
    private EjistoProxyFactory ejistoProxyFactory;

    public OgnlAdapter(OgnlContext ognlContext, EjistoProxyFactory ejistoProxyFactory) {
        this.ognlContext = ognlContext;
        this.ejistoProxyFactory = ejistoProxyFactory;
    }

    public boolean canHandle(MockedField mockedField) {
        return !mockedField.isSimpleValue();
    }

    public void apply(Object target, List<MockedField> mockedFields) throws Exception {
        for (MockedField mockedField : mockedFields) apply(target, mockedField);
    }

    public void apply(Object target, MockedField mockedField) throws Exception {
        Object expression = compileExpression(ognlContext, target, mockedField.getExpression());
        setValue(expression, ognlContext, target, mockedField.getFieldValue());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        OgnlRuntime.setNullHandler(Object.class, ejistoProxyFactory);
    }
}
