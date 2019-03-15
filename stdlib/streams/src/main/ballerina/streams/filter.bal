// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Filter object {
    private function (StreamEvent?[]) nextProcessorPointer;
    private function (map<anydata>) returns boolean conditionFunc;

    function __init(function (StreamEvent?[]) nextProcessorPointer,
                    function (map<anydata>) returns boolean conditionFunc) {
        self.nextProcessorPointer = nextProcessorPointer;
        self.conditionFunc = conditionFunc;
    }

    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] newStreamEventArr = [];
        int index = 0;
        foreach var ev in streamEvents {
            StreamEvent event = <StreamEvent> ev;
            if (self.conditionFunc.call(event.data)) {
                newStreamEventArr[index] = event;
                index += 1;
            }
        }
        if (index > 0) {
            self.nextProcessorPointer.call(newStreamEventArr);
        }
    }
};

public function createFilter(function (StreamEvent?[]) nextProcPointer,
                             function (map<anydata> o) returns boolean conditionFunc) returns Filter {
    Filter filter = new(nextProcPointer, conditionFunc);
    return filter;
}
