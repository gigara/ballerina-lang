import ballerina/io;
function println(string | int value) {
}
function getValue(string key) returns string | error {
    if (key == "") {
        error err = error("key '" + key + "' not found");
        return err;
    } else {
        return "this is a value";
    }
}

public function main() {
    println("This is a string");
    println(101);
    string | error valueOrError1 = getValue("name");

    string | error valueOrError2 = getValue("");
    io:println(valueOrError2);
}
