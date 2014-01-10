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

package com.ejisto.modules.dao.entities.helper;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.dao.entities.WebApplicationDescriptorElement;

import java.util.*;
import java.util.stream.Collectors;

public class WebApplicationDescriptorHelper {
    private WebApplicationDescriptor descriptor;

    public WebApplicationDescriptorHelper(WebApplicationDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public boolean isBlacklistedEntry(String filename) {
        return getElements().stream().anyMatch(x -> x.getPath().equals(filename) && x.isBlacklisted());
    }

    public void setBlacklist(List<String> blacklist) {
        getElements().stream().filter(x -> blacklist.contains(x.getPath()))
                .forEach(WebApplicationDescriptorElement::blacklist);
        getElements().stream().filter(x -> !blacklist.contains(x.getPath()))
                .forEach(WebApplicationDescriptorElement::whitelist);
    }

    public List<String> getIncludedJars() {
        return getElements().stream().map(WebApplicationDescriptorElement::getPath).collect(Collectors.toList());
    }

    public Collection<MockedField> getModifiedFields() {
        Set<MockedField> result = new TreeSet<>((x, y) -> x.getComparisonKey().compareTo(y.getComparisonKey()));
        descriptor.getFields().stream()
                .filter(f -> f.getFieldValue() != null || f.getFieldElementType() != null || f.getExpression() != null)
                .forEach(result::add);
        return result;
    }

    private List<WebApplicationDescriptorElement> getElements() {
        return descriptor.getElements();
    }

}
