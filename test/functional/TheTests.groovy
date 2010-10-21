import grails.util.BuildSettingsHolder
import grails.plugin.remotecontrol.RemoteControl

class TheTests extends GroovyTestCase {

	def remote = new RemoteControl()
	
	void testActions() {
		assert getText("the", "a1") == "1"
		assert getText("the", "a2") == "2"
	}
	
	void testRemoting() {
		remote.exec { theService.setValue(1) }
		assert getText("the", "serviceValue") == "1"
		
		remote.exec { theService.setValue(10) }
		assert getText("the", "serviceValue") == "10"

		remote.exec { theService.setValue(1) }
		assert getText("the", "serviceValue") == "1"
	}
	
	protected getText(controllerName, actionName, params = null) {
		getUrl(controllerName, actionName, params).text
	}
	
	protected getUrl(controllerName, actionName, params = null) {
		def url = baseUrl + controllerName + "/" + actionName
		if (params) {
			// we aren't encoding here, don't use things requiring encoding
			url += "?" + params.collect { k,v -> "$k=$v" }.join("&")
		}
		new URL(url)
	}
	
	protected getBaseUrl() {
		def base = BuildSettingsHolder.settings.functionalTestBaseUrl
		base.endsWith("/") ? base : base + "/"
	}
}