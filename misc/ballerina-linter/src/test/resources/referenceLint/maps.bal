import ballerina/io;

public function main() {
    map<string> m;
    map<string> addrMap = {
        line1: "No. 20",
        line2: "Palm Grove",
        city: "Colombo 03",
        country: "Sri Lanka"
    };
    io:println(addrMap);
    string country = <string>addrMap["country"];
    io:println(country);
    string line2 = addrMap.get("line2");
    io:println(line2);
    boolean hasPostalCode = addrMap.hasKey("postalCode");
    io:println(hasPostalCode);
    addrMap["postalCode"] = "00300";
    io:println(addrMap);
    io:println(addrMap.keys());
    io:println(addrMap.length());
    string removedElement = addrMap.remove("postalCode");
    io:println(addrMap);
    addrMap.forEach(function (string value) {

    });
    map<int> marks = {sam: 50, jon: 60};
    map<int> modifiedMarks = marks.entries().map(function ([string, int] pair) returns int {
        var [name, score] = pair;
        io:println(io:sprintf("%s scored: %d", name, score));
        return score + 10;
    });
    io:println(modifiedMarks);

    map<string> stringMap = {};
    stringMap["index"] = "100892N";
    string index2 = stringMap["index"] ?: "";
    io:println(index2);
}
