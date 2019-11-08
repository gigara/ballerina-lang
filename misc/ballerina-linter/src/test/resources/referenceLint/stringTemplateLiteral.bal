import ballerina/io;

public function main() {
    string name = "Ballerina";
    string name2 = "Ballerina";

    string template = string `Hello ${name}!!!`;
    io:println(template);
}
