package org.ballerinalang.linter.Reference;

public class Reference {
    private Symbol symbol;
    private int count = 1;
    private boolean hasDefinition;

    public Reference(Symbol symbol, boolean hasDefinition) {
        this.symbol = symbol;
        this.hasDefinition = hasDefinition;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isHasDefinition() {
        return hasDefinition;
    }

    public void setHasDefinition(boolean hasDefinition) {
        this.hasDefinition = hasDefinition;
    }
}
