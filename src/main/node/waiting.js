const http = require('http');


http.createServer(function (req, res) {
  res.write('waiting for dyno!');
  res.end();
}).listen(5000);

console.log('started');
