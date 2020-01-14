import ballerina/io;

public function main() {
    io:println("Hello, World!");
    giga(0, "");
}

public function giga(int i, string b, string a = "hello") {
    io:println(i);
}

public function giga2(string a = "hello") {

}