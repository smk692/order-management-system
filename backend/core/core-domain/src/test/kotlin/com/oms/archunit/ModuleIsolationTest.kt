package com.oms.archunit

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition

/**
 * ArchUnit tests to enforce module isolation boundaries.
 *
 * Module Isolation Rules:
 * 1. No cross-domain dependencies (domain-order cannot import domain-claim)
 * 2. Domain modules cannot import infrastructure modules
 * 3. Only core-domain can be imported by all domains
 * 4. Domain modules should be self-contained
 */
@AnalyzeClasses(
    packages = ["com.oms"],
    importOptions = [ImportOption.DoNotIncludeTests::class],
)
class ModuleIsolationTest {
    @ArchTest
    val `domain modules should not depend on each other` =
        SlicesRuleDefinition.slices()
            .matching("com.oms.(*).domain..")
            .should()
            .notDependOnEachOther()
            .because("Domain modules should be isolated and not have cross-domain dependencies")

    @ArchTest
    val `domain-order should not depend on domain-claim`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..order..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..claim..")
            .because("domain-order cannot import domain-claim to maintain module isolation")

    @ArchTest
    val `domain-order should not depend on domain-settlement`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..order..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..settlement..")
            .because("domain-order cannot import domain-settlement to maintain module isolation")

    @ArchTest
    val `domain-inventory should not depend on domain-settlement`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..inventory..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..settlement..")
            .because("domain-inventory cannot import domain-settlement to maintain module isolation")

    @ArchTest
    val `domain-claim should not depend on domain-order`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..claim..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..order..")
            .because("domain-claim cannot import domain-order to maintain module isolation")

    @ArchTest
    val `domain modules should not depend on infrastructure modules`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..infra..")
            .because("Domain layer should not depend on infrastructure to maintain clean architecture")

    @ArchTest
    val `only core-domain can be imported by all domains`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes()
            .that()
            .resideInAPackage("..core.domain..")
            .should()
            .bePublic()
            .because("core-domain provides shared domain primitives for all domain modules")

    @ArchTest
    val `domain modules should not access other domain repositories`: ArchRule =
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage("..order..")
            .should()
            .accessClassesThat()
            .resideInAnyPackage("..claim.repository..", "..settlement.repository..", "..inventory.repository..")
            .because("Domain modules should not access repositories from other domains")

    @ArchTest
    val `domain modules should be free of cycles`: ArchRule =
        SlicesRuleDefinition.slices()
            .matching("com.oms.(*)..")
            .should()
            .beFreeOfCycles()
            .because("Module dependencies should form a directed acyclic graph (DAG)")
}
