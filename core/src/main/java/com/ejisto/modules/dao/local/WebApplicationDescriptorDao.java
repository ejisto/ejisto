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

package com.ejisto.modules.dao.local;

import com.ejisto.modules.dao.entities.WebApplicationDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.equalTo;

public class WebApplicationDescriptorDao extends BaseLocalDao {

    public WebApplicationDescriptor load(String contextPath) {
        return internalLoad(contextPath, getDatabase().getWebApplicationDescriptors());
    }

    public List<WebApplicationDescriptor> loadAll() {
        return new ArrayList<>(getDatabase().getWebApplicationDescriptors());
    }

    public void insert(final WebApplicationDescriptor descriptor) {
        Collection<WebApplicationDescriptor> descriptors = getDatabase().getWebApplicationDescriptors();
        internalDelete(descriptor, descriptors);
        descriptors.add(WebApplicationDescriptor.copyOf(descriptor));
        tryToCommit();

    }

    public void delete(WebApplicationDescriptor descriptor) {
        internalDelete(descriptor, getDatabase().getWebApplicationDescriptors());
        tryToCommit();
    }

    private void internalDelete(WebApplicationDescriptor descriptor, Collection<WebApplicationDescriptor> descriptors) {
        WebApplicationDescriptor existing = internalLoad(descriptor.getContextPath(), descriptors);
        Objects.requireNonNull(existing);
        descriptors.remove(existing);
    }

    private WebApplicationDescriptor internalLoad(String contextPath, Collection<WebApplicationDescriptor> descriptors) {
        return selectFirst(descriptors,
                           having(on(WebApplicationDescriptor.class).getContextPath(), equalTo(contextPath)));
    }

}
