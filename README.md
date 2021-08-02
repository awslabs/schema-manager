# Schema Manager

## Overview

Enterprises building event-driven systems or data buses using streaming technologies like Apache Kafka or Kinesis Data Streams, leverage schemas. Schemas (Apache Avro, Protobuf) define data payloads between these disparate systems/applications, ensuring that producers and consumers understand the data. Schema Registries store schemas and enforce compatibility between each schema version. Validating and deploying schemas to a schema registry is often decoupled from development of applications that use the schema. This leads to manual build and deploy processes of schemas between source repository and schema registry. 
Schema Manager automates this synchronization. Schema Manager assists enterprises in validating and deploying schemas between Schema Registries and source control repositories.


### Features

- Local development validation
- Pull Request validation
- Build validation
- Diff between Git-based repository and runtime SR instance, including compatibility tests
- Deploy changes from the schema repository to the Schema Registry instance


### Road Map Features

- Glue Schema Registry implementation
- Full Protobuf and Json support
- Dynamic reporting schema support 

## Setup

1. Clone the project from the repository
	`git clone <repository_endpoint>/amak-schema-manager.git`
2. `cd amak-schema-manager`
3. Build: `mvn clean install -DskipTests`
4. `chmod +x target/bin/*.sh`
5. Update `conf/*` relevant files with the environment specific endpoints for BitBucket, Schema Regsitry, etc.

Copy `target/*` to a target environment. Or execute from this directory.

## Getting Started
This guide assumes that you have an S3 bucket created and have access to this S3 bucket from the machine that will be executing Schema Manager. Necessary permissions are Put and Get

### Step 1
Configure config file. Using the given [parameters](/doc/configurations.md) update the config file to match your needed setup. 

### Step 2
Now that your configuration file is created, you are ready to run Schema Manager. Below are the three modes to run schema manager in:

#### Pull Request Validation (PR validation)
 In this mode, Schema Manager will validate a schema against both the master branch version of the schema and what is currently deployed in Schema Registry with the schema that is being purposed inside of the pull request. This ensures that the proposed schema is compatible and that the master branch and schema registry remain in-sync. 

#### Schema Deployer
In this mode, Schema Manager can consume and deploy all schemas found inside of a repository to a Schema Registry. This mode is useful for tasks like migrating schemas to new environments or clusters. 

#### Status Check 
In this mode, Schema Manager verifies or does a status check on the schemas specified in the configuration and compares the repository schemas to what is inside of Schema Registry. It will output any differences between the two sources. 

### Step 3
Once the mode is chosen you can either call java class directly with the parameters or create a shell script with the necessary parameters to pass. 
Examples:
-	[Deployer Config](/bin/schema-manager-deploy.sh)
-	[PR Validator](/bin/schema-manager-validate-pr.sh)
-	[Status Check](/bin/schema-manager-status-check.sh)

### Step 4
Review logs for errors and warnings. Any error in processing of a schema or Schema Manager's inability to do so will be logged. If you did not have errors you can check your s3 bucket for the data generated. 
 
# Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.


# License
[Apache-2.0 LICENSE](LICENSE)


