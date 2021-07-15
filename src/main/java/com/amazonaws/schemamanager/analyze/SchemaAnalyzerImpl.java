package com.amazonaws.schemamanager.analyze;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.schemamanager.analyze.types.CheckChangesRequest;
import com.amazonaws.schemamanager.analyze.types.CheckChangesResponse;
import com.amazonaws.schemamanager.analyze.types.CompatibilityError;
import com.amazonaws.schemamanager.analyze.types.SchemaPair;
import com.amazonaws.schemamanager.properties.AnalyzerConfig;
import com.amazonaws.schemamanager.registry.Subject;
import com.amazonaws.schemamanager.repo.RepoUtils;
import com.amazonaws.schemamanager.repo.datatypes.RepoSchema;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.ParsedSchema;

public class SchemaAnalyzerImpl implements ISchemaAnalyzer {
	
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaAnalyzerImpl.class);

	private AnalyzerConfig config;
	
	@Override
	public void init(AnalyzerConfig config) {
		this.config = config;
	}

	public boolean testCompatibility(ParsedSchema newSchema, List<? extends ParsedSchema> existingSchemas, CompatibilityLevel compatibility) {
		
		newSchema.isCompatible(compatibility, existingSchemas);
		return false;
	}

	public boolean testCompatibility(String newSchema, String existingSchema, String compatibilityMode) {
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ValidationError> testCompatibility(List<RepoSchema> newSchemas, List<RepoSchema> existingSchemas,
			CompatibilityLevel defaultCompatibility) {
		
		Map<String,RepoSchema> existingPathToSchema = new HashMap<>();
		existingSchemas.forEach(s -> {existingPathToSchema.put(s.getPath(), s);});
		
		
		final List<ValidationError> allErrors = new LinkedList<>();
		newSchemas.forEach(s ->{
			String path = s.getPath();
			RepoSchema currentRepoSchema = existingPathToSchema.get(path);
			if (currentRepoSchema != null) {
				try {
					List<ValidationError> errors = testCompatibilityLevels(s, currentRepoSchema);
					if (errors != null) {
						allErrors.addAll(errors);
					}
				}catch(Exception e) {
					log.error("TestCompatibility failed", e);
					allErrors.add(new ValidationError(toString()));
				}
				
			}
		});
		
		
		
		return allErrors;
	}
	
	/**
	 * Test compatibility for each subject defined per schema, according to subjects' compatibility level.
	 * @param newSchema
	 * @param currentSchema
	 * @return
	 */
	public List<ValidationError> testCompatibilityLevels(RepoSchema newSchema, RepoSchema currentSchema){
		
		Map<String, CompatibilityLevel> newSubjects = RepoUtils.schemaToSubjectNames(newSchema);
		Map<String, CompatibilityLevel> currentSubjects = RepoUtils.schemaToSubjectNames(currentSchema);
		
		List<ValidationError> errors = new LinkedList<>();
		newSubjects.forEach((subj, compLevel) ->{
			if (currentSubjects.containsKey(subj)) {
				
				List<String> compErrors = newSchema.getSchema().isCompatible(compLevel, Arrays.asList(currentSchema.getSchema()));
				if (compErrors != null && !compErrors.isEmpty()) {
					compErrors.forEach(e -> errors.add(new ValidationError(
							"Compatibility error. Path: %s, subject: %s, compatibility: %s. Error: %s", 
							newSchema.getPath(),
							subj,
							compLevel,
							e)));
				}
			}
		});
		
		return errors;

	}

	/**
	 * Validates new set of schemas.
	 * Each schema can be configured to be related to multiple subjects.
	 * The validation with check the following:
	 * - duplicates. if duplicates are allowed, only warning message will be printed in logs, else - all dups will be reported and validation will fail.
	 * - 
	 * @param newSchemas
	 * @return
	 */
	@Override
	public List<ValidationError> validateSchemas(List<RepoSchema> newSchemas) {
		
		if (newSchemas == null) return null;
		
		List<ValidationError> errors = new LinkedList<>();
		
		Map<String, List<String>> nameAndPath = new HashMap<>();
		final List<RepoSchema> validSchemas = new LinkedList<>();
		
		newSchemas.forEach(s -> {
			if (s.getSchema() == null) {
				errors.add(new ValidationError("Validation failed for schema file %s. Error: %s", s.getPath(), "Cannot parse the file."));
				return;
			}
			String path = s.getPath();
			
			
			String schemaName = s.getSchema().name();
			nameAndPath.computeIfAbsent(schemaName, n -> new LinkedList<String>()).add(path);
			try {
				s.getSchema().validate();
				validSchemas.add(s);
			}catch(Exception e) {
				errors.add(new ValidationError("Validation failed for schema file %s. Error: %s", s.getPath(), e.getMessage()));
			}
		});
		
		// if dups aren't allowed, add error, otherwise print warning to the logs only:
		nameAndPath.forEach((name, paths) ->{
			if (paths != null && paths.size() > 1){
				if (config.getAllowSchemaNameDuplicates()) {
					log.warn(String.format("Duplicated names found! Name: %s, files with this name: ", String.join(",", paths)));
				}else {
					errors.add(new ValidationError("Duplicated names found! Name: %s, files with this name: ", String.join(",", paths)));
				}
			}
		});
		
		// checking for schema name duplicates and subject name duplicates would usually give similar results, but may vary.
		// Not always subject name is the same as schema name, and there may be multiple subjects per schema.
		// another confusion may come from different projects' repositories
		Map<String, List<RepoSchema>> allSubjects = new HashMap<>();
		validSchemas.forEach(s -> {
			RepoUtils.schemaToSubjectMap(s, allSubjects);
		});
		allSubjects.forEach((subj, schemas) ->{
			if (schemas.size() > 1) {
				if (config.getAllowSchemaNameDuplicates()) {
					log.warn(String.format("Duplicated subjects found! Subject: %s, files with this subject: ", 
							String.join(",", schemas.stream().map(RepoSchema::getPath).collect(Collectors.toList())) ));
				}else {
					log.error(String.format("Duplicated names found! Name: %s, files with this name: ", 
							String.join(",", schemas.stream().map(RepoSchema::getPath).collect(Collectors.toList()))));
					errors.add(new ValidationError("Duplicated names found! Name: %s, files with this name: ", 
							String.join(",", schemas.stream().map(RepoSchema::getPath).collect(Collectors.toList()))));
				}

			}
		});
		
		return errors;
	}

	@Override
	public List<ValidationError> testCompatibility(List<RepoSchema> newSchemas, List<Subject> registrySchemas) {
		
		Map<String, List<RepoSchema>> repoSubjects = new HashMap<>();
		newSchemas.forEach(s -> {
			RepoUtils.schemaToSubjectMap(s, repoSubjects);
		});
		
		Map<String, List<Subject>> regSubjectsMap = new HashMap<>();
		registrySchemas.forEach(regSubj -> {
			regSubjectsMap.computeIfAbsent(regSubj.getName(), n -> new LinkedList<Subject>()).add(regSubj);
		});
		
		List<ValidationError> errors = new LinkedList<>();
		
		repoSubjects.forEach((subj, repoSchemas) ->{
			List<Subject> regSubjects = regSubjectsMap.get(subj);
			if (regSubjects == null || regSubjects.isEmpty()) {
				// new Subject scenario. no errors
				log.info("New subject in repo: " + subj);
			}else {
				repoSchemas.forEach(repoSchema -> {
					CompatibilityLevel compLevel = repoSchema.getMetadata().getCompatibilityLevel(subj);
					regSubjects.forEach(regSubject -> {
						List<String> compIssues = repoSchema.getSchema().isCompatible(compLevel, regSubject.getSchemas());
						if (compIssues != null && !compIssues.isEmpty()) {
							compIssues.forEach(issue -> errors.add(new ValidationError(
									"Compatibility error. Path: %s, subject: %s, compatibility: %s. Error: %s", 
									repoSchema.getPath(),
									subj,
									compLevel,
									issue
									)));
						}
					});
				});
			}
		});
		
		return errors;
	}

	@Override
	public CheckChangesResponse checkChanges(CheckChangesRequest request) {
		
		List<RepoSchema> repoSchemas = request.getRepoSchemas();
		List<Subject> subjects = request.getSubjects();
		
		// repo schemas by subject name
		final Map<String, List<RepoSchema>> repoSubjects = new LinkedHashMap<>();
		repoSchemas.forEach(s -> {
			RepoUtils.schemaToSubjectMap(s, repoSubjects);
		});

		Map<String, Subject> regSubjectsMap = new HashMap<>();
		subjects.forEach(regSubj -> {
			regSubjectsMap.put(regSubj.getName(), regSubj);
		});
		
		
		Map<String, RepoSchema>  newSubjectsInRepo = new LinkedHashMap<>();
		Map<String, SchemaPair> newVersionFound = new LinkedHashMap<>();
		List<SchemaPair> unchangedSchemas = new LinkedList<>();
//		Map<String, CompatibilityLevel> updateCompatibility = new HashMap<>();
		Map<String, SchemaPair> updateCompatibility = new LinkedHashMap<>();
		Map<String, List<RepoSchema>> ambiguousSubjects = new LinkedHashMap<>();
		Map<String, CompatibilityError> compatibilityErrors = new LinkedHashMap<>();
		List<Subject> notInRepositorySubjects = new LinkedList<>();
		
		repoSubjects.forEach((s, l) -> {
			
			if (l.size() > 1) {
				for (RepoSchema r : l) {
					if (r == l.get(0)) continue;
					if (!r.getSchema().equals(l.get(0).getSchema())) {
						ambiguousSubjects.computeIfAbsent(s, n -> new LinkedList<RepoSchema>(Arrays.asList(l.get(0)))).add(r);
					}
				}
				if (ambiguousSubjects.get(s) != null && ambiguousSubjects.get(s).size() > 1) {
					// at this point, the subject is not uniquely defined and cannot be represented by the repository. Fix the repo first!
					return;
				}
			}
			
			Subject regSubj = regSubjectsMap.get(s);
			RepoSchema repoSchema = l.get(0);

			if (regSubj == null) {
				// new subject found!!
				newSubjectsInRepo.put(s, repoSchema);
				return;
			}
			
			if (!regSubj.getCompatibility().equals(repoSchema.getMetadata().getCompatibilityLevel(s))) {
//				updateCompatibility.put(s, repoSchema.getMetadata().getCompatibilityLevel(s));
				updateCompatibility.put(s, new SchemaPair(repoSchema, regSubj));
			}
			
			// by this time, we have a single schema in the list of all of them are the same, so it should be safe to take the first one.
				
			ParsedSchema regSubjSchema = regSubj.getLatestSchema();
			if ( repoSchema.getSchema().equals(regSubjSchema)) {
				unchangedSchemas.add(new SchemaPair(repoSchema, regSubj));
				// if schemas are the same, there is nothing to update in registry.
				return;
			}
			
			// test compatibility against the level set in repository, because it will be updated in registry
			List<String> compErrors = repoSchema.getSchema().isCompatible(
					repoSchema.getMetadata().getCompatibilityLevel(s), 
					regSubj.getSchemas());
				
			if (compErrors == null || compErrors.isEmpty()) {
				if (RepoUtils.equalsIgnoreRefs(repoSchema.getSchema(), regSubj.getLatestSchema())) {
					// schema already exists and up to date
					return;
				}else {
					newVersionFound.put(s, new SchemaPair(repoSchema, regSubj));
				}
			}else {
				compatibilityErrors.put(s, new CompatibilityError(s, repoSchema, compErrors));
			}
		});
		
		regSubjectsMap.forEach((sn, s) ->{
			if (!repoSubjects.containsKey(sn)) {
				notInRepositorySubjects.add(s);
			}
		});
		
		
		
		CheckChangesResponse response = new CheckChangesResponse(
				newSubjectsInRepo,
				newVersionFound,
				unchangedSchemas,
				updateCompatibility,
				ambiguousSubjects,
				compatibilityErrors,
				notInRepositorySubjects
		);
		
		return response;
	}
}
