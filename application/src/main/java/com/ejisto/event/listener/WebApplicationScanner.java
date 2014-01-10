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

package com.ejisto.event.listener;

import com.ejisto.core.classloading.scan.ScanAction;
import com.ejisto.event.ApplicationListener;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ApplicationScanRequired;
import com.ejisto.event.def.BlockingTaskProgress;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import lombok.extern.log4j.Log4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/3/12
 * Time: 9:35 PM
 */
@Log4j
public class WebApplicationScanner implements ApplicationListener<ApplicationScanRequired> {

    private final EventManager eventManager;
    private final MockedFieldsRepository mockedFieldsRepository;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public WebApplicationScanner(EventManager eventManager, MockedFieldsRepository mockedFieldsRepository) {
        this.eventManager = eventManager;
        this.mockedFieldsRepository = mockedFieldsRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationScanRequired event) {
        WebApplicationDescriptor descriptor = event.getWebApplicationDescriptor();
        List<MockedField> fields = mockedFieldsRepository.loadAll(descriptor.getContextPath(),FieldsEditorContext.CREATE_FIELD::isAdmitted);
        if (isEmpty(fields)) {
            return;
        }
        String id = UUID.randomUUID().toString();
        eventManager.publishEvent(new BlockingTaskProgress(this, id, "application.deploy.preprocessing.title",
                                                           "application.deploy.preprocessing.description",
                                                           "icon.work.in.progress", true));
        final Map<String,List<MockedField>> groups = fields.stream().collect(
                Collectors.groupingBy(MockedField::getClassName));
        ScanAction action = new ScanAction(descriptor, groups, mockedFieldsRepository);
        try {
            forkJoinPool.invoke(action);
            action.get();
        } catch (Exception ex) {
            log.error("exception during scan", ex);
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, ex));
        }
        eventManager.publishEventAndWait(new BlockingTaskProgress(this, id, null, null, null, false));
    }

    @Override
    public Class<ApplicationScanRequired> getTargetEventType() {
        return ApplicationScanRequired.class;
    }
}
