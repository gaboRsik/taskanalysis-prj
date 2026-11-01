#!/usr/bin/env node
// PreToolUse hook (Edit|Write|MultiEdit) for the whole project.
// Flyway migrations that already exist on main are applied in production and must never change:
// production runs validate-on-migrate=true, so a changed checksum stops the app from starting.
// - Editing or overwriting a migration that exists on main (or origin/main): blocked.
// - Creating a new migration whose version number is already used (locally or on main): blocked.
// - Creating a new migration below the highest version on main: blocked (out-of-order is off).
// - Anything in a subfolder of the migration directory: blocked (Flyway scans subfolders too).
// - Creating a new migration with a free, higher version number: allowed.
// Every failure path ends in block() with exit code 2 (fail closed); any other non-zero
// exit would let the edit through.

const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const MIGRATION_DIR = 'backend/src/main/resources/db/migration';
// Anything below the migration directory, at any depth (Flyway scans subfolders too)
const MIGRATION_FILE = /(^|\/)backend\/src\/main\/resources\/db\/migration\/(.+)$/i;
const VERSION = /^V(\d+)__.+\.sql$/i;

function block(reason) {
  process.stderr.write(
    `Flyway védelem: ${reason}\n` +
    'A már lefuttatott migrációt soha nem szabad módosítani (élesben validate-on-migrate=true). ' +
    `Séma-változáshoz hozz létre új migrációt a következő szabad verziószámmal a ${MIGRATION_DIR} mappában.\n`
  );
  process.exit(2);
}

function filesOnBranch(projectDir, branch) {
  const result = spawnSync('git', ['ls-tree', '--name-only', `${branch}:${MIGRATION_DIR}`], { cwd: projectDir, encoding: 'utf8' });
  if (result.status !== 0) block(`a ${branch} ág migrációs mappája nem olvasható.`);
  return result.stdout.split(/\r?\n/).filter(Boolean);
}

function versionOf(name) {
  const m = name.match(VERSION);
  return m ? Number(m[1]) : null;
}

function branchExists(projectDir, branch) {
  const result = spawnSync('git', ['rev-parse', '--verify', '--quiet', branch], { cwd: projectDir });
  return result.status === 0;
}

function check(raw) {
  const input = JSON.parse(raw);
  // Normalized so that "." and ".." segments cannot slip past MIGRATION_FILE
  const filePath = path.posix.normalize(String((input.tool_input && input.tool_input.file_path) || '').replace(/\\/g, '/'));
  const match = filePath.match(MIGRATION_FILE);
  if (!match) process.exit(0); // not a migration: nothing to check

  const relative = match[2];
  if (relative.includes('/')) block(`a ${relative} a migrációs mappa egy almappájában van. A Flyway az almappákat is beolvassa, ezért ide nem kerülhet fájl.`);
  const fileName = relative;
  const projectDir = process.env.CLAUDE_PROJECT_DIR || input.cwd || process.cwd();

  const branches = ['main', 'origin/main'].filter((b) => branchExists(projectDir, b));
  if (branches.length === 0) block('nem található a main ág, így nem ellenőrizhető, hogy a migráció le van-e futtatva.');

  const onMain = branches.flatMap((b) => filesOnBranch(projectDir, b));
  // Case-insensitive on purpose: on Windows (NTFS) v2__x.sql and V2__X.sql are the same file
  if (onMain.some((name) => name.toLowerCase() === fileName.toLowerCase())) {
    block(`a ${fileName} már szerepel a main ágon, tehát le van futtatva.`);
  }

  // A new file: valid name, free version number, higher than everything on main
  const version = versionOf(fileName);
  // Case-sensitive here: a new migration must use the exact form, whatever the file system allows
  if (version === null || !/^V\d+__.+\.sql$/.test(fileName)) block(`a ${fileName} neve nem felel meg a V<szám>__Leiras.sql formának.`);

  const local = fs.readdirSync(path.join(projectDir, MIGRATION_DIR))
    .filter((name) => name.toLowerCase() !== fileName.toLowerCase());
  const taken = [...onMain, ...local].map(versionOf).filter((v) => v !== null);
  const next = Math.max(0, ...taken) + 1;
  if (taken.includes(version)) {
    block(`a V${version} verziószámot már egy másik migráció használja. A következő szabad szám: V${next}.`);
  }
  const maxOnMain = Math.max(0, ...onMain.map(versionOf).filter((v) => v !== null));
  if (version < maxOnMain) {
    block(`a V${version} kisebb, mint a main ág legnagyobb verziója (V${maxOnMain}), a sorrenden kívüli migráció ki van kapcsolva. A következő szabad szám: V${next}.`);
  }
  process.exit(0);
}

let raw = '';
process.stdin.on('data', (chunk) => (raw += chunk));
process.stdin.on('error', () => block('a hook bemenete nem olvasható.'));
process.stdin.on('end', () => {
  try {
    check(raw);
  } catch (e) {
    block(`belső hiba a hookban (${e.message}).`);
  }
});
