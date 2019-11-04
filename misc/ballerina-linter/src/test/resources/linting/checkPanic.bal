import ballerina/io;
import ballerina/lang.'int as ints;
function parse(string num) returns int | error {
    return ints:fromString(num);
}

public function main() {
    int y =   checkpanic parse("120");
    io:println(y);
    int z =checkpanic parse ("Invalid");
    io:println(z);
}
