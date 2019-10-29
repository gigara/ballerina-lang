 import ballerina / http   ; import
                               ballerina
                                /
                                io
                              ;
import     ballerina / http   as  y ;
            import      ballerina  /    auth   ; import ballerina/  lang .  'int as  ints;
public function main() {
    io:println(http:HttpVersion);
    io:println(auth:Credential);
    io:println(y:HttpVersion);
    int | error i2 = ints:fromString("100");
}            