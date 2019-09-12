package org.ballerinalang.linter.Reference;

import org.ballerinalang.util.diagnostic.Diagnostic;

public class Definition {
    private Symbol symbol;
    private boolean hasReference;
    private Diagnostic.DiagnosticPosition position;

    public Definition(Symbol symbol, boolean hasReference, Diagnostic.DiagnosticPosition position) {
        this.symbol = symbol;
        this.hasReference = hasReference;
        this.position = position;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public boolean isHasReference() {
        return hasReference;
    }

    public void setHasReference(boolean hasReference) {
        this.hasReference = hasReference;
    }

    public Diagnostic.DiagnosticPosition getPosition() {
        return position;
    }

    public void setPosition(Diagnostic.DiagnosticPosition position) {
        this.position = position;
    }
}
