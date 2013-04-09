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

package com.ejisto.core.configuration;

import com.ejisto.core.configuration.binder.CoreBinder;
import com.ejisto.core.configuration.binder.DAOBinder;
import se.jbee.inject.bind.BuildinBundle;
import se.jbee.inject.bootstrap.BootstrapperBundle;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 3/29/13
 * Time: 8:26 AM
 */
public class CoreBundle extends BootstrapperBundle {
    @Override
    protected void bootstrap() {
        install(DAOBinder.class);
        install(CoreBinder.class);
        install(BuildinBundle.LIST);
    }
}
