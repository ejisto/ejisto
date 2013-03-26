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

import ch.lambdaj.group.Group;
import com.ejisto.core.classloading.scan.ScanAction;
import com.ejisto.event.EventManager;
import com.ejisto.event.def.ApplicationError;
import com.ejisto.event.def.ApplicationScanRequired;
import com.ejisto.event.def.BlockingTaskProgress;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import com.ejisto.modules.gui.Application;
import com.ejisto.modules.gui.components.helper.FieldsEditorContext;
import com.ejisto.modules.repository.MockedFieldsRepository;
import com.ejisto.util.FieldsEditorContextMatcher;
import lombok.extern.log4j.Log4j;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

import static ch.lambdaj.Lambda.group;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/3/12
 * Time: 9:35 PM
 */
@Log4j
public class WebApplicationScanner implements ApplicationListener<ApplicationScanRequired> {

    @Resource private EventManager eventManager;
    @Resource private MockedFieldsRepository mockedFieldsRepository;
    @Resource private Application application;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    @Override
    public void onApplicationEvent(ApplicationScanRequired event) {
        WebApplicationDescriptor descriptor = event.getWebApplicationDescriptor();
        List<MockedField> fields = mockedFieldsRepository.loadAll(descriptor.getContextPath(),
                new FieldsEditorContextMatcher(FieldsEditorContext.CREATE_FIELD));
        if (isEmpty(fields)) {
            return;
        }
        String id = UUID.randomUUID().toString();
        eventManager.publishEvent(new BlockingTaskProgress(this, id, "application.deploy.preprocessing.title",
                                                           "application.deploy.preprocessing.description",
                                                           "icon.work.in.progress", true));
        Group<MockedField> groupedByClassName = group(fields, "className");
        ScanAction action = new ScanAction(descriptor, groupedByClassName.subgroups());
        try {
            forkJoinPool.invoke(action);
            action.get();
        } catch (Exception ex) {
            log.error("exception during scan", ex);
            eventManager.publishEvent(new ApplicationError(this, ApplicationError.Priority.HIGH, ex));
        }
        eventManager.publishEventAndWait(new BlockingTaskProgress(this, id, null, null, null, false));
    }
}
