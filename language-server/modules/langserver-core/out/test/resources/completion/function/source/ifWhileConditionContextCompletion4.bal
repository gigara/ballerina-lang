import ballerina/http;
function testConditionFunction() {
    string|boolean test = "hello";
    
    if (test is http:) {
        
    }
}

type RecordName record {
    
};

type ObjectName object {
    
};