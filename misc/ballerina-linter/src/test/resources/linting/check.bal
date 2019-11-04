
import ballerina/lang.'int as ints;
function parse(string num) returns int | error {
    return ints:fromString(num);
}

function scale(string num) returns int | error {
    int x =  check parse(num);
    int y =  
    check   parse(num);

    int z =check  parse(num);
    return x * 10;
}
