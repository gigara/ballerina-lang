/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.stdlib.crypto;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test cases for ballerina.crypto native functions relevant to private/public key parsing.
 *
 * @since 0.990.3
 */
public class KeyParsingTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src" +  File.separator + "crypto" +  File.separator +
                "keyparsing-test.bal");
    }

    @Test(description = "Check parsing encrypted private-key from a p12 file.")
    public void testParsingEncryptedPrivateKeyFromP12() {
        BValue[] args = {new BString("target" + File.separator + "test-classes" + File.separator + "datafiles"
                + File.separator + "crypto" + File.separator + "testKeystore.p12"), new BString("ballerina"),
                new BString("ballerina"), new BString("ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromP12", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get(Constants.PRIVATE_KEY_RECORD_ALGORITHM_FIELD).stringValue(),
                "RSA");
    }

    @Test(description = "Check parsing public-key from a p12 file.")
    public void testParsingPublicKeyFromP12() {
        BValue[] args = {new BString("target" + File.separator + "test-classes" + File.separator + "datafiles"
                + File.separator + "crypto" + File.separator + "testKeystore.p12"), new BString("ballerina"),
                new BString("ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testParsingPublicKeyFromP12", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) returnValues[0]).get(Constants.PUBLIC_KEY_RECORD_ALGORITHM_FIELD).stringValue(),
                "RSA");
        Assert.assertTrue(((BMap) returnValues[0]).get(Constants.PUBLIC_KEY_RECORD_CERTIFICATE_FIELD) instanceof BMap);
        BMap<String, BValue> certificate = (BMap<String, BValue>) ((BMap) returnValues[0]).get("certificate");
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_SERIAL_FIELD).stringValue(), "2097012467");
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_ISSUER_FIELD).stringValue(),
                "CN=localhost,OU=WSO2,O=WSO2,L=Mountain View,ST=CA,C=US");
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_SUBJECT_FIELD).stringValue(),
                "CN=localhost,OU=WSO2,O=WSO2,L=Mountain View,ST=CA,C=US");
        Assert.assertTrue(certificate.get(Constants.CERTIFICATE_RECORD_NOT_BEFORE_FIELD) instanceof BMap);
        Assert.assertTrue(certificate.get(Constants.CERTIFICATE_RECORD_NOT_AFTER_FIELD) instanceof BMap);
        Assert.assertTrue(certificate.get(Constants.CERTIFICATE_RECORD_SIGNATURE_FIELD) instanceof BValueArray);
        Assert.assertEquals(certificate.get(Constants.CERTIFICATE_RECORD_SIGNATURE_ALG_FIELD).stringValue(),
                "SHA256withRSA");
    }

    @Test(description = "Check attemting to read a private key from a non-existing p12 file.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: PKCS12 key store not found.*")
    public void testParsingEncryptedPrivateKeyFromInvalidLocation() {
        BValue[] args = {new BString("target" + File.separator + "test-classes" + File.separator + "datafiles"
                + File.separator + "crypto" + File.separator + "testKeystore.p12.invalid"), new BString("ballerina"),
                new BString("ballerina"), new BString("ballerina")};
        BRunUtil.invoke(compileResult, "testParsingPrivateKeyFromP12", args);
    }

    @Test(description = "Check attemting to read a public key from a non-existing p12 file.",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: PKCS12 key store not found.*")
    public void testParsingPublicKeyFromInvalidLocation() {
        BValue[] args = {new BString("target" + File.separator + "test-classes" + File.separator + "datafiles"
                + File.separator + "crypto" + File.separator + "testKeystore.p12.invalid"), new BString("ballerina"),
                new BString("ballerina")};
        BRunUtil.invoke(compileResult, "testParsingPublicKeyFromP12", args);
    }
}
