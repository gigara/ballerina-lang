/*

 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

package org.ballerinalang.linter.Reference;

import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Ballerina linter reference finder definition class
 *
 * @since 1.0.1
 */
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

    public String md5() {
        String hash = symbol.name.value + symbol.type.name + symbol.pkgID.name.value + symbol.pkgID.orgName.value
                +symbol.tag + symbol.owner.tag + symbol.owner.name.value
                +((symbol.type.tsymbol != null) ? symbol.type.tsymbol.name.value : "")
                +((symbol.kind != null) ? symbol.kind.name() : "");
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
