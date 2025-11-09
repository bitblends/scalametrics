## Development

### Prerequisites

- JDK 17
- SBT 1.11.7+
- Scala 2.12.20, 2.13.17, 3.3.6

### Building

```bash
# Compile (default Scala version 2.13.17)
sbt compile

# Cross-compile for all supported versions
sbt +compile

# Run tests
sbt test

# Run tests for specific Scala version
sbt ++2.12.20 test
sbt ++2.13.17 test
sbt ++3.3.6 test
```

### Project Structure

```
scalametrics/
├── src/
│   ├── main/
│   │   ├── scala/              # Common source code
│   │   ├── scala-2.12/         # Scala 2.12-specific code
│   │   ├── scala-2.13+/        # Scala 2.13-specific code
│   │   └── scala-3/            # Scala 3-specific code
│   └── test/
│       ├── scala/              # Test sources
│       ├── scala-2.12/         # Scala 2.12-specific tests
│       ├── scala-2.13+/        # Scala 2.13-specific tests
│       └── scala-3/            # Scala 3-specific tests
└── build.sbt                   # Build configuration
```

## Key Design Patterns

1. **Immutable Data Flow**: All analyzers return new `AnalysisCtx` instances
2. **Trait-Based Composition**: Analyzers are traits that can be mixed and matched
3. **ScalaMeta AST Traversal**: Type-safe AST pattern matching
4. **Cross-Version Support**: SBT cross-compilation with version-specific sources


## Architecture

### Analysis Pipeline

ScalaMetrics uses a trait-based analyzer pattern with immutable context passing:

```
AnalysisCtx → FileAnalyzer → MethodAnalyzer → MemberAnalyzer → Results
```

Each analyzer:
1. Takes an immutable `AnalysisCtx`
2. Performs its analysis
3. Returns a new `AnalysisCtx` with accumulated metrics

### Core Components

#### Analyzers

- **FileAnalyzer**: Analyzes file-level metrics (LOC, symbols, documentation)
- **MethodAnalyzer**: Analyzes methods and functions (complexity, parameters, return types)
- **MemberAnalyzer**: Analyzes class/trait members (fields, properties)
- **TypeInfer**: Handles type inference and resolution

#### Metric Calculators

Located in the `metrics` package:

- `Cyclomatic.scala`: McCabe cyclomatic complexity
- `NestingDepth.scala`: Maximum nesting depth
- `ExpressionBranchDensity.scala`: Branch density metrics
- `PatternMatching.scala`: Pattern matching analysis
- `InlineAndImplicits.scala`: Inline/implicit/given tracking
- `Parameter.scala`: Parameter analysis
- `ReturnTypeExplicitness.scala`: Return type explicitness

#### Statistics Aggregation

The `Stats` object creates the case classes containing the aggregate metrics at multiple levels::
- File-level statistics
- Package-level roll-ups
- Project-wide aggregations
- Derived metrics (documentation coverage percentages, etc.)

