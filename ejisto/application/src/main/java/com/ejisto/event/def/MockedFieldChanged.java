/*******************************************************************************
 * Copyright 2010 Celestino Bellone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ejisto.event.def;

import com.ejisto.modules.dao.entities.MockedField;


public class MockedFieldChanged extends BaseApplicationEvent {
    private static final long serialVersionUID = -1695827582666783071L;
    
    private MockedField mockedField;

    public MockedFieldChanged(Object source, MockedField mockedField) {
        super(source);
        this.mockedField=mockedField;
    }
    
    public MockedField getMockedField() {
        return mockedField;
    }

    @Override
    public String getDescription() {
        return mockedField + " changed";
    }

    @Override
    public String getKey() {
        return null;
    }
}
