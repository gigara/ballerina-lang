import ballerina/io;

public function main() {
    [int, string] a = [10, "John"];
    io:println(a);

    int aint;
    string astr;
    [aint, astr] = a;
    io:println(aint);

    var [aint1, astr1] = a;
    io:println(astr1);

    var [q, r] = divideBy10(6);

    var [q1, _] = divideBy10(57);
    io:println("57/10: ", "quotient=", q1);
    [int, int] returnValue = divideBy10(9);
    var [_, r1] = returnValue;
}
function divideBy10(int d) returns ([int, int]) {
    int q = d / 10;
    int r = d % 10;
    return [q, r];
}
