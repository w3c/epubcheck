function format(string,args) {
  return string.replace(/{(\d+)}/g, function(match, number) { 
    return typeof args[number] != 'undefined'
      ? args[number]
      : match
    ;
  });
}

module.exports = {
  
  // Log a formatted string
  log(value, ...args) {
    var params = args.map(arg=> {
      return (Object.prototype.toString.call(arg) === "[object String]")?arg:JSON.stringify(arg,null,2)
    })
    console.log(format(value,params))
  }
}