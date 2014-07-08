function testEval() {
    var str = "if ( a ) { 1+1; } else { 1+2; }";
    var a = true;
    return eval (str);
}

alert(testEval());