package org.ballerinalang.linter;

import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test suit for source code linting.
 */
public class LinterTest {
    private Path lintingDirectory = Paths.get("src/test/resources").resolve("linting");

    @Test(description = "test linting functionality on functions", dataProvider = "fileProvider")
    public void formatTestSuit(String testFile) throws IOException, CompilationFailedException {
        Path inputFilePath = lintingDirectory.resolve(testFile);

        String test = LinterTestUtil.readFromFile(inputFilePath);

        LinterTestUtil.compileAndLint(test);
//        String expected = new String(Files.readAllBytes(expectedFilePath));
//        expected = expected.replaceAll("\\r\\n", "\n");
//        DocumentFormattingParams documentFormattingParams = new DocumentFormattingParams();
//
//        TextDocumentIdentifier textDocumentIdentifier1 = new TextDocumentIdentifier();
//        textDocumentIdentifier1.setUri(Paths.get(inputFilePath.toString()).toUri().toString());
//
//        FormattingOptions formattingOptions = new FormattingOptions();
//        formattingOptions.setInsertSpaces(true);
//        formattingOptions.setTabSize(4);
//
//        documentFormattingParams.setOptions(formattingOptions);
//        documentFormattingParams.setTextDocument(textDocumentIdentifier1);

        //TestUtil.openDocument(this.serviceEndpoint, inputFilePath);

        //String result = TestUtil.getFormattingResponse(documentFormattingParams, this.serviceEndpoint);
        //Gson gson = new Gson();
        //ResponseMessage responseMessage = gson.fromJson(result, ResponseMessage.class);
        //String actual = (String) ((LinkedTreeMap) ((List) responseMessage.getResult()).get(0)).get("newText");
        //actual = actual.replaceAll("\\r\\n", "\n");
        //TestUtil.closeDocument(this.serviceEndpoint, inputFilePath);
        //Assert.assertEquals(actual, expected, "Did not match: " + expectedFile);
    }

    @DataProvider
    public static Object[][] fileProvider() {
        return new Object[][]{
                {"function.bal"},
        };
    }

}
