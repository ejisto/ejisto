package com.ejisto.modules.controller.wizard;

import java.util.Comparator;

public class StepControllerComparator implements Comparator<StepController<?>> {

    @Override
    public int compare(StepController<?> o1, StepController<?> o2) {
        return o1.getStep().getIndex() - o2.getStep().getIndex();
    }

}
