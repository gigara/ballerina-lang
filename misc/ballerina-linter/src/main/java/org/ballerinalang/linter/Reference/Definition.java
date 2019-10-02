package org.ballerinalang.linter.Reference;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Definition {
    private BSymbol symbol;
    private boolean hasReference;
    private boolean hasDefinition;
    private Diagnostic.DiagnosticPosition position;

    public Definition(BSymbol symbol, boolean hasReference, boolean hasDefinition, Diagnostic.DiagnosticPosition position) {
        this.symbol = symbol;
        this.hasReference = hasReference;
        this.hasDefinition = hasDefinition;
        this.position = position;
    }

    public BSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(BSymbol symbol) {
        this.symbol = symbol;
    }

    public boolean isHasReference() {
        return hasReference;
    }

    public void setHasReference(boolean hasReference) {
        this.hasReference = hasReference;
    }

    public boolean isHasDefinition() {
        return hasDefinition;
    }

    public void setHasDefinition(boolean hasDefinition) {
        this.hasDefinition = hasDefinition;
    }

    public Diagnostic.DiagnosticPosition getPosition() {
        return position;
    }

    public void setPosition(Diagnostic.DiagnosticPosition position) {
        this.position = position;
    }

    public String md5() {
        String hash = symbol.name.value + symbol.type.name + symbol.pkgID.name.value + symbol.pkgID.orgName.value;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(hash.getBytes());
            byte[] digest = md.digest();
            String md5 = DatatypeConverter
                    .printHexBinary(digest).toUpperCase();
            return md5;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
