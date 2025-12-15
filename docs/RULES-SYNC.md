# AI Tools Rules Synchronization Guide

## Overview

This project uses a **Single Source of Truth** approach for AI development tool rules to ensure consistency across different AI assistants.

## Architecture

```
📋 AGENTS.md (Root)
    ↓ Referenced by
    ├── .kilocode/rules.md     (Kilocode)
    ├── CLAUDE.md              (Claude Code)
    └── [future tools]         (Other AI tools)
```

## File Purposes

### [`AGENTS.md`](../AGENTS.md) - Single Source of Truth

- **Purpose**: Master rule file for all AI development tools
- **Audience**: All AI assistants working on this project
- **Content**: Complete project rules, architecture, patterns, and guidelines
- **Updates**: ✅ Edit this file when rules change

### [`.kilocode/rules.md`](../.kilocode/rules.md) - Kilocode Reference

- **Purpose**: Kilocode-specific wrapper
- **Audience**: Kilocode AI assistant
- **Content**: Reference to AGENTS.md + tool-specific notes
- **Updates**: ❌ Do not edit unless adding Kilocode-specific notes

### [`CLAUDE.md`](../CLAUDE.md) - Claude Code Reference

- **Purpose**: Claude Code backward compatibility
- **Audience**: Claude Code assistant
- **Content**: Reference to AGENTS.md + quick reference
- **Updates**: ❌ Do not edit unless adding Claude-specific notes

## Benefits

### ✅ Advantages

1. **Consistency**: All AI tools follow the same rules
2. **Maintainability**: Update once, applies everywhere
3. **Scalability**: Easy to add new AI tools
4. **Version Control**: Single clear history of rule changes
5. **No Duplication**: Eliminates sync issues between files

### ❌ Problems Solved

- **Before**: Multiple rule files could drift out of sync
- **Before**: Unclear which file is authoritative
- **Before**: Rule changes needed updates in multiple places
- **After**: Single file, single truth, automatic consistency

## How to Update Rules

### Step 1: Edit AGENTS.md

```bash
# Open the master file
vim AGENTS.md  # or your favorite editor
```

### Step 2: Make Your Changes

Update the relevant sections in `AGENTS.md`:

- Architecture principles
- Technology stack
- Code patterns
- Database guidelines
- Testing requirements
- etc.

### Step 3: Update Change Log

Add your change to the "Recent Updates" section at the bottom of `AGENTS.md`:

```markdown
### Recent Updates

- YYYY-MM-DD: Brief description of what changed
```

### Step 4: Commit

```bash
git add AGENTS.md
git commit -m "docs: update [section] in AGENTS.md"
```

### Step 5: Verify

All tool-specific files automatically reference the updated `AGENTS.md`. No additional changes needed!

## Adding a New AI Tool

When adding a new AI development tool to the project:

### 1. Create Tool-Specific Directory (Optional)

```bash
mkdir .newtool
```

### 2. Create Reference File

```bash
touch .newtool/rules.md
```

### 3. Add Reference Content

```markdown
# [Tool Name] Rules

> **⚠️ IMPORTANT**: This file references the unified rule set at [`../AGENTS.md`](../AGENTS.md).
>
> The `AGENTS.md` file is the **single source of truth** for all AI development tools.
> Changes to project rules should be made in `AGENTS.md` to ensure consistency across all tools.

---

## For [Tool Name] Users

This file provides [Tool Name]-specific context. For complete project rules, see [`AGENTS.md`](../AGENTS.md).

[Tool-specific configuration or notes here]
```

### 4. Update AGENTS.md Reference List

Add the new tool to `AGENTS.md` introduction or change log.

## Verification Checklist

When you update rules, verify:

- [ ] Changes are in `AGENTS.md`
- [ ] "Recent Updates" section is updated
- [ ] Tool-specific files still reference `AGENTS.md`
- [ ] No duplicated rules in tool-specific files
- [ ] Changes are committed with clear message

## Common Mistakes to Avoid

### ❌ DON'T: Edit tool-specific files directly

```bash
# Wrong!
vim .kilocode/rules.md  # Adding new architecture rule
vim CLAUDE.md           # Adding new pattern
```

### ✅ DO: Edit AGENTS.md

```bash
# Correct!
vim AGENTS.md  # Add all rule changes here
```

### ❌ DON'T: Duplicate rules across files

```markdown
<!-- Wrong! Don't copy rules to multiple files -->

.kilocode/rules.md: "Use Compose..."
CLAUDE.md: "Use Compose..."
AGENTS.md: "Use Compose..."
```

### ✅ DO: Reference from one source

```markdown
<!-- Correct! Reference from single source -->

.kilocode/rules.md: "See AGENTS.md for rules"
CLAUDE.md: "See AGENTS.md for rules"
AGENTS.md: "Use Compose..." ← Only place with actual rules
```

## FAQ

### Q: Why not use multiple separate rule files?

**A**: Multiple files lead to:

- Rules drifting out of sync
- Confusion about which file is authoritative
- Duplicate maintenance effort
- Inconsistent behavior across AI tools

### Q: What if a tool needs specific configuration?

**A**: Add tool-specific notes in the tool's directory, but keep project rules in `AGENTS.md`:

```markdown
# Tool-specific file

See AGENTS.md for rules.

## Tool-Specific Notes

- Configuration for this tool
- Special commands
- Tool quirks
```

### Q: How do I know if files are in sync?

**A**: They're always in sync because tool-specific files only reference `AGENTS.md`, they don't duplicate its content.

### Q: What happens to old rule files?

**A**:

- Keep them for backward compatibility
- Update them to reference `AGENTS.md`
- Eventually deprecate if the tool supports direct `AGENTS.md` reading

## Summary

```
┌─────────────────────────────────────────┐
│         DO THIS                         │
├─────────────────────────────────────────┤
│ 1. Edit AGENTS.md                       │
│ 2. Update change log                    │
│ 3. Commit                               │
│ ✅ Done! All tools stay in sync        │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         DON'T DO THIS                   │
├─────────────────────────────────────────┤
│ ❌ Edit .kilocode/rules.md directly    │
│ ❌ Edit CLAUDE.md directly             │
│ ❌ Copy rules between files            │
│ ❌ Maintain separate rule versions     │
└─────────────────────────────────────────┘
```

## Support

If you have questions about the rules system:

1. Read this guide
2. Check `AGENTS.md` structure
3. Look at `.kilocode/README.md` for examples
4. Ask the team if still unclear
