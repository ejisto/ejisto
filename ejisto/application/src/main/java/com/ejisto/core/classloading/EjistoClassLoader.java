/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ejisto.core.classloading;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import com.ejisto.modules.dao.entities.MockedField;

public class EjistoClassLoader extends WebAppClassLoader {

    private static final Logger logger = Logger.getLogger(EjistoClassLoader.class);
	private ClassTransformer transformer;
	private Collection<MockedField> mockedFields;

    public EjistoClassLoader(String webAppPath, Collection<MockedField> mockedFields, WebAppContext context) throws IOException {
        super(context);
//        InstrumentationHolder.getInstrumentation().addTransformer(new ClassTransformer(this, mockedFields), true);
        this.transformer=new ClassTransformer(this, mockedFields);
        this.mockedFields=mockedFields;
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
		return (selectFirst(mockedFields, having(on(MockedField.class).getClassName(), equalTo(name.replaceAll("/", ".")))) != null);
	}
	
}
