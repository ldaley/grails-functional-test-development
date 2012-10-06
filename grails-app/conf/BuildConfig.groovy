grails.servlet.version = "2.5"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolution = {
    inherits("global")
    log "warn"
    checksums true
    repositories {
        inherits true
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        test "org.codehaus.geb:geb-junit4:0.7.2"
        test "org.seleniumhq.selenium:selenium-chrome-driver:2.25.0"
    }
    plugins {
        runtime ":hibernate:$grailsVersion"
        build ":tomcat:$grailsVersion"

        test ":geb:0.7.2", ":spock:0.6", ":remote-control:1.3", {
            export = false
        }

        build(":release:2.0.4") {
            export = false
            exclude "groovy"
        }
    }
}

grails.release.scm.enabled = false