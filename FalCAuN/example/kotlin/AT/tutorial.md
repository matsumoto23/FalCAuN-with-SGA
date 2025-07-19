# Tutorial for Automatic Transmission benchmark
Here, we provide a introduction of Automatic Transmission benchmark using specification-guided abstraction.

We take stl1.kts as an example.

First, it imports FalCAuN and another script AutoTrans.kt, which is a script for initiallizing the AT model and importing parameters of them.

Second, define a numerical mapper.
Since Mealy machines receive finite events, real-valued signals are abstracted into a finite alphabet. 

- The input mapper maps discrete input events into a real-valued input signal.
- The output mapper maps a real-valued output signal into discrete observations.
- The signal mapper creates "pseudo output signals" from the input and output signals to handle complicated properties.

The approximated Mealy machine uses these alphabet-abstracted signals. For instance, in the following example, an input signal consists of two dimensions (throttle and brake), and we create input events for each pair of the following values: (0.0, 0.0), (100.0, 0.0), (0.0, 325.0), and (100.0, 325.0).
An output signal consists of three dimensions (velocity, acceleration, and gear value).
The output mapper ignores the acceleration and gear value and assigns a discrete observation for each of the following range of the velocity value: $(-\infty, 110.0]$, $(110.0, 115.0]$, $(115.0, 120.0]$, $(120.0, \infty)$.

```kotlin
// Define the input and output mappers
val throttleValues = listOf(0.0, 100.0)
val brakeValues = listOf(0.0, 325.0)
val inputMapper = InputMapperReader.make(listOf(throttleValues, brakeValues))
val velocityValues = listOf(110.0, 115.0, 120.0, null)
val accelerationValues = listOf(null)
val gearValues = listOf(null)
val outputMapperReader = OutputMapperReader(listOf(velocityValues, accelerationValues, gearValues))
outputMapperReader.parse()
val signalMapper = ExtendedSignalMapper()
```

Third, give STL formulas. The syntax of STL in FalCAuN is as follows.
```
expr : atomic
     | expr && expr
     | expr || expr
     | expr -> expr
     | ! expr
     | GLOBALLY expr
     | GLOBALLY _ INTERVAL expr
     | EVENTUALLY expr
     | EVENTUALLY _ INTERVAL expr
     | X expr
     | expr U expr
     | expr U _ INTERVAL expr
     | ( expr )

atomic : signal(NATURAL) == value
       | signal(NATURAL) < value
       | signal(NATURAL) > value
       | signal(NATURAL) != value
       | input(NATURAL) == value
       | input(NATURAL) < value
       | input(NATURAL) > value
       | input(NATURAL) != value
       | output(NATURAL) == value
       | output(NATURAL) < value
       | output(NATURAL) > value
       | output(NATURAL) != value

value : -? NATURAL | -? FLOAT

GLOBALLY : '[]' | 'alw' | 'G'

EVENTUALLY : '<>' | 'ev' | 'F'

INTERVAL : [ NATURAL , NATURAL ]
```
In FalCAuN, STL formulas are given as strings.
```kotlin
// Define the STL properties
val stlFactory = STLFactory()
val stlList = listOf(
    "[] (signal(0) < 110)",
    "[] (signal(0) < 115)",
    "[] (signal(0) < 120)"
).stream().map { stlString ->
    stlFactory.parse(
        stlString,
        inputMapper,
        outputMapperReader.outputMapper,
        outputMapperReader.largest
    )
}.toList()
val signalLength = 30
val properties = AdaptiveSTLList(stlList, signalLength)
```
`STLFactory` is used for parsing them.
`signal(0)` points to the first output signal, the velocity values in this case.

Fourth, select a method of abstraction and create a mapper based on abstraction.
`NumericSULMapper` is used to applying the numerical mappers defined above to the AT model,
and`OutputEquivalence` is used to applying specification-guided abstraction in addition to the numerical mappers.
Here, a method of abstraction is selected based on a command line argument `args[0]`.
`original`, `abstract` `partial` is corresponding to the methods NOABS, COARSEST, DISJSENSE in the paper, respectively.

```kotlin
// Select a method of abstraction
var mapper : NumericSULMapper? = null
if (args[0] == "original"){  // NOABS
    mapper =
        NumericSULMapper(inputMapper, outputMapperReader.largest, outputMapperReader.outputMapper, signalMapper)
} else if (args[0] == "abstract"){  // COARSEST
    mapper = OutputEquivalence(inputMapper, outputMapperReader.largest, outputMapperReader.outputMapper, signalMapper, stlList, false)
    properties.setMapper(mapper as OutputEquivalence)
} else if (args[0] == "partial") {  // DISJSENSE
    mapper = OutputEquivalence(inputMapper, outputMapperReader.largest, outputMapperReader.outputMapper, signalMapper, stlList, true)
    properties.setMapper(mapper as OutputEquivalence)
}
```


Then, define parameters of an equivalence oracle.
In this example, we use an equivalence oracle based on a genetic algorithm.
So, we define parametes of an genetic algorithm here.
```kotlin
// Constants for the GA-based equivalence testing
val maxTest = 50000
val populationSize = 200
val crossoverProb = 0.5
val mutationProb = 0.01
```

Then, define a verifier using the above.
A verifier needs `MealyEquivalenceOracle` to check if the given system and the learned Mealy machine behave equivalently.
In this example, the verifier below has two equivalence oracles: `CornerCaseEQOracle` and `GAEQOracle`:
`CornerCaseEQOracle` is an equivalence oracle that tests the equivalence for the corner case inputs, such as keeping the maximum throttle;
`GAEQOracle` is an equivalence oracle based on a genetic algorithm.
`addGAEQOracleAll` adds this oracle for each STL property.

```kotlin
// Load the automatic transmission model. This automatically closes MATLAB
SimulinkSUL(initScript, paramNames, signalStep, simulinkSimulationStep).use { autoTransSUL ->
    // Configure and run the verifier
    val verifier = NumericSULVerifier(autoTransSUL, signalStep, properties, mapper)
    // Timeout must be set before adding equivalence testing
    verifier.setTimeout(20 * 60) // 20 minutes
    verifier.addCornerCaseEQOracle(signalLength, signalLength / 2);
    verifier.addGAEQOracleAll(
        signalLength,
        maxTest,
        ArgParser.GASelectionKind.Tournament,
        populationSize,
        crossoverProb,
        mutationProb
    )
    val result = verifier.run()

    // Print the result
    if (result) {
        println("The property is likely satisfied")
    } else {
        for (i in 0 until verifier.cexProperty.size) {
            println("${verifier.cexProperty[i]} is falsified by the following counterexample")
            println("cex concrete input: ${verifier.cexConcreteInput[i]}")
            println("cex abstract input: ${verifier.cexAbstractInput[i]}")
            println("cex output: ${verifier.cexOutput[i]}")
        }
    }
    println("Execution time for simulation: ${verifier.simulationTimeSecond} [sec]")
    println("Number of simulations: ${verifier.simulinkCount}")
    println("Number of simulations for equivalence testing: ${verifier.simulinkCountForEqTest}")
}
```

## Notes
Although `signalMapper` is not used in this example, you can use them by replacing some definitions like:
```kotlin
val outputMapperReader = OutputMapperReader(listOf(ignoreValues, accelerationValues, gearValues, velocityValues))
outputMapperReader.parse()
val mapperString = listOf("previous_max_output(0)").joinToString("\n")
val signalMapper: ExtendedSignalMapper = ExtendedSignalMapper.parse(BufferedReader(StringReader(mapperString)))
```
By specifying 'previous_max_output(0)' in the definition of signal mapper, it creates a pseudo signal of the maximum value of 'output(0)' (velocity in this case) since the latest sampling point.
In this case, the output mapper ignores the velocity, acceleration, and gear value and assigns a discrete observation for each of the following range of the pseudo signal: $(-\infty, 110.0]$, $(110.0, 115.0]$, $(115.0, 120.0]$, $(120.0, \infty)$.

In this case, since the velocity values are ignored by the output mapper, the difinition of STL properties must be like the below. 
```kotlin
// Define the STL properties
val stlFactory = STLFactory()
val stlList = listOf(
    "[] (signal(3) < 110)",
    "[] (signal(3) < 115)",
    "[] (signal(3) < 120)"
).stream().map { stlString ->
    stlFactory.parse(
        stlString,
        inputMapper,
        outputMapperReader.outputMapper,
        outputMapperReader.largest
    )
}.toList()
val signalLength = 30
val properties = AdaptiveSTLList(stlList, signalLength)
```
