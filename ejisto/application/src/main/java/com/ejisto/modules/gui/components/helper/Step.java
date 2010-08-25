/*
 * Copyright 2010 Celestino Bellone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.ejisto.modules.gui.components.helper;

public enum Step {
    FILE_SELECTION(0),FILE_EXTRACTION(1), CLASSES_FILTERING(2), APPLICATION_SCANNING(3), PROPERTIES_EDITING(4), SUMMARY(5);
    private int index;

    private Step(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static Step nextStep(Step current) {
        if (current.getIndex() >= Step.values().length - 1)
            return null;
        else
            return Step.values()[current.getIndex() + 1];
    }
    
    public static Step previousStep(Step current) {
        if (current.getIndex() <= 0)
            return null;
        else
            return Step.values()[current.getIndex() - 1];
    }
}