# Kilocode Rules Directory

This directory contains Kilocode-specific configuration for the EatWhat Android application.

## ⚠️ Single Source of Truth: AGENTS.md

**All project rules are maintained in [`../AGENTS.md`](../AGENTS.md)** to ensure consistency across all AI development tools.

### Rule File Hierarchy

```
AGENTS.md                    ← 📋 Single Source of Truth (edit this!)
├── .kilocode/rules.md      ← References AGENTS.md
├── CLAUDE.md               ← References AGENTS.md
└── [other tool configs]    ← Should reference AGENTS.md
```

## Files

### [`rules.md`](rules.md)

Kilocode-specific wrapper that references [`../AGENTS.md`](../AGENTS.md). Contains:

- Link to unified rules
- Kilocode-specific quick references
- Tool-specific notes (if any)

## Usage

### For AI Development Tools

All AI tools should read [`../AGENTS.md`](../AGENTS.md) as the primary rule source.

### For Developers

1. **Read** [`../AGENTS.md`](../AGENTS.md) for complete project rules
2. **Edit** [`../AGENTS.md`](../AGENTS.md) when updating rules
3. Tool-specific files will automatically reference the latest rules

## Benefits of This Approach

✅ **Single Source of Truth** - No rule duplication or inconsistency  
✅ **Easy Maintenance** - Update once, applies to all tools  
✅ **Tool Agnostic** - Add new AI tools without duplicating rules  
✅ **Version Control** - Clear change history in one file

## Updates

When updating rules:

1. ✏️ Edit [`../AGENTS.md`](../AGENTS.md) with your changes
2. 📝 Update the "Recent Updates" section in `AGENTS.md`
3. ✅ Tool-specific files automatically reference the latest version

**DO NOT edit** `.kilocode/rules.md` or `CLAUDE.md` directly unless adding tool-specific notes.
