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

package com.ejisto.core.classloading;

import org.apache.log4j.Logger;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.IOException;

import static com.ejisto.util.SpringBridge.isMockableClass;

public class EjistoClassLoader extends WebAppClassLoader {

    private static final Logger logger = Logger.getLogger(EjistoClassLoader.class);
	private ClassTransformer transformer;
	private String contextPath;
    private String installationPath;

    public EjistoClassLoader(String installationPath, WebAppContext context) throws IOException {
        super(context);
//        InstrumentationHolder.getInstrumentation().addTransformer(new ClassTransformer(this, mockedFields), true);
        this.installationPath=installationPath;
        this.contextPath = context.getContextPath();
        this.transformer=new ClassTransformer(this);
    }

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
		    
			boolean instrumentableClass = isInstrumentableClass(name);
			if(logger.isDebugEnabled()) logger.debug(name+" is mockable class: "+instrumentableClass);
			if(instrumentableClass) return loadInstrumentableClass(name);
			return super.loadClass(name);
		} catch (Exception e) {
			throw new ClassNotFoundException("Unable to load class ["+name+"]",e);
		}
	}
	
	public Class<?> loadInstrumentableClass(String name) throws Exception {
		if(logger.isDebugEnabled()) logger.debug("loading instrumentable class: "+name);
		return transformer.transform(name);
	}
	
	public boolean isInstrumentableClass(String name) {
	    return isMockableClass(contextPath, name.replaceAll("/", "."));
	}
	
	public String getContextPath() {
        return contextPath;
    }
	
	public String getInstallationPath() {
        return installationPath;
    }
	
	
	
}
