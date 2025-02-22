plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
//    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
}

group = "com.company"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("app.jar")
    }
}

tasks {
    shadowJar {
        // нужно для flyway, иначе затираются плагины и не проходят валидацию имена миграций
        mergeServiceFiles()
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.logback.classic)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation(libs.kafka.clients)

    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.jdk8)

    implementation(libs.r2dbc.pool)
//    implementation(libs.r2dbc.mysql)
    implementation(libs.r2dbc.postgresql)
    implementation(libs.flyway.core)
//    runtimeOnly(libs.flyway.mysql)
//    runtimeOnly(libs.mariadb.java.client)
    runtimeOnly(libs.flyway.postgres)
    runtimeOnly(libs.postgres)
//    jooqGenerator(libs.mariadb.java.client)
    jooqGenerator(libs.postgres)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

buildscript {
    dependencies {
//        classpath(libs.flyway.mysql)
//        classpath(libs.mariadb.java.client)
        classpath(libs.flyway.postgres)
        classpath(libs.postgres)
    }
}

flyway {
//    url = "jdbc:mariadb://localhost:3409/project_name"
    url = "jdbc:postgresql://localhost:5332/project_name"
    user = "project_name"
    password = "project_name"
    schemas = arrayOf("project_name")
    cleanDisabled = false
}

jooq {
    version.set(libs.versions.jooq.version)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
//                    driver = "org.mariadb.jdbc.Driver"
                    driver = "org.postgresql.Driver"
//                    url = "jdbc:mariadb://localhost:3409/project_name"
                    url = "jdbc:postgresql://localhost:5332/project_name"
                    user = "project_name"
                    password = "project_name"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
//                        name = "org.jooq.meta.mariadb.MariaDBDatabase"
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "project_name"
                        excludes = "flyway_schema_history"
                    }
                    generate.apply {
                        isPojos = true
                        isImmutablePojos = true
                        isPojosAsKotlinDataClasses = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullRecordAttributes = true
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.company.jooq"
                        directory = "build/generated/jooq/src"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}