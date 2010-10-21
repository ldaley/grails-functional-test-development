class TheController {

	def theService
	
	def a1 = { render(contentType: "text/plain", text: "1") }
	def a2 = { render(contentType: "text/plain", text: "2") }
	def serviceValue = { render(contentType: "text/plain", text: theService.value) }
}