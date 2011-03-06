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

package com.ejisto.modules.dao.entities.helper;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;

import java.util.Collection;
import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;

public class WebApplicationDescriptorHelper {
    private WebApplicationDescriptor descriptor;

    public WebApplicationDescriptorHelper(WebApplicationDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public boolean isBlacklistedEntry(String filename) {
        WebApplicationDescriptorElement element = selectFirst(getElements(), having(on(
                WebApplicationDescriptorElement.class).getPath(), equalTo(filename)));
        return element != null && element.isBlacklisted();
    }

    public void setBlacklist(List<String> blacklist) {
        forEach(select(getElements(), having(on(WebApplicationDescriptorElement.class).getPath(), isIn(blacklist))),
                WebApplicationDescriptorElement.class).blacklist();
        forEach(select(getElements(),
                       having(on(WebApplicationDescriptorElement.class).getPath(), not(isIn(blacklist)))),
                WebApplicationDescriptorElement.class).whitelist();
    }

    public List<String> getIncludedJars() {
        return extractProperty(getElements(), "path");
    }

    @SuppressWarnings("unchecked")
    public Collection<MockedField> getModifiedFields() {
        List<MockedField> fields = select(descriptor.getFields(),
                                          having(on(MockedField.class).getFieldValue(), notNullValue()));
        fields.addAll(select(descriptor.getFields(),
                             anyOf(having(on(MockedField.class).getFieldElementType(), notNullValue()),
                                   having(on(MockedField.class).getFieldValue(), notNullValue()),
                                   having(on(MockedField.class).getExpression(), notNullValue()))));
        return selectDistinct(fields, "comparisonKey");
    }

    private List<WebApplicationDescriptorElement> getElements() {
        return descriptor.getElements();
    }

}
