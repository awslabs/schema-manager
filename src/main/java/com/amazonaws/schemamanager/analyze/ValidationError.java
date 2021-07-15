package com.amazonaws.schemamanager.analyze;

public class ValidationError {
	
	private String errorMsg;
	
	public ValidationError() {}
	
	public ValidationError(String msg) {
		errorMsg = msg;
	}

	public ValidationError(String msgPattern, Object ... params) {
		this.errorMsg = String.format(msgPattern, params);
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidationError other = (ValidationError) obj;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValidationError [errorMsg=" + errorMsg + "]";
	}
	
	public static void main(String[] args) {
		System.out.println(new ValidationError("Pattern %s = %s", "key", "value"));
	}
}
