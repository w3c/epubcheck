function log(message) {
    var client = new XMLHttpRequest();
    client.open("POST", "/log");
    client.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
    client.send(message);
}

log("Book being read!");