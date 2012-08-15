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

package com.ejisto.modules.factory;

import com.ejisto.core.ApplicationException;
import com.ejisto.modules.repository.ObjectFactoryRepository;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/16/11
 * Time: 11:22 PM
 * <p/>
 * Base Class for all the implementations of <code>ObjectFactory&lt;C></code> where <code>C</code> is
 * a Container like <code>Collection&lt;E></code> or <code>Map&lt;K,E></code>
 *
 * @param <C> the Container Type
 * @param <E> the Element Type
 */

public abstract class AbstractContainerFactory<C, E> implements ObjectFactory<C> {

    @SuppressWarnings("unchecked")
    protected ObjectFactory<E> loadElementObjectFactory(String elementType, String contextPath) {
        try {
            return ObjectFactoryRepository.getInstance().getObjectFactory(elementType, contextPath);
        } catch (Exception e) {
            throw new ApplicationException("Got exception during objectFactory loading", e);
        }
    }
}
