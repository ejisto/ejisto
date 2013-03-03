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

package com.ejisto.modules.dao.db;

import com.ejisto.modules.web.util.JSONUtil;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 2/17/13
 * Time: 5:08 PM
 */
public class JSONSerializer<T> implements Serializer<T>, Serializable {



    private final Class<T> target;

    public JSONSerializer(Class<T> target) {
        this.target = target;
    }

    @Override
    public void serialize(DataOutput out, T value) throws IOException {
        out.writeUTF(JSONUtil.encode(value));
    }

    @Override
    public T deserialize(DataInput in, int available) throws IOException {
        return JSONUtil.decode(in.readUTF(), target);
    }
}
