require('colors');
const fs = require('fs');
const shell = require('shelljs');
const readline = require('readline-sync');
const path = require('path');
const version = process.argv.slice(2)[0];
let nextDevVersion = process.argv.slice(2)[1];

if (!version) {
    const fileName = path.basename(__filename);
    console.log('usage:');
    console.log(`   ${fileName} x.y.z [x.y.a-SNAPSHOT]`);
    console.log('example:');
    console.log(`   ${fileName} 2.23.42 [2.23.43-SNAPSHOT]`);
    console.log('');
    console.log('from that a tag of the form urlaubsverwaltung-x.y.z will be created. And the new');
    console.log('development-version will be set to 2.23.43-SNAPSHOT');
    console.log('');
    process.exit(-1);
}
const tagName = `urlaubsverwaltung-${version}`;

shell.echo('identify current branch name'.yellow);
const d = shell.exec('git rev-parse --abbrev-ref HEAD');
const branchName = d.stdout.trim();

const content = fs.readFileSync('CHANGELOG.md', 'utf-8');
let headlineCounter = 0;
const releaseNotes = content.split('\n').filter(line => {
    if (line.startsWith('#')) {
        headlineCounter++;
    } else {
        if (headlineCounter === 1) {
            return true;
        }
    }
    return false
}).join('\n');

if (!nextDevVersion) {
    const versionParts = version.split('.');
    const versionMajor = versionParts[0];
    const versionMinor = versionParts[1];
    const versionBugfix = Number(versionParts[2]);
    nextDevVersion = `${versionMajor}.${versionMinor}.${versionBugfix+1}-SNAPSHOT`;
}

shell.echo('do you want to release?'.yellow);
shell.echo('');
shell.echo('tag:'.yellow);
shell.echo(`  ${tagName}`.blue);
shell.echo('releaseNotes:'.yellow);
shell.echo(`${releaseNotes}`.blue);
shell.echo('next development version'.yellow);
shell.echo(` ${nextDevVersion}`.blue);

const startRelease = readline.keyInYN('start release-process?');
if (!startRelease) {
    shell.echo('do not release'.yellow);
    process.exit(1);
}

shell.echo('setting version'.yellow);
if (shell.exec(`./mvnw org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=${version}`).code !== 0) {
    shell.echo('setting version failed');
    shell.exit(1);
}

shell.echo('adding pom');
if (shell.exec(`git add pom.xml`).code !== 0) {
    shell.echo('add failed');
    shell.exit(1);
}

shell.echo('commit new release version');
if (shell.exec(`git commit -m "create release urlaubsverwaltung-${version}"`).code !== 0) {
    shell.echo('commit failed');
    shell.exit(1);
}

shell.echo('create release tag');
if (shell.exec(`git tag -a ${tagName} -m "Release ${version}"`).code !== 0) {
    shell.echo('tag failed');
    shell.exit(1);
}

shell.echo('push commit and tag');
if (shell.exec(`git push origin ${tagName} ${branchName}`).code !== 0) {
    shell.echo('push failed');
    shell.exit(1);
}

shell.echo('updating new snapshots');
if (shell.exec(`./mvnw org.codehaus.mojo:versions-maven-plugin:2.5:set -DnewVersion=${nextDevVersion}`).code !== 0) {
    shell.echo('update to snapshot-version failed');
    shell.exit(1);
}


shell.echo('adding pom');
if (shell.exec(`git add pom.xml`).code !== 0) {
    shell.echo('add failed');
    shell.exit(1);
}

shell.echo('commit new snapshot version');
if (shell.exec(`git commit -m "create new snapshot-version: ${nextDevVersion}"`).code !== 0) {
    shell.echo('snapshot commit failed');
    shell.exit(1);
}

shell.echo('push snapshot commit');
if (shell.exec(`git push origin ${branchName}`).code !== 0) {
    shell.echo('push of snapshot failed');
    shell.exit(1);
}

shell.echo('');
shell.echo('everything went well, travis should do the rest'.yellow);
shell.echo('');
