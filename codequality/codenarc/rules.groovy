ruleset {

    def extendedTestName = ".*/(Test.*|.*(Test|Tests|TestCase))\\.groovy"

    //you can use below to list the classes that don't meet the quality standards yet
    //and work to improve the codebase incrementally
    def bigClasses = 'SomeGnarlyGroovyClass.groovy,SomeFatLegacyGroovyClass.groovy'
    def bigMethods = 'SomeLegacyClassWithBigMethods.groovy,EvenWorseClassWithBigMethods.groovy'
    def complicatedMethods = 'IHateThisCode.groovy,PleaseRefactorMe.groovy'

    description ''' codenarc rules by pygradle'''

    // http://codenarc.sourceforge.net/codenarc-rules-basic.html
    // rulesets/basic.xml
    AssertWithinFinallyBlock
    AssignmentInConditional
    BigDecimalInstantiation
    BitwiseOperatorInConditional
    BooleanGetBoolean
    BrokenNullCheck
    BrokenOddnessCheck
    ClassForName
    ComparisonOfTwoConstants
    ComparisonWithSelf
    ConstantAssertExpression
    ConstantIfExpression
    ConstantTernaryExpression
    DeadCode
    DoubleNegative
    DuplicateCaseStatement
    DuplicateMapKey
    DuplicateSetValue
    EmptyCatchBlock
    EmptyClass
    EmptyElseBlock
    EmptyFinallyBlock
    EmptyForStatement
    EmptyIfStatement
    EmptyInstanceInitializer
    // EmptyMethod -- there are legit cases for empty methods (interface implementations)
    EmptyStaticInitializer
    EmptySwitchStatement
    EmptySynchronizedStatement
    EmptyTryBlock
    EmptyWhileStatement
    EqualsAndHashCode
    EqualsOverloaded
    ExplicitGarbageCollection
    ForLoopShouldBeWhileLoop
    IntegerGetInteger
    MultipleUnaryOperators
    RandomDoubleCoercedToZero
    RemoveAllOnSelf
    ReturnFromFinallyBlock
    ThrowExceptionFromFinallyBlock

    // rulesets/braces.xml
    ElseBlockBraces {
        bracesRequiredForElseIf = false //-- disabled due to codenarc bug that yielded false positives
    }
    ForStatementBraces
    IfStatementBraces
    WhileStatementBraces

    // rulesets/concurrency.xml
    BusyWait
    DoubleCheckedLocking
    InconsistentPropertyLocking
    InconsistentPropertySynchronization
    NestedSynchronization
    StaticCalendarField
    StaticConnection
    StaticDateFormatField
    StaticMatcherField
    StaticSimpleDateFormatField
    SynchronizedMethod
    SynchronizedOnBoxedPrimitive
    SynchronizedOnGetClass
    SynchronizedOnReentrantLock
    SynchronizedOnString
    SynchronizedOnThis
    SynchronizedReadObjectMethod
    SystemRunFinalizersOnExit
    ThisReferenceEscapesConstructor
    ThreadGroup
    ThreadLocalNotStaticFinal
    ThreadYield
    UseOfNotifyMethod
    VolatileArrayField
    VolatileLongOrDoubleField
    WaitOutsideOfWhileLoop

    // rulesets/convention.xml
    ConfusingTernary
    // CouldBeElvis -- there are legit scenarios where avoiding elvis results in cleaner code
    HashtableIsObsolete
    IfStatementCouldBeTernary
    // InvertedIfElse -- it feels too rigid, sometimes we want the negative case to be first for clarity
    LongLiteralWithLowerCaseL
    // NoDef  -- Disabled because it can be useful in small methods
    ParameterReassignment
    TernaryCouldBeElvis
    VectorIsObsolete

    // rulesets/design.xml
    AbstractClassWithPublicConstructor
    AbstractClassWithoutAbstractMethod
    BooleanMethodReturnsNull
    // BuilderMethodWithSideEffects -- too rigid, there are legit use cases, high noise - low value
    CloneableWithoutClone
    CloseWithoutCloseable
    CompareToWithoutComparable
    ConstantsOnlyInterface
    EmptyMethodInAbstractClass
    FinalClassWithProtectedMember
    ImplementationAsType
    // Instanceof   // -- we don't find it reasonable to ban instanceof keyword
    LocaleSetDefault
    // NestedForLoop -- sometimes nested for loop is cleaner than extracting a new method
    PrivateFieldCouldBeFinal {
        doNotApplyToFilesMatching = extendedTestName
    }
    // PublicInstanceField
    ReturnsNullInsteadOfEmptyArray
    ReturnsNullInsteadOfEmptyCollection
    SimpleDateFormatMissingLocale
    StatelessSingleton
    ToStringReturnsNull

    // rulesets/dry.xml
    /*
    Disabled because sometimes literals lead to cleaner code. Classes full of low-value constants are hard to read and comprehend.

    DuplicateListLiteral
    DuplicateMapLiteral
    DuplicateNumberLiteral
    DuplicateStringLiteral
    */

    // rulesets/enhanced.xml
    // These rules cause issues with the output and don't provide values
    // CloneWithoutCloneable
    //JUnitAssertEqualsConstantActualValue
    // UnsafeImplementationAsMap

    // rulesets/exceptions.xml
    CatchArrayIndexOutOfBoundsException
    CatchError
    // CatchException -- sometimes it is desired to catch Exception types
    CatchIllegalMonitorStateException
    CatchIndexOutOfBoundsException
    CatchNullPointerException
    CatchRuntimeException
    // CatchThrowable -- we need to catch Throwable at times
    ConfusingClassNamedException
    ExceptionExtendsError
    ExceptionExtendsThrowable
    ExceptionNotThrown
    MissingNewInThrowStatement
    // ReturnNullFromCatchBlock -- there are legit use cases for this pattern
    SwallowThreadDeath
    ThrowError
    ThrowException
    ThrowNullPointerException
    // ThrowRuntimeException -- there are legit use cases
    ThrowThrowable

    // rulesets/formatting.xml
    // BlankLineBeforePackage -- not useful
    BracesForClass
    BracesForForLoop
    BracesForIfElse
    BracesForMethod
    BracesForTryCatchFinally
    // ClassJavadoc -- Some classes may not need javadoc
    ClosureStatementOnOpeningLineOfMultipleLineClosure
    // ConsecutiveBlankLines  // -- high noise, low signal
    // FileEndsWithoutNewline -- not useful
    LineLength {
        length = 300
        doNotApplyToFilesMatching = extendedTestName
        //sometimes we validate/assert multiline Strings that have long lines
    }
    MissingBlankLineAfterImports
    MissingBlankLineAfterPackage
    SpaceAfterComma

    SpaceAfterFor
    SpaceAfterIf
    SpaceAfterOpeningBrace {
        ignoreEmptyBlock = true
    }
    SpaceAfterSemicolon
    SpaceAfterSwitch
    SpaceAfterWhile
    SpaceAroundClosureArrow
    //SpaceAroundMapEntryColon  //-- This rule does not seem to work correctly and returns false positives
    SpaceAroundOperator
    SpaceBeforeClosingBrace {
        ignoreEmptyBlock = true
    }
    SpaceBeforeOpeningBrace
    TrailingWhitespace

    // rulesets/groovyism.xml
    AssignCollectionSort
    AssignCollectionUnique
    ClosureAsLastMethodParameter
    CollectAllIsDeprecated
    ConfusingMultipleReturns
    ExplicitArrayListInstantiation
    ExplicitCallToAndMethod
    // ExplicitCallToCompareToMethod -- there are legit cases
    // ExplicitCallToDivMethod -- there are legit cases, there are div() methods outside of gdk
    ExplicitCallToEqualsMethod
    ExplicitCallToGetAtMethod
    ExplicitCallToLeftShiftMethod
    ExplicitCallToMinusMethod
    ExplicitCallToModMethod
    ExplicitCallToMultiplyMethod
    ExplicitCallToOrMethod
    ExplicitCallToPlusMethod
    ExplicitCallToPowerMethod
    ExplicitCallToRightShiftMethod
    ExplicitCallToXorMethod
    ExplicitHashMapInstantiation
    // ExplicitHashSetInstantiation -- as Set looks awkward in code
    ExplicitLinkedHashMapInstantiation
    // ExplicitLinkedListInstantiation -- as LinkedList looks awkward in code
    ExplicitStackInstantiation
    ExplicitTreeSetInstantiation
    GStringAsMapKey
    GStringExpressionWithinString
    // GetterMethodCouldBeProperty -- using the getter may be preferable in some cases
    GroovyLangImmutable
    UseCollectMany
    UseCollectNested

    // rulesets/imports.xml
    DuplicateImport
    ImportFromSamePackage
    ImportFromSunPackages
    // MisorderedStaticImports -- high noise, low signal
    // NoWildcardImports  -- we find wildcard imports useful
    UnnecessaryGroovyImport
    UnusedImport

    // rulesets/junit.xml
    ChainedTest
    CoupledTestCase
    JUnitAssertAlwaysFails
    JUnitAssertAlwaysSucceeds
    JUnitFailWithoutMessage
    JUnitLostTest
    JUnitPublicField
    // JUnitPublicNonTestMethod -- not compatible with spock, which is a standard x-unit framework for groovy
    // JUnitPublicProperty //-- not compatible with spock/clean groovy tests where we want to avoid using unnecessary keywords like 'private' for test's clarity
    JUnitSetUpCallsSuper
    JUnitStyleAssertions
    JUnitTearDownCallsSuper
    JUnitTestMethodWithoutAssert
    JUnitUnnecessarySetUp
    JUnitUnnecessaryTearDown
    JUnitUnnecessaryThrowsException
    SpockIgnoreRestUsed
    UnnecessaryFail
    UseAssertEqualsInsteadOfAssertTrue
    UseAssertFalseInsteadOfNegation
    UseAssertNullInsteadOfAssertEquals
    UseAssertSameInsteadOfAssertTrue
    UseAssertTrueInsteadOfAssertEquals
    UseAssertTrueInsteadOfNegation

    // rulesets/naming.xml
    /* unless otherwise noted, they were enable/disabled by choice */
    AbstractClassName
    ClassName
    ClassNameSameAsFilename
    // ConfusingMethodName -- for groovy dsl we sometimes have legit cases
    // FactoryMethodName -- too rigid and low value; sometimes 3rd party APIs violate this rule and there is not much we can do.
    ClassNameSameAsSuperclass
    FieldName {
        regex = /[a-z][a-zA-Z0-9_]*/ //examples: fooBar, foo, fooBar23, fooBar2_3
    }
    InterfaceName
    InterfaceNameSameAsSuperInterface
    MethodName {
        doNotApplyToFilesMatching = extendedTestName
    }
    ObjectOverrideMisspelledMethodName
    PackageName
    PackageNameMatchesFilePath // -- Disabled due to codenarc version TODO remove useless comments
    ParameterName
    PropertyName
    VariableName {
        regex = /[a-z][a-zA-Z0-9]*/
        finalRegex = regex
    }

    // rulesets/size.xml
    AbcMetric {  // Requires the GMetrics jar
        maxMethodAbcScore = 150 //pushed higher, clean methods often land between 50-100 for Gradle plugin classes
        maxClassAverageMethodAbcScore = 150
        // pushed higher, clean classes often land between 50-100 for Gradle plugin classes
        doNotApplyToFileNames = complicatedMethods
    }
    ClassSize {
        maxLines = 350
        doNotApplyToFilesMatching = extendedTestName
        doNotApplyToFileNames = bigClasses
    }
    // CrapMetric   // Need to write cobertura file
    CyclomaticComplexity // Requires the GMetrics jar
    MethodCount
    MethodSize {
        maxLines = 150
        doNotApplyToFileNames = bigMethods
    }
    NestedBlockDepth {
        maxNestedBlockDepth = 6
    }
    ParameterCount {
        maxParameters = 6
    }

    // rulesets/unnecessary.xml
    AddEmptyString
    ConsecutiveLiteralAppends
    // ConsecutiveStringConcatenation -- ignore, there are legit use cases for clean code
    UnnecessaryBigDecimalInstantiation
    UnnecessaryBigIntegerInstantiation
    UnnecessaryBooleanExpression {
        doNotApplyToFilesMatching = extendedTestName //not compatible with spock
    }
    UnnecessaryBooleanInstantiation
    UnnecessaryCallForLastElement
    UnnecessaryCallToSubstring
    UnnecessaryCast
    UnnecessaryCatchBlock
    // UnnecessaryCollectCall -- sometimes collect() is cleaner
    UnnecessaryCollectionCall
    UnnecessaryConstructor
    UnnecessaryDefInFieldDeclaration
    UnnecessaryDefInMethodDeclaration
    UnnecessaryDefInVariableDeclaration
    UnnecessaryDotClass
    UnnecessaryDoubleInstantiation
    // UnnecessaryElseStatement
    UnnecessaryFinalOnPrivateMethod
    UnnecessaryFloatInstantiation
    // UnnecessaryGString -- the code is cleaner and more readable if consistent quote is used
    // UnnecessaryGetter -- sometimes getter is desired / cleaner
    UnnecessaryIfStatement
    UnnecessaryInstanceOfCheck
    UnnecessaryInstantiationToGetClass
    UnnecessaryIntegerInstantiation
    UnnecessaryLongInstantiation
    UnnecessaryModOne
    // UnnecessaryNullCheck //more explicit is clearer
    UnnecessaryNullCheckBeforeInstanceOf
    // UnnecessaryObjectReferences -- high noise - low value
    UnnecessaryOverridingMethod
    UnnecessaryPackageReference
    UnnecessaryParenthesesForMethodCallWithClosure
    // UnnecessaryPublicModifier
    // UnnecessaryReturnKeyword  -- explicit return is clean
    UnnecessarySafeNavigationOperator
    UnnecessarySelfAssignment
    UnnecessarySemicolon
    UnnecessaryStringInstantiation
    // UnnecessarySubstring -- explicit substring() is cleaner in some cases and easier to understand by java developers
    UnnecessaryTernaryExpression
    // UnnecessaryToString -- explicit toString() yields cleaner code at times and makes operation explicit
    UnnecessaryTransientModifier

    // rulesets/unused.xml
    UnusedArray
    // UnusedMethodParameter -- sometimes we implement interfaces and not use the parameters
    UnusedObject {
        doNotApplyToFilesMatching = extendedTestName
    }
    UnusedPrivateField
    UnusedPrivateMethod
    UnusedPrivateMethodParameter
    UnusedVariable
}
