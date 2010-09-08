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



public class ApplicationError extends BaseApplicationEvent{
    private static final long serialVersionUID = -451087873117261043L;
    private Priority priority;
    private Throwable error;

    public enum Priority {
        FATAL,
        HIGH,
        LOW
    }

    public ApplicationError(Object source, Priority priority, Throwable error) {
        super(source);
        this.priority=priority;
        this.error=error;
    }

    public Priority getPriority() {
        return priority;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public String getDescription() {
        return "error";
    }

    @Override
    public String getKey() {
        return null;
    }
}
