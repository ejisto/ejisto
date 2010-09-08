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

package com.ejisto.core.jetty.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.LogMessage;
import com.ejisto.util.SpringBridge;


public class EventOutputStream extends OutputStream {

    private StringBuffer buffer;
    private LinkedBlockingQueue<LogMessage> queue;
    private EventManager eventManager;

    public EventOutputStream() {
        super();
        this.queue = new LinkedBlockingQueue<LogMessage>();
        this.buffer = new StringBuffer();
    }

    @Override
    public void write(int b) throws IOException {
        buffer.append((char) b);
    }

    @Override
    public void flush() throws IOException {
        eventManager = SpringBridge.getInstance().getBean("eventManager", EventManager.class);
        queue.offer(new LogMessage(this, buffer.toString()));
        if (eventManager != null) {
            publishEvents();
        }
    }

    private void publishEvents() {
        LogMessage logMessage;
        while((logMessage=queue.poll()) != null) {
            eventManager.publishEvent(logMessage);
        }
    }

}
