import ballerina/test;
import ballerina/io;

@test:Config{
    dataProvider:"invalidDataGen"
}
function testFunc2 (string fValue, string sValue, string result) returns error? {

    var value1 = check int.convert(fValue);
    var value2 = check int.convert(sValue);
    var result1 = check int.convert(result);
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

function invalidDataGen() returns (string) {
    return "hi";
}
