package com.amazonaws.schemamanager.repo.datatypes;

import com.amazonaws.schemamanager.properties.AppConfigHelper;

public class DefaultSchemaMetadata extends RepoSchemaMetadata {
	
	public DefaultSchemaMetadata() {
	}
	
	public DefaultSchemaMetadata(DefaultSchemaMetadata parent) {
		this.parent = parent;
	}
	
	private DefaultSchemaMetadata parent;

	public DefaultSchemaMetadata getParent() {
		return parent;
	}

	public void setParent(DefaultSchemaMetadata parent) {
		this.parent = parent;
	}
	
	@Override
	public String getDefaultCompatibility() {
		String comp = super.getDefaultCompatibility();
		if (comp != null) return comp;
		
		if (parent == null) {
			return AppConfigHelper.getDefaultCompatibility().toString();
		}
		
		return parent.getDefaultCompatibility();
	}
}
