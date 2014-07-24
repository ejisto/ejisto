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

package com.ejisto.modules.dao.remote;

import com.ejisto.modules.dao.MockedFieldsDao;
import com.ejisto.modules.dao.entities.MockedField;
import com.ejisto.modules.web.MockedFieldRequest;
import com.ejisto.modules.web.util.MockedFieldsJSONUtil;

import java.util.Collection;
import java.util.List;

import static com.ejisto.constants.StringConstants.CTX_GET_MOCKED_FIELD;
import static com.ejisto.constants.StringConstants.CTX_NEWLY_CREATED_FIELDS;
import static com.ejisto.modules.web.MockedFieldRequest.*;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 7/2/12
 * Time: 7:32 PM
 */
public class RemoteMockedFieldsDao extends BaseRemoteDao implements MockedFieldsDao {

    private static final String REQUEST_PATH = CTX_GET_MOCKED_FIELD.getValue();

    @Override
    public List<MockedField> loadAll() {
        return callAndDecodeResponse(requestAllFields());
    }

    @Override
    public Collection<MockedField> loadContextPathFields(String contextPath) {
        return loadByContextPathAndClassName(contextPath, null);
    }

    @Override
    public List<MockedField> loadByContextPathAndClassName(String contextPath, String className) {
        return callAndDecodeResponse(requestAllFieldsOf(contextPath, className));
    }

    @Override
    public int countByContextPathAndClassName(String contextPath, String className) {
        return loadByContextPathAndClassName(contextPath, className).size();
    }

    @Override
    public MockedField getMockedField(String contextPath, String className, String fieldName) {
        List<MockedField> fields = callAndDecodeResponse(requestSingleField(contextPath, className, fieldName));
        if (fields == null || fields.size() == 0) {
            return null;
        }
        return fields.get(0);
    }

    @Override
    public boolean exists(String contextPath, String className, String fieldName) {
        return getMockedField(contextPath, className, fieldName) != null;
    }

    @Override
    public List<MockedField> getRecentlyCreatedFields() {
        return decodeResponse(remoteCall("", REQUEST_PATH + "/" + CTX_NEWLY_CREATED_FIELDS.getValue()));
    }

    @Override
    public void recordFieldCreation(MockedField mockedField) {
        throw new UnsupportedOperationException(REMOTE_DAO_IS_READ_ONLY);
    }

    @Override
    public boolean update(MockedField field) {
        throw new UnsupportedOperationException(REMOTE_DAO_IS_READ_ONLY);
    }

    @Override
    public MockedField insert(MockedField field) {
        throw new UnsupportedOperationException(REMOTE_DAO_IS_READ_ONLY);
    }

    @Override
    public void insert(Collection<MockedField> mockedFields) {
        throw new UnsupportedOperationException(REMOTE_DAO_IS_READ_ONLY);
    }

    @Override
    public boolean createContext(String contextPath) {
        throw new UnsupportedOperationException(REMOTE_DAO_IS_READ_ONLY);
    }

    @Override
    public boolean deleteContext(String contextPath) {
        throw new UnsupportedOperationException(REMOTE_DAO_IS_READ_ONLY);
    }

    private List<MockedField> decodeResponse(String responseBody) {
        return MockedFieldsJSONUtil.decodeMockedFields(responseBody);
    }

    private List<MockedField> callAndDecodeResponse(MockedFieldRequest request) {
        return decodeResponse(remoteCall(encodeRequest(request), REQUEST_PATH));
    }
}
