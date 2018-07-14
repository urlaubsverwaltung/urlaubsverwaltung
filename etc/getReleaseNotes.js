/**
 * Filter out release notes from release-notes.md
 *
 * It will only print out the content under the first headline.
 */

const fs = require('fs');
const marked = require('marked');

const content = fs.readFileSync('CHANGELOG.md', 'utf-8');

let headlineCounter = 0;

const notes = content.split('\n').filter(line => {
    if (line.startsWith('#')) {
        headlineCounter++;
    } else {
        if (headlineCounter === 1) {
            return true;
        }
    }
    return false
}).join('\n');

const singleLineNotes = marked(notes).replace(/\n/g, '');

console.log(singleLineNotes);