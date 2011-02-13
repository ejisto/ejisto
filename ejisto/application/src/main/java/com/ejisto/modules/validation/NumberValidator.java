/*
 * Ejisto, a powerful developer assistant
 *
 * Copyright (C) 2010-2011  Celestino Bellone
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

package com.ejisto.modules.validation;

import javax.swing.*;
import java.awt.*;

public class NumberValidator extends InputVerifier {
    private ValidationType validationType;

    public enum ValidationType {
        UNSIGNED_INTEGER,
        SIGNED_INTEGER,
        DOUBLE
    }

    public NumberValidator() {
        this(ValidationType.UNSIGNED_INTEGER);
    }

    public NumberValidator(ValidationType validationType) {
        this.validationType = validationType;
    }

    @Override
    public boolean verify(JComponent input) {
        JFormattedTextField field = (JFormattedTextField) input;
        boolean valid;
        try {
            valid = internalVerify(field.getText());
        } catch (NumberFormatException e) {
            valid = false;
        }
        field.setForeground(valid ? Color.black : Color.red);
        return valid;
    }

    private boolean internalVerify(String text) {
        switch(validationType) {
            case DOUBLE:
                return new Double(text).compareTo(0.0D) >= 0;
            case UNSIGNED_INTEGER:
                return Integer.parseInt(text) >= 0;
            case SIGNED_INTEGER:
                return Integer.decode(text) != null;
        }
        if (validationType == ValidationType.DOUBLE) {
            return new Double(text).compareTo(0.0D) >= 0;
        } else {
            return Integer.parseInt(text) >= 0;
        }
    }


}
