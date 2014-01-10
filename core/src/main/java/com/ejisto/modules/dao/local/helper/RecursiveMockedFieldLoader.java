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

package com.ejisto.modules.dao.local.helper;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.dao.local.LocalCollectedDataDao;
import com.ejisto.modules.recorder.CollectedData;
import com.ejisto.modules.web.MockedFieldRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 4/25/13
 * Time: 12:55 PM
 */
public class RecursiveMockedFieldLoader extends ForkJoinTask<List<MockedField>> {

    private final List<CollectedData> activeSessions;
    private final LocalCollectedDataDao dao;
    private final Predicate<MockedField> matcher;
    private List<MockedField> result;

    public RecursiveMockedFieldLoader(List<CollectedData> activeSessions,
                                      LocalCollectedDataDao dao,
                                      MockedFieldRequest request) {
        this(activeSessions, dao, buildMatcher(request));
    }

    public RecursiveMockedFieldLoader(List<CollectedData> activeSessions,
                                      LocalCollectedDataDao dao,
                                      Predicate<MockedField> matcher) {
        this.activeSessions = activeSessions;
        this.dao = dao;
        this.matcher = matcher;
    }

    @Override
    public List<MockedField> getRawResult() {
        return result;
    }

    @Override
    protected void setRawResult(List<MockedField> value) {
        this.result = value;
    }

    @Override
    protected boolean exec() {
        int size = activeSessions.size();
        if (size == 0) {
            setRawResult(Collections.<MockedField>emptyList());
            return true;
        }
        RecursiveMockedFieldLoader forked = null;
        if (size > 1) {
            forked = new RecursiveMockedFieldLoader(activeSessions.subList(1, size), dao, matcher);
            invokeAll(forked);
        }
        CollectedData current = activeSessions.get(0);
        List<MockedField> result = new ArrayList<>();
        List<MockedField> allFields = new ArrayList<>();
        allFields.addAll(current.getAllRequestAttributes().stream().flatMap(l -> l.stream()).collect(toList()));
        allFields.addAll(current.getAllSessionAttributes().stream().flatMap(l -> l.stream()).collect(toList()));
        result.addAll(allFields.stream().filter(matcher).collect(toList()));
        if (forked != null) {
            result.addAll(forked.join());
        }
        setRawResult(result);
        return true;
    }

    private static Predicate<MockedField> buildMatcher(MockedFieldRequest request) {
        if (request.areAllFieldsRequested()) {
            return (l -> true);
        }
        Predicate<MockedField> result = (l -> request.getContextPath().equals(l.getContextPath()));
        if (request.getClassName() != null) {
            result = result.and(l -> request.getClassName() == null || request.getClassName().equals(l.getClassName()));
        }
        if (request.getFieldName() != null) {
            result = result.and(l -> request.getFieldName() == null || request.getFieldName().equals(l.getFieldName()));
        }
        return result;
    }

}
