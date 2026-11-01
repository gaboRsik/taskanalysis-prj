#!/usr/bin/env node
// Tests for protect-migrations.js: node .claude/hooks/protect-migrations.test.js
// Runs in the real repository. Nothing is written, the hook only reads. The fixed names below are
// migrations on main; the free version number is derived, so new migrations do not break the tests.

const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const HOOK = path.join(__dirname, 'protect-migrations.js');
const PROJECT = path.resolve(__dirname, '..', '..');
const DIR = 'backend/src/main/resources/db/migration';

const mainNames = spawnSync('git', ['ls-tree', '--name-only', `main:${DIR}`], { cwd: PROJECT, encoding: 'utf8' }).stdout.split(/\r?\n/);
const localNames = fs.readdirSync(path.join(PROJECT, DIR));
const next = Math.max(...[...mainNames, ...localNames].map((name) => Number((name.match(/^V(\d+)__/i) || [])[1] || 0))) + 1;

const cases = [
  // [description, tool_name, file_path, expected exit code]
  ['meglévő migráció szerkesztése', 'Edit', `${PROJECT}/${DIR}/V1__Initial_Schema.sql`, 2],
  ['meglévő migráció felülírása', 'Write', `${PROJECT}/${DIR}/V8__Template_Subtask_Tags.sql`, 2],
  ['MultiEdit meglévőn', 'MultiEdit', `${PROJECT}/${DIR}/V5__Add_Password_Reset_Fields.sql`, 2],
  ['Windows-os útvonal', 'Edit', `${PROJECT.replace(/\//g, '\\')}\\${DIR.replace(/\//g, '\\')}\\V3__Add_Task_Count.sql`, 2],
  ['kisbetűs fájlnév', 'Edit', `${PROJECT}/${DIR}/v2__task_templates.sql`, 2],
  ['nem normalizált útvonal', 'Edit', `${PROJECT}/backend/./src/main/resources/db/migration/V1__Initial_Schema.sql`, 2],
  ['foglalt verziószám új néven', 'Write', `${PROJECT}/${DIR}/V8__Something_Else.sql`, 2],
  ['rossz fájlnév', 'Write', `${PROJECT}/${DIR}/add_column.sql`, 2],
  ['rossz kiterjesztés', 'Write', `${PROJECT}/${DIR}/V${next}__Add_Something.txt`, 2],
  ['kisbetűs előtag új fájlon', 'Write', `${PROJECT}/${DIR}/v${next}__Add_Something.sql`, 2],
  ['nagybetűs kiterjesztés új fájlon', 'Write', `${PROJECT}/${DIR}/V${next}__Add_Something.SQL`, 2],
  ['almappa', 'Write', `${PROJECT}/${DIR}/old/V${next}__Add_Something.sql`, 2],
  ['a main legnagyobb verziója alatti szám', 'Write', `${PROJECT}/${DIR}/V0__Too_Early.sql`, 2],
  ['új migráció szabad számmal', 'Write', `${PROJECT}/${DIR}/V${next}__Add_Something.sql`, 0],
  ['új migráció nagyobb számmal', 'Write', `${PROJECT}/${DIR}/V${next + 3}__Later.sql`, 0],
  ['nem migráció (Java)', 'Edit', `${PROJECT}/backend/src/main/java/com/taskanalysis/service/TemplateService.java`, 0],
  ['nem migráció (properties)', 'Edit', `${PROJECT}/backend/src/main/resources/application.properties`, 0],
  ['teszt-erőforrás', 'Edit', `${PROJECT}/backend/src/test/resources/db/migration/V1__Initial_Schema.sql`, 0],
];

let failed = 0;
for (const [description, tool, filePath, expected] of cases) {
  const input = JSON.stringify({ tool_name: tool, tool_input: { file_path: filePath }, cwd: PROJECT });
  const result = spawnSync('node', [HOOK], { input, env: { ...process.env, CLAUDE_PROJECT_DIR: PROJECT } });
  const ok = result.status === expected;
  if (!ok) failed++;
  console.log(`${ok ? 'OK  ' : 'HIBA'} [${result.status}] ${description}`);
}

// Unreadable input must fail closed
const bad = spawnSync('node', [HOOK], { input: 'nem json' });
if (bad.status !== 2) failed++;
console.log(`${bad.status === 2 ? 'OK  ' : 'HIBA'} [${bad.status}] olvashatatlan bemenet`);

console.log(failed ? `\n${failed} eset hibás` : `\nMind a ${cases.length + 1} eset rendben`);
process.exit(failed ? 1 : 0);
