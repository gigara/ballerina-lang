package org.ballerinalang.linter.Reference;

import java.util.Objects;

public class Symbol {
    private String name;
    private String type;
    private String pkgName;
    private String pkgOrgName;

    public Symbol(String name, String type, String pkgName, String pkgOrgName) {
        this.name = name;
        this.type = type;
        this.pkgName = pkgName;
        this.pkgOrgName = pkgOrgName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return name.equals(symbol.name) &&
                type.equals(symbol.type) &&
                pkgName.equals(symbol.pkgName) &&
                pkgOrgName.equals(symbol.pkgOrgName);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getPkgOrgName() {
        return pkgOrgName;
    }
}
