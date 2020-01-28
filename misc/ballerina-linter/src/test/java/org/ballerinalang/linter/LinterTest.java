package org.ballerinalang.linter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.linter.Reference.ReferenceFinder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil.generateJSON;
import static org.ballerinalang.linter.LinteringNodeTree.lintErrors;

/**
 * Test suit for source code linting.
 */
public class LinterTest {
    private BLangCompilationUnit compilationUnit = new BLangCompilationUnit();
    private PackageID packageID = new PackageID(new Name("chamupathi"), new Name("main"), new Name("0.1.0"));
    private DiagnosticLogTest diagnosticLog = new DiagnosticLogTest();
    private Path lintingDirectory = Paths.get("src/test/resources");
    private Gson gson = new Gson();
    private JsonObject model;

    @DataProvider
    public Object[][] lintDataProvider() {
        return new Object[][]{
                {"abort.bal", "expectedAbort.txt"},
                {"arrayLiteralExpr.bal", "expectedArrayLiteralExpr.txt"},
                {"arrowExpr.bal", "expectedArrowExpr.txt"},
                {"binaryExpr.bal", "expectedBinaryExpr.txt"},
                {"bindingPatterns.bal", "expectedBindingPatterns.txt"},
                {"blockExpandOnDemand.bal", "expectedBlockExpandOnDemand.txt"},
                {"break.bal", "expectedBreak.txt"},
                {"check.bal", "expectedCheck.txt"},
                {"checkPanic.bal", "expectedCheckPanic.txt"},
                {"compilationUnit.bal", "expectedCompilationUnit.txt"},
                {"compilationUnitMultiEOF.bal", "expectedCompilationUnitMultiEOF.txt"},
                {"compoundAssignment.bal", "expectedCompoundAssignment.txt"},
                {"constant.bal", "expectedConstant.txt"},
                {"constrainedType.bal", "expectedConstrainedType.txt"},
                {"continue.bal", "expectedContinue.txt"},
                {"documentation.bal", "expectedDocumentation.txt"},
                {"elvisExpr.bal", "expectedElvisExpr.txt"},
                {"empty.bal", "expectedEmpty.txt"},
                {"endpoint.bal", "expectedEndpoint.txt"},
                {"errorVariableDefinition.bal", "expectedErrorVariableDefinition.txt"},
                {"errorVariableReference.bal", "expectedErrorVariableReference.txt"},
                {"expressionStatement.bal", "expectedExpressionStatement.txt"},
                {"fieldBasedAccess.bal", "expectedFieldBasedAccess.txt"},
                {"foreach.bal", "expectedForeach.txt"},
                {"forever.bal", "expectedForever.txt"},
                {"function.bal", "expectedFunction.txt"},
                {"functionType.bal", "expectedFunctionType.txt"},
                {"having.bal", "expectedHaving.txt"},
                {"if.bal", "expectedIf.txt"},
                {"import.bal", "expectedImport.txt"},
                {"invocation.bal", "expectedInvocation.txt"},
                {"lock.bal", "expectedLock.txt"},
                {"matchStmt.bal", "expectedMatchStmt.txt"},
                {"namedArgsExpr.bal", "expectedNamedArgsExpr.txt"},
                {"panic.bal", "expectedPanic.txt"},
                {"recordDestructure.bal", "expectedRecordDestructure.txt"},
                {"restArgsExpr.bal", "expectedRestArgsExpr.txt"},
                {"return.bal", "expectedReturn.txt"},
                {"serviceConstruct.bal", "expectedServiceConstruct.txt"},
                {"stringTemplateLiteral.bal", "expectedStringTemplateLiteral.txt"},
                {"table.bal", "expectedTable.txt"},
                {"tableQuery.bal", "expectedTableQuery.txt"},
                {"ternaryExpr.bal", "expectedTernaryExpr.txt"},
                {"transaction.bal", "expectedTransaction.txt"},
                {"trap.bal", "expectedTrap.txt"},
                {"tupleDestructure.bal", "expectedTupleDestructure.txt"},
                {"tupleType.bal", "expectedTupleType.txt"},
                {"tupleTypeRest.bal", "expectedTupleTypeRest.txt"},
                {"typeDefinition.bal", "expectedTypeDefinition.txt"},
                {"typeDesc.bal", "expectedTypeDesc.txt"},
                {"unicodeCharTest.bal", "expectedUnicodeCharTest.txt"},
                {"unionType.bal", "expectedUnionType.txt"},
                {"variableDefinition.bal", "expectedVariableDefinition.txt"},
                {"while.bal", "expectedWhile.txt"},
                {"worker.bal", "expectedWorker.txt"},
                {"xmlCommentLiteral.bal", "expectedXmlCommentLiteral.txt"},
                {"xmlElementLiteral.bal", "expectedXmlElementLiteral.txt"},
                {"xmlPlLiteral.bal", "expectedXmlPlLiteral.txt"},
                {"xmlns.bal", "expectedXmlns.txt"},
                {"xmlTextLiteral.bal", "expectedXmlTextLiteral.txt"},
        };
    }

    @DataProvider
    public static Object[][] lengthDataProvider() {
        return new Object[][]{
                {"main.bal", "expected.txt"},
        };
    }

    @BeforeMethod
    public void setUp(Object[] testArgs) throws IOException {
        diagnosticLog.getLog().clear();
        String testFile = (String) testArgs[0];
        BDiagnosticSource source = new BDiagnosticSource(packageID, testFile);
        DiagnosticPos diagnosticPos = new DiagnosticPos(source, 0, 0, 0, 0);
        compilationUnit.pos = diagnosticPos;
    }

    @Test(description = "test white space linting functionality", dataProvider = "lintDataProvider")
    public void whitespaceTest(String testFile, String expected) throws IOException {
        try {
            Path ballerinaSource = lintingDirectory.resolve("linting").resolve(testFile);;
            Path expectedSource = lintingDirectory.resolve("linting").resolve("expected").resolve(expected);
            if (Files.exists(ballerinaSource)) {
                String expectedStr = new String(Files.readAllBytes(expectedSource));

                BallerinaFile ballerinaFile = ExtendedLSCompiler.compileFile(ballerinaSource, CompilerPhase.COMPILER_PLUGIN);
                List<BLangCompilationUnit> compilationUnits = ballerinaFile.getBLangPackage().get().getCompilationUnits();

                for (CompilationUnitNode compilationUnitNode : compilationUnits) {

                    JsonElement modelElement = null;
                    try {
                        modelElement = generateJSON(compilationUnitNode, new HashMap<>(), new HashMap<>());

                    } catch (JSONGenerationException e) {
                        e.printStackTrace();
                    }
                    model = modelElement.getAsJsonObject();
                    FormattingSourceGen.build(model, "CompilationUnit");

                    lintErrors.clear();
                    WhitespaceVisitorEntry visitorEntry = new WhitespaceVisitorEntry();
                    visitorEntry.accept(model, compilationUnit);

                    LinterPlugin linterPlugin = new LinterPlugin();
                    linterPlugin.pushWhiteSpacesErrors(model, diagnosticLog);

                    StringBuilder actual = new StringBuilder();
                    for (String line : diagnosticLog.getLog()) {
                        actual.append(line).append(System.lineSeparator());
                    }
                }

                StringBuilder actual = new StringBuilder();
                for (String line : diagnosticLog.getLog()) {
                    actual.append(line).append(System.lineSeparator());
                }
                Assert.assertEquals(actual.toString(), expectedStr, "Did not match");
            }

        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }
    }

    @Test(description = "test line length linting functionality", dataProvider = "lengthDataProvider")
    public void lineLengthTest(String testFile, String expected) throws IOException {
        try {
            Path ballerinaSource = lintingDirectory.resolve("linting").resolve(testFile);;
            Path expectedSource = lintingDirectory.resolve("lineLengthAnalyzer").resolve(expected);
            if (Files.exists(ballerinaSource)) {
                String expectedStr = new String(Files.readAllBytes(expectedSource));

                BallerinaFile ballerinaFile = ExtendedLSCompiler.compileFile(ballerinaSource, CompilerPhase.COMPILER_PLUGIN);
                List<BLangCompilationUnit> compilationUnits = ballerinaFile.getBLangPackage().get().getCompilationUnits();

                for (CompilationUnitNode compilationUnitNode : compilationUnits) {

                    JsonElement modelElement = null;
                    try {
                        modelElement = generateJSON(compilationUnitNode, new HashMap<>(), new HashMap<>());

                    } catch (JSONGenerationException e) {
                        e.printStackTrace();
                    }
                    model = modelElement.getAsJsonObject();

                    lintErrors.clear();
                    LineLengthAnalyzer.lintLineLength(model, compilationUnit, diagnosticLog);

                    StringBuilder actual = new StringBuilder();
                    for (String line : diagnosticLog.getLog()) {
                        actual.append(line).append(System.lineSeparator());
                    }
                }

                StringBuilder actual = new StringBuilder();
                for (String line : diagnosticLog.getLog()) {
                    actual.append(line).append(System.lineSeparator());
                }
                Assert.assertEquals(actual.toString(), expectedStr, "Did not match");
            }

        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }
    }

}

