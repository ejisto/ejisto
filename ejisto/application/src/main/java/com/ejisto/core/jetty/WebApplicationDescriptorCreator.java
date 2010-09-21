/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010  Celestino Bellone
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

package com.ejisto.core.jetty;

import ch.jamme.conf.def.DefaultObjectCreator;
import org.jdom.Element;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

public class WebApplicationDescriptorCreator extends DefaultObjectCreator {

    @Override
    public Object createObject(Element element, Class<?> objectClass, Field field) {
        try {
            if(!objectClass.isInterface() && URL.class.isAssignableFrom(objectClass)) return new URL("file","",element.getChildText("file"));
            return super.createObject(element, objectClass, field);
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
