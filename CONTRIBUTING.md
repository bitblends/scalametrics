# Contributing to ScalaMetrics

Thanks for your interest in improving **ScalaMetrics**! We welcome issues, discussions, and pull requests from everyone. 
By participating, you agree to follow our [Code of Conduct](CODE_OF_CONDUCT.md).

## Guidelines

ScalaMetrics is lightweight and only depends on [ScalaMeta]("https://scalameta.org/") library to analyze the code. The goal 
is to keep the library simple and easy to use. Therefore, we have the following guidelines for contributions:

- **General guidelines**
    - ScalaMeta should maintain a zero-dependency footprint (excluding ScalaMeta). Adding dependencies to the library 
      must be avoided. Exceptions to this rule are `sbt` plugins, test dependencies and in very rare cases which are not
      packaged in the releases.
    - ScalaMeta adheres to basic [Scala Style Guile](https://docs.scala-lang.org/style/) and use `scalaFmt` plugin to enforce a few basic rules.
    - Compiler warnings must be fixed and should not be suppressed. If you are not sure how to fix a warning, please
      open an issue. Exceptions might be made for rare cases related to issues raised due to cross-compilation of the
      code.
    - Avoid leaving TODO or FIXME in the code.
    - We cross-build for **Scala 2.12.x**, **Scala 2.13.x** and **Scala 3.3.x**. Keep code portable.
- **API design**
    - Avoid breaking changes. If unavoidable, discuss first and mark clearly.
- **Naming & docs**
    - All public **and** private types/methods and any nested types/methods must be documented with examples using
      Scaladoc comments (purpose, params, complexity.)
- **Testing**
    - Use **ScalaTest** for behavior tests when suitable.
    - Cover success + edge cases + failure modes. Use the `sbt-coverage` plugin to check the test coverage.
    - Keep tests deterministic; seed property tests where necessary.
---

## Quick Start

Read the [Development](DEVELOPMENT.md) guide for basic development environment setup.

Open a PR from your fork and include:

- Tests for new/changed behavior
- Docs/Scaladoc updates
- A DCO sign-off in your commit(s)
---

## How can I Help?

- **Bug reports:** Use the *Bug report* template. Provide a minimal reproduction (code + versions).
- **Feature requests:** Start a Discussion first; include motivation, API sketch, prior art.
- **Documentation:** Typos, examples, and API clarifications are great first PRs.
- **Good first issues:** Look for labels `good first issue`, `help wanted`, and `milestones`.

---

## Development Environment

- **JDK:** 17+
- **Scala:** 2.12.x, 2.13.x and 3.x.x
- **sbt:** latest stable

> CI runs the same checks. If it fails locally, it will fail in CI.

---

## Commit & PR process

- **Small PRs** are easier to review.
- **Conventional Commits** style is encouraged, use feature, fix, docs, style, refactor, test, chore, etc. so they get picked up by auto labeler in CICD, for example:
    - `feat: add complexity metric for functions`
    - `bugfix: handle empty source files`
    - `docs: clarify usage of MetricsAnalyzer`
- Keep PRs focused to a single concern
- Update `doc/` and `microsite/` when applicable.
- CI will enforce the checklist below, but ensure the checklist passes locally before opening a PR.

### PR Checklist

- [ ] `sbt +scalafmtCheckAll` passes
- [ ] `sbt +scalafixAll` applied to fix
- [ ] `sbt headerCreate` to add missing headers to source files
- [ ] `sbt +test` green (2.12, 2.13, & 3)
- [ ] Public APIs documented; examples updated if needed
- [ ] Commit(s) signed off per DCO (see below)

---

## Legal: License & DCO

- License: MIT (see [LICENSE](LICENSE)).
- Provenance: We use the **Developer Certificate of Origin (DCO)

Sign off each commit with:

```
Signed-off-by: Your Name <you@example.com>
```

You can add the sign-off automatically:

```bash
git commit -s -m "feat: added automatic dialect detection"
```

If you authored code that includes third-party material, ensure its license is compatible with MIT and attribute accordingly.

---

## Security

If you believe you’ve found a vulnerability, **DO NOT OPEN A PUBLIC ISSUE**. Please email <b>scalametrics&commat;bitblends.com</b> for private disclosure.

---

## Release Process (maintainers)

Releases are performed via tags and `sbt-ci-release`. On `vX.Y.Z` tag:

- CI publishes artifacts to Sonatype.

Community members: if you need a release, please comment on the relevant issue.

---

## Community & support

- **Bugs/Features/Questions:** GitHub Issues

We appreciate your time and contributions—thank you for helping make **ScalaMetrics** better!
