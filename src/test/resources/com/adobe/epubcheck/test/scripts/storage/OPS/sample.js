function testStorage() {
    if(typeof(Storage)!=="undefined")
    {
        localStorage.lastname="Noble";
        sessionStorage.firstName="Donna";
        return + localStorage.lastname + ", " + sessionStorage.firstName;
    }
    else
    {
        document.getElementById("result").innerHTML="Sorry, your browser does not support web storage...";
    }
}

alert(testEval());