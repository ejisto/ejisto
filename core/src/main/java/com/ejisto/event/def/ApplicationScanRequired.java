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

package com.ejisto.event.def;

import com.ejisto.modules.dao.entities.WebApplicationDescriptor;
import lombok.Getter;

/**
 * Created by IntelliJ IDEA.
 * User: celestino
 * Date: 8/3/12
 * Time: 6:35 PM
 */
@Getter
public class ApplicationScanRequired extends BaseApplicationEvent {

    private final WebApplicationDescriptor webApplicationDescriptor;
    private final String requestId;

    public ApplicationScanRequired(Object source, String requestId, WebApplicationDescriptor webApplicationDescriptor) {
        super(source);
        this.requestId = requestId;
        this.webApplicationDescriptor = webApplicationDescriptor;
    }

    @Override
    public String getDescription() {
        return "Application scan requested";
    }

    @Override
    public String getKey() {
        return "scan";
    }

    @Override
    protected String getEventDescriptionValue() {
        return webApplicationDescriptor.getContextPath();
    }


}
