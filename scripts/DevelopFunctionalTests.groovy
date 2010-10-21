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

includeTargets << grailsScript("_GrailsEvents")

target('default': "Run a Grails applications unit tests") {

	println ""
	
	def runAppArgs = args.tokenize()
	def run = true

	// Set in launchApp()
	baseUrl = null
	appOutput = null
	
	def app = launchApp(*runAppArgs)
	
	addShutdownHook {
		if (isRunning(app)) {
			println ""
			update "stopping app"
			app.destroy()
			exhaust(appOutput)
			app.waitFor()
		}
	}
	
	def input = new BufferedReader(new InputStreamReader(System.in))
	def last = ""
	
	while (run) {
		println ""
		println "-- Ready to run tests --"
		println "   * Enter a test target pattern to run tests (e.g. functional: Login)"
		if (last) {
			println "   * Enter a blank line to rerun the previous tests (pattern: '$last')"
		} else {
			println "   * Enter a blank line to run all functional tests"
		}
		println "   * Enter 'restart' to restart the running application"
		println "   * Enter 'exit' to stop"
		println ""
		print "Command: "

		def line = input.readLine().trim()
		
		println ""
				
		if (isExit(line)) {
			run = false
			update "stopping app"
			app.destroy()
			exhaust(appOutput)
			app.waitFor()
		} else if (isRestart(line)) {
			update "restarting app"
			app.destroy()
			exhaust(appOutput)
			app.waitFor()
			app = launchApp(*runAppArgs)
		} else { // is test command
			if (line == "") {
				if (last == "") {
					last = "functional:"
				}
				line = last
			} else {
				last = line
			}
			
			def baseUrlArg = "-baseUrl=$baseUrl" as String
			def tests = runTests(baseUrlArg, *(line.tokenize() as String[]))
			def testsOutput = getOutputReader(tests)
			exhaust(testsOutput)
			
			if (!isRunning(app)) {
				update "the app crashed, restarting it"
				app = launchApp(*runAppArgs)
			}
		}
	}
}

launchApp = { String[] args ->
	def command = ["test", "run-app"] + args.toList()
	update "Launching application with '${command.join(' ')}'"
	def process = createGrailsProcess(command as String[])
	
	appOutput = getOutputReader(process)
	
	def starting = true
	while (starting) {
		def line = appOutput.readLine()
		if (line == null) {
			die "run-app process has closed output"
		}
		println "  > $line"
		
		baseUrl = extractBaseUrlFromStartedMessage(line)
		if (baseUrl) {
			starting = false
		}
	}
	
	process
}

runTests = { String[] args -> 
	def command = ["test-app"] + args.toList()
	update "Launching tests with '${command.join(' ')}'"
	createGrailsProcess(command as String[])
}

createGrailsProcess = { String[] args, err2out = true ->
	createGrailsProcessBuilder(*args).redirectErrorStream(err2out).start()
}

createGrailsProcessBuilder = { String[] args ->
	new ProcessBuilder(getGrailsStarterPath(), *args).directory(grailsSettings.baseDir)
}

getGrailsStarterPath = {
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
	event("StatusFinal", [it])
	System.exit(1)
}

update = {
	event("StatusUpdate", [it])
}

isRunning = { Process process ->
	try {
		process.exitValue()
		false
	} catch (IllegalThreadStateException e) {
		true
	}
}

extractBaseUrlFromStartedMessage = {
	def matcher = it =~ ~/Server running. Browse to (\S+).*/
	if (matcher) {
		def base = matcher[0][1]
		base.endsWith("/") ? base : base + "/"
	} else {
		null
	}
}

isExit = {
	it.toLowerCase() == "exit"
}

isRestart = {
	it.toLowerCase() == "restart"
}

getOutputReader = { Process process ->
	new BufferedReader(new InputStreamReader(process.in))
}

exhaust = { Reader reader, String prefix = "  > " ->
	try {
		def line = reader.readLine()
		while (line != null) {
			println "${prefix}${line}"
			line = reader.readLine()
		}
	} catch (IOException e) {
		// ignore
	}
}