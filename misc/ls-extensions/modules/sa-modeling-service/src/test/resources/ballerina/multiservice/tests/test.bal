import ballerina/io;
import ballerina/test;

function beforeFunc() {
    io:println("I'm the before function!");
}
@test:Config {
    before: beforeFunc,
    after: afterFunc
}
function testFunction() {
    io:println("I'm in test function!");
    test:assertTrue(true, msg = "Failed!");
}
function afterFunc() {
    io:println("I'm the after function!");
}