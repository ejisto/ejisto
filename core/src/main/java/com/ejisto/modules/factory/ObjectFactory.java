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

package com.ejisto.modules.factory;

import com.ejisto.modules.dao.entities.MockedField;

/**
 * Base Interface for pluggable ObjectFactories
 * You could create your own factory for domain-specific objects
 * implementing this interface and placing jar-packaged class(es) into
 * $EJISTO_HOME/lib/ext .
 * Ejisto will load these classes, even if you've already started your application.
 *
 * @param <T> the managed type
 * @author Celestino Bellone
 */
public interface ObjectFactory<T> {
    /**
     * Returns the target class name for caching purposes.<br>
     * <b>Please note that cache manager won't allow existing mappings</b>
     *
     * @return target class name
     */
    String getTargetClassName();

    /**
     * Factory's factory method
     *
     * @param m           the target Field
     * @param actualValue actual field value
     * @return new Instance of managed Object
     */
    T create(MockedField m, T actualValue);

    /**
     * Returns a flag indicating that this ObjectFactory supports random values creation
     *
     * @return {@code true} if supported, @{code false} otherwise
     */
    boolean supportsRandomValuesCreation();

    /**
     * Random value generator
     *
     * @return random value (if supported)
     */
    T createRandomValue();
}
