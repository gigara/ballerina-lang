// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/socket;

function echo(string msg) returns string {
    socket:UdpClient socketClient = new;
    byte[] c1 = msg.toByteArray("utf-8");
    var sendResult = socketClient->sendTo(c1, { host: "localhost", port: 48826 });
    if (sendResult is int) {
        io:println("Number of bytes written: ", sendResult);
    } else {
        panic sendResult;
    }
    string returnStr = "";
    var result = socketClient->receiveFrom();
    if (result is [byte[], int, socket:Address]) {
        var [content, length, address] = result;
        var str = getString(content);
        if (str is string) {
            returnStr = <@untainted> str;
        } else {
            io:println(str.detail().message);
        }
    } else {
        io:println(result);
    }
    checkpanic socketClient->close();
    return returnStr;
}

function contentReceive() returns string {
    socket:UdpClient socketClient = new(localAddress = { port: 48827 });
    string returnStr = "";
    var result = socketClient->receiveFrom();
    if (result is [byte[], int, socket:Address]) {
        var [content, length, address] = result;
        var str = getString(content);
        if (str is string) {
            returnStr = <@untainted> str;
        } else {
            io:println(str.detail().message);
        }
    } else {
        io:println(result);
    }
    checkpanic socketClient->close();
    return returnStr;
}

function contentReceiveWithLength() returns string {
    socket:UdpClient socketClient = new(localAddress = { host: "localhost", port: 48828 });
    string returnStr = "";
    var result = socketClient->receiveFrom(length = 56);
    if (result is [byte[], int, socket:Address]) {
        var [content, length, address] = result;
        var str = getString(content);
        if (str is string) {
            returnStr = <@untainted> str;
        } else {
            io:println(str.detail().message);
        }
    } else {
        io:println(result);
    }
    checkpanic socketClient->close();
    return returnStr;
}

function getString(byte[] content) returns @tainted string|error {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(content);
    io:ReadableCharacterChannel characterChannel = new io:ReadableCharacterChannel(byteChannel, "UTF-8");
    return characterChannel.read(60);
}
