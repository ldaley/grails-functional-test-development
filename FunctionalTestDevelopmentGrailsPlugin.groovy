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
class FunctionalTestDevelopmentGrailsPlugin {
	def version = "0.9.3"
	def grailsVersion = "* > 2.0.0"
	def dependsOn = [:]
	def pluginExcludes = [
		"grails-app/**/*",
		"scripts/_Events.groovy"
	]

	def author = "Luke Daley"
	def authorEmail = "ld@ldaley.com"
	def title = "Functional Test Development"
	def description = "Provides a script to make developing functional tests quicker"
	def documentation = "http://grails.org/plugin/functional-test-development"
}
