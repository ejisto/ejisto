/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.ejisto.event.def;

import org.springframework.context.ApplicationEvent;

public abstract class BaseApplicationEvent extends ApplicationEvent {

    private static final long serialVersionUID = -2616190172593844401L;

    public BaseApplicationEvent(Object source) {
        super(source);
    }
    
    public abstract String getDescription();
    
    public String getIconKey() {
        return "";
    }
    
    public abstract String getKey();
    
}
