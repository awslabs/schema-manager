package com.amazonaws.schemamanager.analyze.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

public class CompatibilityError {
	private String subject;
	private RepoSchema repoSchema;
	private List<String> errors;
	
	public CompatibilityError() {
	}

	public CompatibilityError(String subject, RepoSchema repoSchema, List<String> errors) {
		this.subject = subject;
		this.repoSchema = repoSchema;
		this.errors = errors;
	}	
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public RepoSchema getRepoSchema() {
		return repoSchema;
	}
	public void setRepoSchema(RepoSchema repoSchema) {
		this.repoSchema = repoSchema;
	}
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	public CompatibilityError addErrors(Collection<String> newErrors) {
		if (errors == null) {
			errors =new LinkedList<>();
		}
		errors.addAll(newErrors);
		return this;
	}
	public CompatibilityError addErrors(String ... newErrors) {
		if (newErrors == null || newErrors.length == 0) return this;
		return addErrors(Arrays.asList(newErrors));
	}
}
