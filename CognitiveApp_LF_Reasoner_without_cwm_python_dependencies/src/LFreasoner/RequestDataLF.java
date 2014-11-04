package LFreasoner;

public class RequestDataLF {

	private String patient;
	private String rule;
	private String uriRequest;
	
	public RequestDataLF (String patient, String rule, String uriRequest) {
		this.patient = patient;
		this.rule = rule;
		this.uriRequest = uriRequest;
	}
	
	public String getPatient() {
		return patient;
	}
	
	public String getRule() {
		return rule;
	}
	
	public String getRequestURI() {
		return uriRequest;
	}
	
}
