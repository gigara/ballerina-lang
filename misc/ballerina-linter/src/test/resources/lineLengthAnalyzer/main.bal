import ballerina/io;
public function main() {
    int a = 4;
    // io:println(a);
    int[] b = [1, 2, 3, 4, 5, 6, 7, 8, 2, 3, 4, 5, 6, 7, 8, 2, 3, 4, 5, 6, 7, 8, 2, 3, 4, 5, 6, 7, 8, 2, 3, 4, 5, 6, 7, 8, 2, 3, 4, 5, 6, 7, 8];
    io:println(b[0]);
    string toTrim = "  Ballerina Programming Language /./..................................................................";
    string s7 = toTrim.trim();
    io:println("Trim: ", s7);
}