const https = require('https');
const fs = require('fs');

const repo = 'synyx/urlaubsverwaltung';
const hostname = 'api.github.com';
const path = `/repos/${repo}/releases/latest`;

console.log(`get release-info from: https://${hostname}${path}`);

new Promise(resolve => {
    const options = {
        hostname,
        path,
        headers: {
            'User-Agent': 'node' // needed by github: http://developer.github.com/v3/#user-agent-required
        }
    };

    https.get(options, res => {
        let content = '';

        res.on('data', chunk => {
            content += chunk;
        });

        res.on('end', () => {
            resolve(JSON.parse(content));
        });

    });
}).then(json => {
    const downloadUrl = json.assets[0].browser_download_url;
    const fileName = json.assets[0].name;

    return new Promise(resolve => {
        const file = fs.createWriteStream(fileName);
        https.get(downloadUrl, res => {
            res.pipe(file);

            file.on('finish', () => {
                console.log(`saved last release to ${fileName}`);
                file.close(resolve);
            });
        }).on('error', err => {
            fs.unlink(fileName);
            console.error(err.message);
        });
    });
});
