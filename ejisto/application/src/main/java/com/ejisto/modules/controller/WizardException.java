package com.ejisto.modules.controller;

public class WizardException extends Exception {
    private static final long serialVersionUID = -8367325325739064925L;

    public WizardException(String message, Throwable cause) {
        super(message, cause);
    }

    public WizardException(Throwable cause) {
        super(cause);
    }
}
