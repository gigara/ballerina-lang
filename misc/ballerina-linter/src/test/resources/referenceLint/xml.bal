import ballerina/io;

public function main() {
    xml x1 = xml `<book>The Lost World</book>`;

    xml x2 = xml `Hello, world!`;

    xml x3 = xml `<!--I am a comment-->`;

    xml x4 = xml `<?target data?>`;
    io:println(x4);

    xml x5 = x2 + x4;
    io:println("\nResulting XML sequence:");
    io:println(x5);
}
