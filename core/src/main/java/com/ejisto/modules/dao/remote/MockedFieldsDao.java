/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2012  Celestino Bellone
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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.modules.web.util.JSONUtil;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/2/12
 * Time: 7:32 PM
 */
public class MockedFieldsDao extends BaseRemoteDao implements com.ejisto.modules.dao.MockedFieldsDao {

    private static final String REQUEST_PATH = "/getField";

    @Override
    public List<MockedField> loadAll() {
        return callAndDecodeResponse(new MockedFieldRequest(null, null, null));
    }

    @Override
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return callAndDecodeResponse(new MockedFieldRequest(contextPath, null, null));
    }

    @Override
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        return callAndDecodeResponse(new MockedFieldRequest(contextPath, className, null));
    }

    @Override
    public int countByContextPathAndClassName(String contextPath, String className) {
        return callAndDecodeResponse(new MockedFieldRequest(contextPath, className, null)).size();
    }

    @Override
    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        List<MockedField> fields = callAndDecodeResponse(new MockedFieldRequest(contextPath, className, fieldName));
        if (fields == null || fields.size() == 0) {
            return null;
        }
        return fields.get(0);
    }

    @Override
    public boolean update(MockedField field) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }

    @Override
    public long insert(MockedField field) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }

    @Override
    public void insert(Collection<MockedField> mockedFields) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }

    @Override
    public boolean deleteContext(String contextPath) {
        throw new UnsupportedOperationException("Remote dao is read-only");
    }

    private List<MockedField> decodeResponse(String responseBody) {
        return JSONUtil.decodeMockedFields(responseBody);
    }

    private List<MockedField> callAndDecodeResponse(MockedFieldRequest request) {
        return decodeResponse(remoteCall(encodeRequest(request), REQUEST_PATH));
    }

}
