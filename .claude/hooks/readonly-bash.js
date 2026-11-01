#!/usr/bin/env node
// PreToolUse hook for the code-reviewer subagent.
// Allowlist only: read-only git commands, ls and pwd. Everything else is blocked
// with exit code 2. Any other non-zero exit would let the command run, so every
// failure path must end in block().
// Input: the hook event as JSON on stdin (tool_name, tool_input.command, ...).

const GIT_READ = new Set(['status', 'diff', 'log', 'show', 'ls-files', 'rev-parse', 'merge-base', 'blame']);
const GIT_BRANCH_LIST = new Set(['--list', '-a', '-r', '-v', '-vv', '--show-current']);

// Only plain words pass, so the shell has nothing to expand or redirect: no quotes,
// backslash, globs, $, backtick, <, >, parentheses, braces or a single &.
const SAFE_WORD = /^[A-Za-z0-9_./:=@^~+,%-]+$/;

// Git accepts abbreviated long options, so these match on prefixes:
// --output (writes a file), --ext-diff and --textconv (run programs),
// --no-index and --contents (read files outside git).
// -O (diff) reads an order file; short options can be clustered, as in -wO<file>.
const GIT_FORBIDDEN_OPTION = /^(--ou|--ext|--textc|--no-i|--con|-[A-Za-z]*O)/;

// git blame -S<file> and --ignore-revs-file=<file> print the lines of any file in errors.
const BLAME_FORBIDDEN_OPTION = /^(-[A-Za-z]*S|--ig)/;

// .env anywhere in a word, in any case (NTFS ignores case). Only .env.example is exempt.
const ENV_FILE = /\.env/i;
const ENV_EXAMPLE = /(^|[/:=])\.env\.example$/;

// git diff silently switches to no-index mode for a path outside the work tree,
// which reads any file there and ignores .gitignore. So paths must stay relative.
const OUTSIDE_REPO = /(^|[=:])[/~]|^[A-Za-z]:|(^|[/=:])\.\.([/]|$)/;

function isSafeWord(word) {
  return SAFE_WORD.test(word)
    && !OUTSIDE_REPO.test(word)
    && !ENV_FILE.test(word.replace(ENV_EXAMPLE, '$1'));
}

function block(reason) {
  process.stderr.write(
    `A code-reviewer csak olvasó parancsot futtathat. Elutasítva: ${reason}\n` +
    'Engedélyezett: git status/diff/log/show/ls-files/rev-parse/merge-base/blame, ' +
    'git branch (listázás), ls, pwd, egyszerű szavakkal (idézőjel, helyettesítő karakter ' +
    'és átirányítás nélkül), a repón belüli relatív utakkal. A cd és a git -C nem engedélyezett. ' +
    'Fájlokat a Read, Grep és Glob eszközzel olvass.\n'
  );
  process.exit(2);
}

function isAllowed(words) {
  const [program, sub, ...rest] = words;
  if (program === 'ls') return true;
  if (program === 'pwd') return words.length === 1;
  if (program !== 'git') return false;
  if (sub === 'blame' && rest.some((word) => BLAME_FORBIDDEN_OPTION.test(word))) return false;
  if (GIT_READ.has(sub)) return !rest.some((word) => GIT_FORBIDDEN_OPTION.test(word));
  if (sub === 'branch') return rest.every((word) => GIT_BRANCH_LIST.has(word));
  return false;
}

function check(raw) {
  const input = JSON.parse(raw);
  if (input.tool_name !== 'Bash') block('ismeretlen eszköz');

  const command = input.tool_input.command;
  if (typeof command !== 'string' || !command.trim()) block('üres parancs');

  // Every part of a compound command (&&, ||, ;, |, newline) must be allowed on its own
  const parts = command.split(/&&|\|\||;|\||\n/).map((part) => part.trim()).filter(Boolean);
  if (parts.length === 0) block('üres parancs');
  for (const part of parts) {
    const words = part.split(/\s+/);
    if (!words.every(isSafeWord) || !isAllowed(words)) {
      block(`"${part}"`);
    }
  }
}

let raw = '';
process.stdin.on('data', (chunk) => (raw += chunk));
process.stdin.on('error', () => block('a hook bemenete nem olvasható'));
process.stdin.on('end', () => {
  try {
    check(raw);
  } catch {
    block('a hook bemenete nem olvasható');
  }
  process.exit(0);
});
