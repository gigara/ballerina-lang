package org.ballerinalang.linter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.ballerinalang.linter.Reference.ReferenceFinder;
import org.ballerinalang.model.elements.PackageID;
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
import java.util.List;

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
                {"abort.bal", "abortCompUnit.txt", "expectedAbort.txt"},
                {"arrayLiteralExpr.bal", "arrayLiteralExprCompUnit.txt", "expectedArrayLiteralExpr.txt"},
                {"arrowExpr.bal", "arrowExprCompUnit.txt", "expectedArrowExpr.txt"},
                {"binaryExpr.bal", "binaryExprCompUnit.txt", "expectedBinaryExpr.txt"},
                {"bindingPatterns.bal", "bindingPatternsCompUnit.txt", "expectedBindingPatterns.txt"},
                {"blockExpandOnDemand.bal", "blockExpandOnDemandCompUnit.txt", "expectedBlockExpandOnDemand.txt"},
                {"break.bal", "breakCompUnit.txt", "expectedBreak.txt"},
                {"check.bal", "checkCompUnit.txt", "expectedCheck.txt"},
                {"checkPanic.bal", "checkPanicCompUnit.txt", "expectedCheckPanic.txt"},
                {"compilationUnit.bal", "compilationUnitCompUnit.txt", "expectedCompilationUnit.txt"},
                {"compilationUnitMultiEOF.bal", "compilationUnitMultiEOFCompUnit.txt",
                        "expectedCompilationUnitMultiEOF.txt"},
                {"compoundAssignment.bal", "compoundAssignmentCompUnit.txt", "expectedCompoundAssignment.txt"},
                {"constant.bal", "constantCompUnit.txt", "expectedConstant.txt"},
                {"constrainedType.bal", "constrainedTypeCompUnit.txt", "expectedConstrainedType.txt"},
                {"continue.bal", "continueCompUnit.txt", "expectedContinue.txt"},
                {"documentation.bal", "documentationCompUnit.txt", "expectedDocumentation.txt"},
                {"elvisExpr.bal", "elvisExprCompUnit.txt", "expectedElvisExpr.txt"},
                {"empty.bal", "emptyCompUnit.txt", "expectedEmpty.txt"},
                {"endpoint.bal", "endpointCompUnit.txt", "expectedEndpoint.txt"},
                {"errorVariableDefinition.bal", "errorVariableDefinitionCompUnit.txt",
                        "expectedErrorVariableDefinition.txt"},
                {"errorVariableReference.bal", "errorVariableReferenceCompUnit.txt",
                        "expectedErrorVariableReference.txt"},
                {"expressionStatement.bal", "expressionStatementCompUnit.txt", "expectedExpressionStatement.txt"},
                {"fieldBasedAccess.bal", "fieldBasedAccessCompUnit.txt", "expectedFieldBasedAccess.txt"},
                {"foreach.bal", "foreachCompUnit.txt", "expectedForeach.txt"},
                {"forever.bal", "foreverCompUnit.txt", "expectedForever.txt"},
                {"function.bal", "functionCompUnit.txt", "expectedFunction.txt"},
                {"functionType.bal", "functionTypeCompUnit.txt", "expectedFunctionType.txt"},
                {"having.bal", "havingCompUnit.txt", "expectedHaving.txt"},
                {"if.bal", "ifCompUnit.txt", "expectedIf.txt"},
                {"import.bal", "importCompUnit.txt", "expectedImport.txt"},
                {"invocation.bal", "invocationCompUnit.txt", "expectedInvocation.txt"},
                {"lock.bal", "lockCompUnit.txt", "expectedLock.txt"},
                {"matchStmt.bal", "matchStmtCompUnit.txt", "expectedMatchStmt.txt"},
                {"namedArgsExpr.bal", "namedArgsExprCompUnit.txt", "expectedNamedArgsExpr.txt"},
                {"panic.bal", "panicCompUnit.txt", "expectedPanic.txt"},
                {"recordDestructure.bal", "recordDestructureCompUnit.txt", "expectedRecordDestructure.txt"},
                {"restArgsExpr.bal", "restArgsExprCompUnit.txt", "expectedRestArgsExpr.txt"},
                {"return.bal", "returnCompUnit.txt", "expectedReturn.txt"},
                {"serviceConstruct.bal", "serviceConstructCompUnit.txt", "expectedServiceConstruct.txt"},
                {"stringTemplateLiteral.bal", "stringTemplateLiteralCompUnit.txt", "expectedStringTemplateLiteral.txt"},
                {"table.bal", "tableCompUnit.txt", "expectedTable.txt"},
                {"tableQuery.bal", "tableQueryCompUnit.txt", "expectedTableQuery.txt"},
                {"ternaryExpr.bal", "ternaryExprCompUnit.txt", "expectedTernaryExpr.txt"},
                {"transaction.bal", "transactionCompUnit.txt", "expectedTransaction.txt"},
                {"trap.bal", "trapCompUnit.txt", "expectedTrap.txt"},
                {"tupleDestructure.bal", "tupleDestrctureCompUnit.txt", "expectedTupleDestructure.txt"},
                {"tupleType.bal", "tupleTypeCompUnit.txt", "expectedTupleType.txt"},
                {"tupleTypeRest.bal", "tupleTypeRestCompUnit.txt", "expectedTupleTypeRest.txt"},
                {"typeDefinition.bal", "typeDefinitionCompUnit.txt", "expectedTypeDefinition.txt"},
                {"typeDesc.bal", "typeDescCompUnit.txt", "expectedTypeDesc.txt"},
                {"unicodeCharTest.bal", "unicodeCharTestCompUnit.txt", "expectedUnicodeCharTest.txt"},
                {"unionType.bal", "unionTypeCompUnit.txt", "expectedUnionType.txt"},
                {"variableDefinition.bal", "variableDefinitionCompUnit.txt", "expectedVariableDefinition.txt"},
                {"while.bal", "whileCompUnit.txt", "expectedWhile.txt"},
                {"worker.bal", "workerCompUnit.txt", "expectedWorker.txt"},
                {"xmlCommentLiteral.bal", "xmlCommentLiteralCompUnit.txt", "expectedXmlCommentLiteral.txt"},
                {"xmlElementLiteral.bal", "xmlElementLiteralCompUnit.txt", "expectedXmlElementLiteral.txt"},
                {"xmlPlLiteral.bal", "xmlPlLiteralCompUnit.txt", "expectedXmlPlLiteral.txt"},
                {"xmlns.bal", "xmlnsCompUnit.txt", "expectedXmlns.txt"},
                {"xmlTextLiteral.bal", "xmlTextLiteralCompUnit.txt", "expectedXmlTextLiteral.txt"},
        };
    }

    @DataProvider
    public static Object[][] lengthDataProvider() {
        return new Object[][]{
                {"main.bal", "compunit.txt", "expected.txt"},
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
    public void whitespaceTest(String testFile, String compUnit, String expected) throws IOException {

        Path expectedFilePath = lintingDirectory.resolve("linting").resolve("expected").resolve(expected);
        String expectedStr = new String(Files.readAllBytes(expectedFilePath));

        Path compUnitFilePath = lintingDirectory.resolve("linting").resolve("compUnits").resolve(compUnit);
        String compUnitStr = new String(Files.readAllBytes(compUnitFilePath));
        JsonElement element = gson.fromJson(compUnitStr, JsonElement.class);
        model = element.getAsJsonObject();
        FormattingSourceGen.build(model, "CompilationUnit");

        WhitespaceVisitorEntry visitorEntry = new WhitespaceVisitorEntry();
        visitorEntry.accept(model, compilationUnit);

        LinterPlugin linterPlugin = new LinterPlugin();
        linterPlugin.pushWhiteSpacesErrors(model, diagnosticLog);

        StringBuilder actual = new StringBuilder();
        for (String line : diagnosticLog.getLog()) {
            actual.append(line).append(System.lineSeparator());
        }
        Assert.assertEquals(actual.toString(), expectedStr, "Did not match");
    }

    @Test(description = "test line length linting functionality", dataProvider = "lengthDataProvider")
    public void lineLengthTest(String testFile, String compUnit, String expected) throws IOException {

        Path expectedFilePath = lintingDirectory.resolve("lineLengthAnalyzer").resolve(expected);
        String expectedStr = new String(Files.readAllBytes(expectedFilePath));

        Path compUnitFilePath = lintingDirectory.resolve("lineLengthAnalyzer").resolve(compUnit);
        String compUnitStr = new String(Files.readAllBytes(compUnitFilePath));
        JsonElement element = gson.fromJson(compUnitStr, JsonElement.class);
        model = element.getAsJsonObject();

        LineLengthAnalyzer.lintLineLength(model, compilationUnit, diagnosticLog);
        StringBuilder actual = new StringBuilder();
        for (String line : diagnosticLog.getLog()) {
            actual.append(line).append(System.lineSeparator());
        }
        Assert.assertEquals(actual.toString(), expectedStr, "Did not match");
    }

}

