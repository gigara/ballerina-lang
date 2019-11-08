import ballerina/io;
const string GET = "GET";
const POST = "POST";
type ACTION GET | POST;
const int I = 125;
const map<string> data = {"user": "Ballerina", "ID": "1234"};
const map<map<string>> complexData = {"data": data, "data2": {"user": "WSO2"}};
const map<map<string>> complexData2 = {"data": data, "data2": {"user": "WSO2"}};

public function main() {
    ACTION get = GET;
    int value = I + 10;
    io:println(value);
    io:println(data);
    io:println(complexData);
    io:println(complexData["data"]["user"]);
}