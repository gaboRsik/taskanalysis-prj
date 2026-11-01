#!/usr/bin/env node
// Tests for readonly-bash.js: node .claude/hooks/readonly-bash.test.js
// The commands are only passed to the hook as JSON, they are never executed.

const { spawnSync } = require('child_process');
const path = require('path');

const HOOK = path.join(__dirname, 'readonly-bash.js');

const ALLOWED = [
  'git status',
  'git status --short',
  'git diff main...HEAD',
  'git diff HEAD',
  'git diff --stat HEAD~1',
  'git log --oneline -5',
  'git show HEAD:backend/pom.xml',
  'git ls-files',
  'git rev-parse --abbrev-ref HEAD',
  'git merge-base main HEAD',
  'git blame frontend/src/styles.scss',
  'git branch',
  'git branch --show-current',
  'git branch -a',
  'ls -la .claude/hooks',
  'pwd',
  'git status --short && git diff HEAD',
  'git log --oneline | git status',
  'git log --oneline -3 -- .env.example',
  'git diff HEAD -- backend/.env.example',
  'git log -Sneedle --oneline',
  'git diff main..HEAD',
];

const BLOCKED = [
  // Write commands
  'git commit --allow-empty -m hook-teszt',
  'git push',
  'git stash',
  'git switch main',
  'git branch -D main',
  'git -C backend status',
  'git -c core.pager=x log',
  'rm -rf backend',
  // Hidden commands
  'ls & rm -rf backend',
  'git status & git push',
  'ls&rm',
  'cat <(rm -rf backend)',
  'ls $(rm -rf backend)',
  'ls `rm -rf backend`',
  'FOO=bar git status',
  // Redirection
  'git diff > out.txt',
  'git diff >out.txt',
  'git log 2>/dev/null',
  // Reading .env
  'cat .env',
  'cat .env|head',
  'cat .en?',
  'cat .e""nv',
  'cat .\\env',
  'ls .env',
  'git show HEAD:.env',
  'git diff -- backend/.env.local',
  'git diff -- .ENV',
  'git diff -- .env.example.bak',
  'git blame -S.env README.md',
  'git blame -S.gitignore README.md',
  'git blame -wS.gitignore README.md',
  'git blame --ignore-revs-file=.gitignore README.md',
  'git diff -O.gitignore',
  'grep -r PASSWORD .',
  // Paths outside the work tree (git diff switches to no-index mode)
  'git diff /tmp/empty .',
  'git diff C:/temp/empty .',
  'git diff ~/empty .',
  'git diff ../empty .',
  'git diff backend/../../empty .',
  'git diff --stat=/tmp/x',
  'cd backend && git status',
  'cd /tmp && git diff a b',
  // Removed programs
  'find . -delete;pwd',
  'find . -fprint x',
  'tree -o out.txt',
  'file -C -m x',
  'cat README.md',
  // Git options that write files, run programs or read outside git
  'git diff --output=x',
  'git diff --outpu=x',
  'git diff --out""put=x',
  'git diff --ext-diff',
  'git show --textconv HEAD',
  'git diff --no-index /dev/null x',
  'git blame --contents=x README.md',
];

function run(stdin) {
  return spawnSync(process.execPath, [HOOK], { input: stdin, encoding: 'utf8' }).status;
}

const bash = (command) => JSON.stringify({ tool_name: 'Bash', tool_input: { command } });

const MALFORMED = [
  ['üres bemenet', ''],
  ['nem JSON', 'nem json'],
  ['null', 'null'],
  ['üres objektum', '{}'],
  ['hiányzó tool_input', '{"tool_name":"Bash"}'],
  ['nem szöveges parancs', '{"tool_name":"Bash","tool_input":{"command":1}}'],
  ['üres parancs', bash('   ')],
  ['csak elválasztó', bash(';')],
];

let failed = 0;
function expect(label, stdin, status) {
  const actual = run(stdin);
  if (actual !== status) {
    failed += 1;
    console.error(`HIBA: ${label}: várt ${status}, kapott ${actual}`);
  }
}

for (const command of ALLOWED) expect(command, bash(command), 0);
for (const command of BLOCKED) expect(command, bash(command), 2);
for (const [label, stdin] of MALFORMED) expect(label, stdin, 2);

const total = ALLOWED.length + BLOCKED.length + MALFORMED.length;
console.log(`${total} eset, ${total - failed} sikeres, ${failed} hibás`);
process.exit(failed ? 1 : 0);
