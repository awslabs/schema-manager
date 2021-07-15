## Setup Conf file 
-	Application Name – Name of schema manager application that will be deployed
-	Application instance ID
-	Environment – Environment where application is to be deployed
-	Repo Client Class  - Type of repository and the associated class where schemas will live. This tells the application what type of repo client to build while executing
-	Registry Client Class – Type of Registry that the schema will be published to or evaluated against. Currently supported is Confluent Schema Registry. 
-	Reporter Class – Type of reporting mechanism (S3) where the data output from the application is sent
-	Analyzer Class –  Type of analyzer for the application to use 
-	Deploy -  Boolean to say if you want the schemas in the repository to be deployed into the schema registry. If not provided, defaults to false
### Repo Client Properties
-	Repo Endpoint – URL of the repository to use for schemas.
-	Base Info – Base of the URL where schema manager should begin its traversing. Default scopes are important to pay attention to when setting base info
-	Path Prefix – Specific paths for Schema Manager to walk 
-	Credentials – Credentials for Schema Manager to use to authenticate to Repository
-	Subject Name Pattern – Pattern for Schema Manager to use to identify subjects
-	Schema Name Pattern – Patter for Schema Manager to use to identify schemas
### Local Repo Client Properties
-	Base Info – Base of schema directory Schema Manager should use to walk on the local file system. 
### Registry Client Properties
-	Registry Endpoint – URL of the Registry endpoint.
### Analyzer Properties
-	Default Compatibility – Default compatibility for schema manager to use if no other default at the schema, subject, or directory level are provided. Options are NONE, BACKWARD, BACKWARD_TRANSITIVE, FORWARD, FORWARD_TRANSITIVE, FULL, FULL_TRANSITIVE. Read more about compatibility types [here](https://docs.confluent.io/platform/current/schema-registry/avro.html)
-	Allow Schema Name Duplicates -  if allowed schemas sharing the same name are allowed to exist within the same registry
### Reporter Properties
-	Bucket Name – Name of S3 bucket where data from Schema Manager should be written
-	Prefix Directory – Name of Directory for Schema Manager to write data to
-	File Name Pattern – Pattern Schema Manager should use when creating objects inside of the bucket. 
