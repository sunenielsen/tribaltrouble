 import com.diffplug.gradle.spotless.SpotlessExtension
import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("net.ltgt.errorprone") version "5.1.0" apply false
    id("net.ltgt.nullaway") version "3.0.0" apply false
    id("com.smushytaco.lwjgl3") version "1.0.2" apply false
    id("com.diffplug.spotless") version "7.0.4" apply false
}

allprojects {
    group = "com.oddlabs.tribaltrouble"
    version = "2.1"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "net.ltgt.errorprone")
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        java {
            target("src/**/*.java")
            eclipse().configFile(rootProject.file("intellij-java-style.xml"))
            toggleOffOn()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    dependencies {
        implementation("org.jspecify:jspecify:1.0.0")
        "errorprone"("com.google.errorprone:error_prone_core:2.48.0")
        "errorprone"("com.uber.nullaway:nullaway:0.13.1")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_26
        targetCompatibility = JavaVersion.VERSION_26
        modularity.inferModulePath.set(true)
    }

    tasks.withType<JavaCompile>().configureEach {
        options.errorprone {
            option("NullAway:AnnotatedPackages", "com.oddlabs")

            disableAllChecks = false

            disable( "NullAway", "IntLongMath", "ImmutableEnumChecker",
                "NarrowingCompoundAssignment",
                "TimeUnitConversionChecker", "UnusedNestedClass", "SameNameButDifferent", "AssignmentExpression",
                "NullablePrimitive", "ObjectToString", "ByteBufferBackingArray",
                "InputStreamSlowMultibyteRead", "BadComparable", "CatchAndPrintStackTrace",
                "ModifyCollectionInEnhancedForLoop", "StringCaseLocaleUsage",
                "EqualsHashCode", "MissingSummary", "JavaUtilDate", "DoNotCallSuggester",
                "MutablePublicArray", "InconsistentCapitalization",
                "TypeParameterUnusedInFormals", "PatternMatchingInstanceof", "DefaultCharset", "EmptyCatch",
                "MissingOverride",
                "EnumOrdinal", "JdkObsolete", "UnnecessaryParentheses", "UnusedMethod", "UnusedVariable",
                "StatementSwitchToExpressionSwitch",
                "EffectivelyPrivate", "ArrayRecordComponent", "StringSplitter", "InterruptedInCatchBlock" )
        }
    }
}
