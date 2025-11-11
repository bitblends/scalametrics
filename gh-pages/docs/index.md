---
title: ScalaMetrics
description: The most comprehensive code metrics and analysis library for Scala
keywords: [ScalaMetrics, code metrics, static analysis, Scala, cyclomatic complexity, code quality, ScalaMeta]
layout: doc
toc: true
---

<style>
  .md-typeset h1,
  .md-content__button {
    display: none;
  }
</style>

<div class="header-logo" markdown="1"> 

![Logo](images/logo_light.png#only-light){ width="36" }
![Logo](images/logo.png#only-dark){ width="36" }
    <span style="font-size: 36pt; font-weight: bold; padding-left: 6pt;">ScalaMetrics</span> 
</div>

> The most comprehensive code metrics and analysis library for Scala

<div markdown class="badges">

 [![Release](https://img.shields.io/github/v/release/bitblends/scalametrics?sort=semver&style=flat&color=darkgreen&labelColor=2f363d&logo=github&logoColor=white)](https://github.com/bitblends/scalametrics/releases/latest){:target="_blank"}
 [![Maven Central](https://img.shields.io/maven-central/v/com.bitblends/scalametrics_2.13?style=flat&color=darkgreen&labelColor=2f363d&logo=Sonatype&logoColor=white)](https://central.sonatype.com/artifact/com.bitblends/scalametrics_2.13){:target="_blank",style="margin-right: 10px;"}
 [![CI](https://img.shields.io/github/actions/workflow/status/bitblends/scalametrics/ci.yml?branch=main&style=flat&color=green&labelColor=2f363d)](https://github.com/bitblends/scalametrics/actions/workflows/ci.yml?query=branch%3Amain){:target="_blank"}
 [![Scala versions](https://img.shields.io/badge/Scala-2.12%20%7C%202.13%20%7C%203-ff4757?style=flat&color=red&labelColor=2f363d&logo=scala&logoColor=white)](https://www.scala-lang.org){:target="_blank"}
 [![License](https://img.shields.io/badge/License-MIT-3?style=flat&color=yellow&labelColor=2f363d&logoColor=white)](LICENSE){:target="_blank"}

</div>

<p>
ScalaMetrics is a powerful static analysis library for Scala projects. It provides comprehensive analysis at multiple granularity levels: project, package, file, method, and member.
</p>

## Features

- **Multi-Level Analysis**: Extract metrics at project, package, file, method, and member levels
- **Comprehensive Metrics**:
    - Cyclomatic complexity
    - Nesting depth
    - Expression branch density
    - Pattern matching
    - Lines of code
    - Documentation coverage
    - Parameter and arity (implicit, using, default, varargs)
    - Return type explicitness
    - Inline and implicit usage
- **Raw and Aggregated Metrics**: Provides both raw and aggregated metrics for detailed insights
- **Multiple Dialect Support**: Supports Scala 2.12, 2.13, and 3.3
- **Immutable Design**: Functional pipeline architecture with immutable data flow
- **ScalaMeta-Powered**: Leverages ScalaMeta for accurate AST parsing and traversal
- **Automatic Dialect Detection**: Automatically detects Scala dialects for accurate parsing using a combination of
   heuristics and statistical methods

Please refer to the [Getting Started][getting-started] guide for installation instructions and quick start examples.
You can also explore the [Metrics Overview][measuring-what-matters-a-gentle-introduction] to learn about the various metrics provided by 
ScalaMetrics and see how they can help improve your code quality.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

Licensed under the MIT License. See [LICENSE](LICENSE) file for details.

## Acknowledgments

Built with: [ScalaMeta](https://scalameta.org/) - Scala metaprogramming library
