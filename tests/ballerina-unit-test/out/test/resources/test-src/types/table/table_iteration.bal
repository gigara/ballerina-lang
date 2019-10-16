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

import ballerina/h2;

type Person record {
    int id;
    int age;
    float salary;
    string name;
};

type ResultCount record {
    int COUNTVAL;
};

type Employee record {
    int id;
    string name;
    float salary;
};

type EmployeeCompatible record {
    int id;
    string name;
    float salary;
};

type EmployeeSalary record {
    int id;
    float salary;
};

type EmployeeSalaryCompatible record {
    int id;
    float salary;
};

int idValue = -1;
int ageValue = -1;
float salValue = -1.0;
string nameValue = "";

function testForEachInTableWithStmt() returns (int, int, float, string) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person where id = 1", Person);

    int id = -1;
    int age = -1;
    float salary = -1;
    string name = "";

    if (dt is table<Person>) {
        foreach var x in dt {
            id = x.id;
            age = x.age;
            salary = x.salary;
            name = x.name;
        }
    }
    checkpanic testDB.stop();
    return (id, age, salary, name);
}

function testForEachInTableWithIndex() returns (string, string) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person where id < 10 order by id", Person);

    string indexStr = "";
    string idStr = "";
    if (dt is table<Person>) {
        int i = 0;
        foreach var x in dt {
            indexStr = indexStr + "," + i;
            idStr = idStr + "," + x.id;
            i += 1;
        }
    }
    checkpanic testDB.stop();
    return (idStr, indexStr);
}

function testForEachInTable() returns (int, int, float, string) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person where id = 1", Person);

    if (dt is table<Person>) {
        dt.foreach(function (Person p) {
                idValue = untaint p.id;
                ageValue = untaint p.age;
                salValue = untaint p.salary;
                nameValue = untaint p.name;
            }
        );
    }
    int id = idValue;
    int age = ageValue;
    float salary = salValue;
    string name = nameValue;
    checkpanic testDB.stop();
    return (id, age, salary, name);
}

function testCountInTable() returns (int) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person where id < 10", Person);
    int count = -1;
    if (dt is table<Person>) {
        count = dt.count();
    }
    checkpanic testDB.stop();
    return count;
}

function testFilterTable() returns (int, int, int) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person", Person);
    Person[] personBelow35 = [];
    int id1 = -1;
    int id2 = -1;
    int count = -1;
    if (dt is table<Person>) {
        personBelow35 = dt.filter(isBelow35);
        count = personBelow35.length();
        id1 = personBelow35[0].id;
        id2 = personBelow35[1].id;
    }
    checkpanic testDB.stop();
    return (count, id1, id2);
}

function testFilterWithAnonymousFuncOnTable() returns (int, int, int) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person", Person);
    Person[] personBelow35;
    int count = -1;
    int id1 = -1;
    int id2 = -1;
    if (dt is table<Person>) {
        personBelow35 = dt.filter(function (Person p) returns (boolean) {
                return p.age < 35;
            });
        count = personBelow35.length();
        id1 = personBelow35[0].id;
        id2 = personBelow35[1].id;
    }
    checkpanic testDB.stop();
    return (count, id1, id2);
}

function testFilterTableWithCount() returns (int) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person", Person);
    int count = -1;
    if (dt is table<Person>) {
        count = dt.filter(isBelow35).count();
    }
    checkpanic testDB.stop();
    return count;
}

function testMapTable() returns (string[]) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    string[] names = [];
    if (dt is table<Person>) {
        names = dt.map(getName);
    }
    checkpanic testDB.stop();
    return names;
}

function testMapWithFilterTable() returns (string[]) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });
    var dt = testDB->select("SELECT * from Person order by id", Person);
    string[] names = [];
    if (dt is table<Person>) {
        names = dt.map(getName).filter(isGeraterThan4String);
    }
    checkpanic testDB.stop();
    return names;
}

function testFilterWithMapTable() returns (string[]) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    string[] names = [];
    if (dt is table<Person>) {
        names = dt.filter(isGeraterThan4).map(getName);
    }
    checkpanic testDB.stop();
    return names;
}

function testFilterWithMapAndCountTable() returns (int) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    int count = -1;
    if (dt is table<Person>) {
        count = dt.filter(isGeraterThan4).map(getName).count();
    }
    checkpanic testDB.stop();
    return count;
}

function testAverageWithTable() returns (float) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).average();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testMinWithTable() returns (float) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).min();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testMaxWithTable() returns (float) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).max();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testSumWithTable() returns (float) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT * from Person order by id", Person);
    float avgSal = -1;
    if (dt is table<Person>) {
        avgSal = dt.map(getSalary).sum();
    }
    checkpanic testDB.stop();
    return avgSal;
}

function testCloseConnectionPool() returns (int) {
    h2:Client testDB = new({
        path: "./target/tempdb/",
        name: "TEST_DATA_TABLE__ITR_DB",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 1 }
    });

    var dt = testDB->select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SESSIONS",
        ResultCount);
    int count = -1;
    if (dt is table<ResultCount>) {
        while (dt.hasNext()) {
            var rs = dt.getNext();
            if (rs is ResultCount) {
                count = rs.COUNTVAL;
            }
        }
    }
    checkpanic testDB.stop();
    return count;
}

function testSelect() returns (json) {

    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalary);
    var ret = json.convert(salaryTable);
    json res = {};
    if (ret is json) {
        res = ret;
    } else {
        res = { Error: ret.reason() };
    }
    return res;
}

function testSelectCompatibleLambdaInput() returns (json) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalaryCompatibleInput);
    var ret = json.convert(salaryTable);
    json res = {};
    if (ret is json) {
        res = ret;
    } else {
        res = { Error: ret.reason() };
    }
    return res;
}

function testSelectCompatibleLambdaOutput() returns (json) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalaryCompatibleOutput);
    var ret = json.convert(salaryTable);
    json res = {};
    if (ret is json) {
        res = ret;
    } else {
        res = { Error: ret.reason() };
    }
    return res;
}

function testSelectCompatibleLambdaInputOutput() returns (json) {
    table<Employee> dt = createTable();

    table<EmployeeSalary> salaryTable = dt.select(getEmployeeSalaryCompatibleInputOutput);
    var ret = json.convert(salaryTable);
    json res = {};
    if (ret is json) {
        res = ret;
    } else {
        res = { Error: ret.reason() };
    }
    return res;
}

function getEmployeeSalary(Employee e) returns (EmployeeSalary) {
    EmployeeSalary s = { id: e.id, salary: e.salary };
    return s;
}

function getEmployeeSalaryCompatibleInput(EmployeeCompatible e) returns (EmployeeSalary) {
    EmployeeSalary s = { id: e.id, salary: e.salary };
    return s;
}

function getEmployeeSalaryCompatibleOutput(Employee e) returns (EmployeeSalaryCompatible) {
    EmployeeSalaryCompatible s = { id: e.id, salary: e.salary };
    return s;
}

function getEmployeeSalaryCompatibleInputOutput(EmployeeCompatible e) returns (EmployeeSalaryCompatible) {
    EmployeeSalaryCompatible s = { id: e.id, salary: e.salary };
    return s;
}

function createTable() returns (table<Employee>) {
    table<Employee> dt = table{};

    Employee e1 = { id: 1, name: "A", salary: 100.0 };
    Employee e2 = { id: 2, name: "B", salary: 200.0 };
    Employee e3 = { id: 3, name: "C", salary: 300.0 };

    checkpanic dt.add(e1);
    checkpanic dt.add(e2);
    checkpanic dt.add(e3);

    return dt;
}

function isBelow35(Person p) returns (boolean) {
    return p.age < 35;
}

function getName(Person p) returns (string) {
    return p.name;
}

function getSalary(Person p) returns (float) {
    return p.salary;
}

function isGeraterThan4(Person p) returns (boolean) {
    return p.name.length() > 4;
}

function isGeraterThan4String(string s) returns (boolean) {
    return s.length() > 4;
}
