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

package org.ballerinalang.linter;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.linter.Reference.ReferenceFinder;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ReferenceTest {

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private Path projectPath = RES_DIR.resolve(Paths.get("referenceLint"));
    private DiagnosticLogTest diagnosticLog = new DiagnosticLogTest();

    @BeforeMethod
    public void setUp() {
        diagnosticLog.getLog().clear();
    }

    @DataProvider
    public static Object[][] referenceDataProvider() {
        return new Object[][]{
                {"constants.bal","expectedConstants.txt"},
                {"function.bal","expectedFunction.txt"},
                {"variable.bal","expectedVariable.txt"},
                {"maps.bal","expectedMaps.txt"},
                {"matchStmt.bal","expectedMatchStmt.txt"},
                {"object.bal","expectedObject.txt"},
                {"recordDestructure.bal","expectedRecordDestructure.txt"},
                {"serviceConstruct.bal","expectedServiceConstruct.txt"},
                {"stringTemplateLiteral.bal","expectedStringTemplateLiteral.txt"},
                {"ternaryExpr.bal","expectedTernaryExpr.txt"},
                {"tuple.bal","expectedTuple.txt"},
                {"tupleDestructure.bal","expectedTupleDestructure.txt"},
                {"transaction.bal","expectedTransaction.txt"},
                {"unionTypes.bal","expectedUnionTypes.txt"},
                {"worker.bal","expectedWorker.txt"},
                {"while.bal","expectedWhile.txt"},
                {"xml.bal","expectedXml.txt"}
        };
    }

    @Test(description = "Test Ballerina skeleton generation", dataProvider = "referenceDataProvider")
    public void ReferenceTest(String file, String expectedFile) {
        try {
            Path ballerinaSource = projectPath.resolve(file);
            Path expectedSource = projectPath.resolve("expected").resolve(expectedFile);
            if (Files.exists(ballerinaSource)) {
                String expected = new String(Files.readAllBytes(expectedSource));

                BallerinaFile ballerinaFile = ExtendedLSCompiler.compileFile(ballerinaSource, CompilerPhase.DESUGAR);
                List<BLangCompilationUnit> compilationUnits = ballerinaFile.getBLangPackage().get().getCompilationUnits();

                ReferenceFinder referenceFinder = new ReferenceFinder();
                for (CompilationUnitNode compilationUnit:compilationUnits) {
                    referenceFinder.visit((BLangCompilationUnit) compilationUnit);
                }

                LinterPlugin linterPlugin = new LinterPlugin();
                linterPlugin.pushReferenceErrors(referenceFinder, diagnosticLog);

                StringBuilder actual = new StringBuilder();
                for (String line : diagnosticLog.getLog()) {
                    actual.append(line).append(System.lineSeparator());
                }
                Assert.assertEquals(actual.toString(), expected, "Did not match");
            }

        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }
    }
}
