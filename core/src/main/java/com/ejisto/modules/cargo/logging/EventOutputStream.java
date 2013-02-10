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

package com.ejisto.modules.cargo.logging;

import com.ejisto.event.EventManager;
import com.ejisto.event.def.LogMessage;
import com.ejisto.util.SpringBridge;
import org.apache.log4j.MDC;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

import static com.ejisto.constants.StringConstants.CONTAINER_ID;

public class EventOutputStream extends OutputStream {

    private final StringBuffer buffer;
    private final LinkedBlockingQueue<LogMessage> queue;
    private EventManager eventManager;

    public EventOutputStream() {
        super();
        this.queue = new LinkedBlockingQueue<>();
        this.buffer = new StringBuffer();
    }

    @Override
    public void write(int b) throws IOException {
        buffer.append((char) b);
    }

    @Override
    public void flush() throws IOException {
        eventManager = SpringBridge.getInstance().getBean("eventManager", EventManager.class);
        String containerId = (String) MDC.get(CONTAINER_ID.getValue());
        queue.offer(new LogMessage(this, buffer.toString(), containerId));
        if (eventManager != null) {
            publishEvents();
        }
    }

    private void publishEvents() {
        LogMessage logMessage;
        while ((logMessage = queue.poll()) != null) {
            eventManager.publishEvent(logMessage);
        }
    }

}
