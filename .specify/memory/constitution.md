<!--
Sync Impact Report:
- Version Change: None → v1.0.0 (Initial version)
- Modified Principles: None (New constitution)
- Added Sections: Project Metadata, 5 Core Principles, Governance Framework
- Removed Sections: None
- Templates Requiring Updates:
  ✅ plan-template.md - Already includes Constitution Check section
  ✅ spec-template.md - Aligned with constitution requirements
  ✅ tasks-template.md - Task categorization reflects principles
  ✅ commands/*.md - No updates needed
- Follow-up TODOs: None
-->

# 吃点啥 (EatWhat) Constitution

## Core Principles

### I. Compose First

**Declaration**: This project MUST be built entirely with Jetpack Compose. Traditional View-based UI components are strictly prohibited.

**Requirements**:
- All UI components MUST use Compose declarative syntax
- NO XML layouts, NO View classes, NO findViewById
- Follow Compose best practices: unidirectional data flow, state hoisting, recomposition optimization
- Use Compose Material 3 components exclusively

**Rationale**: Jetpack Compose represents the modern Android UI toolkit. Mixing View and Compose creates technical debt, increases complexity, and reduces maintainability. A pure Compose approach ensures consistency, leverages the latest Android capabilities, and provides the best developer experience.

### II. State Management Excellence

**Declaration**: All state management MUST use the ComposeHooks library (https://github.com/junerver/ComposeHooks). Direct mutableStateOf usage is discouraged except for simple local state.

**Requirements**:
- Use ComposeHooks for complex state logic (useState, useEffect, useRequest, etc.)
- Follow state hoisting principles: state flows down, events flow up
- Implement single source of truth for each piece of state
- Document state dependencies and side effects clearly

**Rationale**: ComposeHooks provides React-like hooks for Compose, offering a familiar and powerful state management pattern. This standardization prevents fragmented state management approaches, improves code readability, and makes state logic more testable and reusable.

### III. Material Design Consistency

**Declaration**: The UI MUST strictly follow Material Design 3 (Material You) guidelines. All visual elements MUST use the Material 3 design system.

**Requirements**:
- Use Material 3 components from androidx.compose.material3
- Follow Material Design color system, typography, and spacing guidelines
- Implement dynamic color theming where appropriate
- Maintain consistent interaction patterns (ripples, elevation, motion)
- Ensure accessibility compliance (contrast ratios, touch targets, screen readers)

**Rationale**: Material Design 3 provides a comprehensive, tested design system that ensures visual consistency, accessibility, and platform integration. Adhering to MD3 reduces design decisions, improves user familiarity, and ensures the app feels native to Android.

### IV. User-Centric Simplicity

**Declaration**: The app MUST prioritize simplicity and ease of use. Every feature MUST justify its existence by solving a real user problem.

**Requirements**:
- Minimize cognitive load: clear information hierarchy, obvious actions
- Reduce steps to complete core tasks
- Provide immediate feedback for user actions
- Use familiar patterns and conventions
- Avoid feature bloat: say no to "nice-to-have" features that complicate the core experience

**Rationale**: "吃点啥" (What to Eat) addresses a simple daily decision. The app must be as simple as the problem it solves. Complexity kills usability. Every additional feature, option, or screen increases friction and reduces the likelihood users will engage with the app.

### V. Code Quality & Maintainability

**Declaration**: Code MUST be written for humans first, machines second. Readability, testability, and documentation are non-negotiable.

**Requirements**:
- Write self-documenting code: clear naming, logical structure, appropriate abstraction
- Add comments ONLY where the "why" isn't obvious from the code itself
- Keep functions small and focused (single responsibility)
- Write testable code: avoid tight coupling, use dependency injection
- Document public APIs and complex algorithms
- Follow Kotlin coding conventions and idioms

**Rationale**: Code is read far more often than it's written. Maintainability directly impacts development velocity and bug rates. Clear, testable code reduces onboarding time, makes debugging easier, and enables confident refactoring. This is especially critical for a project that may evolve over time.

## Technical Constraints

### Technology Stack

**Required**:
- Language: Kotlin (latest stable version)
- UI Framework: Jetpack Compose (latest stable version)
- State Management: ComposeHooks library
- Design System: Material Design 3 (androidx.compose.material3)
- Minimum SDK: 24 (Android 7.0)
- Target SDK: Latest stable Android SDK

**Prohibited**:
- Traditional View system (XML layouts, View classes)
- Mixed View/Compose architectures
- Deprecated Android APIs
- Third-party UI component libraries that conflict with Material Design

### Performance Standards

- App launch time: < 2 seconds on mid-range devices
- UI interactions: 60 FPS minimum, no dropped frames during animations
- Memory usage: Efficient memory management, no memory leaks
- APK size: Keep minimal, avoid unnecessary dependencies

### Code Organization

- Follow standard Android project structure
- Group code by feature, not by layer (feature-first organization)
- Keep related code close together
- Use clear package naming that reflects functionality

## Development Workflow

### Code Review Requirements

- All code changes MUST be reviewed for:
  - Compliance with constitution principles
  - Code quality and readability
  - Proper use of ComposeHooks for state management
  - Material Design 3 adherence
  - Performance implications

### Testing Standards

- Write tests for complex business logic
- Test state management logic independently
- Verify UI behavior with Compose testing utilities
- Test edge cases and error scenarios

### Documentation Requirements

- Document architectural decisions
- Maintain up-to-date README with setup instructions
- Document complex algorithms or non-obvious code
- Keep constitution updated as project evolves

## Governance

### Amendment Process

This constitution can be amended through the following process:

1. **Proposal**: Document the proposed change with rationale
2. **Review**: Evaluate impact on existing code and principles
3. **Approval**: Requires explicit approval and documentation
4. **Migration**: Create migration plan if existing code is affected
5. **Version Update**: Increment version according to semantic versioning:
   - MAJOR: Backward-incompatible principle changes
   - MINOR: New principles or significant expansions
   - PATCH: Clarifications, wording improvements, typo fixes

### Compliance Verification

- All pull requests MUST verify compliance with this constitution
- Code reviews MUST check adherence to core principles
- Complexity that violates principles MUST be justified and documented
- Violations MUST be addressed before merging

### Conflict Resolution

When conflicts arise between principles or between principles and practical constraints:

1. User-Centric Simplicity takes precedence over feature completeness
2. Code Quality & Maintainability takes precedence over short-term velocity
3. Material Design Consistency takes precedence over custom design preferences
4. Document any necessary compromises and their justification

**Version**: v1.0.0 | **Ratified**: 2025-12-05 | **Last Amended**: 2025-12-05
