console.log('start');

const http = require('http');

console.log('create server');
http.createServer(function (req, res) {
  res.write('waiting for dyno!');
  res.end();
}).listen(5000);

console.log('started');
