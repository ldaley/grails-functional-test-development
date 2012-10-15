/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.codehaus.groovy.grails.cli.CommandLineHelper

includeTargets << grailsScript("_GrailsEvents")

target('default': "Run a Grails applications unit tests") {

	println ""
	
	appOutputPrefix = "  [app]  "
	testOutputPrefix = " [test]  "
	
	def runAppArgs = args.tokenize()
	def run = true
	
	app = launchApp(*runAppArgs)
	
	addShutdownHook {
		if (isRunning(binding.app)) {
			println ""
			update "stopping app"
			stopApp()
		}
	}

	def last = ""
	
	def inputHelper = new CommandLineHelper()
	
	while (run) {
		println ""
		println "Ready to run tests."
		println " - Enter a test target pattern to run tests"
		if (last) {
			println " - Enter a blank line to rerun the previous tests (pattern: '$last')"
			println " - Enter 'all' to run all functional tests"
		} else {
			println " - Enter a blank line (or 'all') to run all functional tests"
		}
		println " - Enter 'restart' to restart the running application"
		println " - Enter 'exit' to stop"
		println ""

		def line = inputHelper.userInput("")?.trim()
		
		println ""
				
		if (isExit(line)) {
			run = false
			update "stopping app"
			stopApp(quiet: true)
		} else if (isRestart(line)) {
			update "restarting app"
			stopApp(quiet: true)
			app = launchApp(*runAppArgs)
		} else { // is test command
			if (line == "") {
				line = last
			} else if (line == "all") {
				line = ""
				last = ""
			} else {
				last = line
			}
			
			def args = []
			def properties = [:]
			
			buildLaunchArgs(parseCommandLine(line), args, properties, "D")
			
			def baseUrlArg = ""
			if (!args.any { it ==~ /--?baseUrl=.+/ }) {
				baseUrlArg = " -baseUrl=$baseUrl" as String
			}
			def tests = runTests(*:properties, "-non-interactive${baseUrlArg} functional: $line")
			def testsOutput = new BufferedReader(new InputStreamReader(tests.in))
			exhaust(testsOutput, testOutputPrefix)
			
			println ""
			update "Output from the application while running tests"
			try {
				exhaust(appOutput, appOutputPrefix, true)
			} catch (IOException e) {
				// ignore
			}
			
			if (!isRunning(app)) {
				update "the app crashed, restarting it"
				app = launchApp(*runAppArgs)
			}
		}
	}
}

buildLaunchArgs = { tokens, args, properties, propPrefix ->
	tokens.each {
		if (it.startsWith("-$propPrefix")) {
			def m = it =~ ~/-$propPrefix(.+)=(.+)/
			properties[m[0][1]] = m[0][2]
		} else {
			args << it
		}
	}
}

launchApp = { String[] givenArgs ->
	def args = []
	def properties = [:]
	
	buildLaunchArgs(givenArgs, args, properties, "P")
	
	def command = ["run-app"] + args.toList()
	if (!properties["grails.env"]) {
		command.add(0, "test")
	}
	command.add(0, "-reloading")
	properties["grails.reload.enabled"] = "true"
	
	def process = createGrailsProcess(properties, command.join(" "))
	
	def inputStream = new PipedInputStream()
	def outputStream = new PipedOutputStream(inputStream)
	appOutput = new BufferedReader(new InputStreamReader(inputStream))
	appOutputReadingThread = process.consumeProcessOutputStream(outputStream)
	
	try {
		baseUrl = exhaust(appOutput, appOutputPrefix, false) {
			def matcher = it =~ ~/Server running. Browse to (\S+).*/
			if (matcher) {
				def base = matcher[0][1]
				base.endsWith("/") ? base : base + "/"
			} else {
				null
			}
		}
	} catch (IOException e) {
		if (!(isRunning(process))) {
			die "the application did not start successfully"
		}
	}
		
	process
}

stopApp = { Map options = [:] ->
	appOutputReadingThread.defaultUncaughtExceptionHandler = ignoreIOExceptions
	app.destroy()
	try {
		exhaust(appOutput, appOutputPrefix)
	} catch (IOException e) {
		if (isRunning(app) && options.quiet != true) {
			throw e
		}
	}
	app.waitFor()
}

runTests = { Map properties, String line -> 
	createGrailsProcess(properties, "test-app $line")
}

createGrailsProcess = { Map properties, String args, err2out = true ->
	update "grails $args"
	createGrailsProcessBuilder(args).redirectErrorStream(err2out).start()
}

createGrailsProcessBuilder = { String args ->
	def builder = new ProcessBuilder()
	builder.directory(grailsSettings.baseDir)
	def env = builder.environment()
	def props = System.properties
	
	def javaOpts = []
	[env.GRAILS_OPTS, env.JAVA_OPTS]*.with {
		eachMatch(~/-.+?(?=\s+-|-s*$)/) {
			javaOpts << it.replace('"', '')
		}
	}

	if (javaOpts.empty) {
		javaOpts << "-Xmx512m" << "-XX:MaxPermSize=96m"
	}
	
	["grails.home", "grails.version", "base.dir", "tools.jar", "groovy.starter.conf"].each { 
		javaOpts << ("-D" + it + "=" + props[it])
	}
	
	def java = props["java.home"] + "/bin/java"
	def cmd = [java, *javaOpts, '-classpath', props['java.class.path'], 'org.codehaus.groovy.grails.cli.support.GrailsStarter', '--main', 'org.codehaus.groovy.grails.cli.GrailsScriptRunner', '--conf', props['groovy.starter.conf']]

	builder.command(*cmd, args)
	builder
}

getGrailsPath = {
	def grailsHome = grailsSettings.grailsHome
	
	if (!grailsHome) {
		die "GRAILS_HOME is not set in build settings, cannot continue"
	}
	if (!grailsHome.exists()) {
		die "GRAILS_HOME points to $grailsHome.path which does not exist, cannot continue"
	}
	
	def starterFile = new File(grailsHome, "bin/grails")
	if (!starterFile.exists()) {
		die "GRAILS_HOME points to $grailsHome.path which does not have a 'bin/grails' in it, cannot continue"
	}
	
	starterFile.absolutePath
}

die = {
	println ""
	event("StatusError", ["Error: $it"])
	System.exit(1)
}

update = {
	event("StatusUpdate", ["$it"])
}

isRunning = { Process process ->
	try {
		process.exitValue()
		false
	} catch (IllegalThreadStateException e) {
		true
	}
}

isExit = {
	it.toLowerCase() == "exit"
}

isRestart = {
	it.toLowerCase() == "restart"
}

exhaust = { Reader reader, String prefix, boolean noWait = false, Closure stopAt = null ->
	if (noWait && !reader.ready()) {
		return null
	}
	
	def line = reader.readLine()
	while (line != null) {
		println "${prefix}${line}"
		if (stopAt) {
			def stopped = stopAt(line)
			if (stopped) {
				return stopped
			}
		}
		
		if (noWait && !reader.ready()) {
			return null
		}
		
		line = reader.readLine()
	}
}

ignoreIOExceptions =  [uncaughtException: { Thread t, Throwable e -> 
	if (e instanceof IOException) {
		// ignore
	} else {
		throw e
	}
}] as Thread.UncaughtExceptionHandler

parseCommandLine = { line ->
	line = line.trim()
	def words = []
	while (line) {
		
		def field = ''
		def snippet
		
		def match
		def matches = { pattern -> match = line =~ pattern }
		
		while (true) {
			if (matches(~/^("(([^"\\]|\\.)*)").*/)) {
				line -= match[0][1]
				snippet = match[0][2].replace("\\", "")
			} else if (line.startsWith('"')) {
				throw new IllegalArgumentException("Unmatched double quote: ${line}")
			} else if (matches(~/^('([^']*)').*/)) {
				line -= match[0][1]
				snippet = match[0][2]
			} else if (line.startsWith("'")) {
				throw new IllegalArgumentException("Unmatched single quote: ${line}")
			} else if (matches(~/^(\\(.)?).*/)) {
				line -= match[0][1]
				snippet = match[0][2] ?: "\\"
			} else if (matches(~/^([^\s\\'"]+)/)) {
				line -= match[0][1]
				snippet = match[0][1]
			} else {
				line = line.trim()
				break
			}
			field += snippet
		}
		
		words << field
	}
	
	words
}