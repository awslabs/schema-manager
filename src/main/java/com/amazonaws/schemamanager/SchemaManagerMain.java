/*
 *    Copyright [yyyy] [name of copyright owner]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.amazonaws.schemamanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.amazonaws.schemamanager.properties.AppConfigHelper;

public class SchemaManagerMain {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SchemaManagerMain.class);
	
	private static final String PROP_NAME_HELP_SHORT = "h";
	private static final String PROP_NAME_HELP_FULL = "help";

	private static final String PROP_NAME_CONFIG_FILE_SHORT = "c";
	private static final String PROP_NAME_CONFIG_FILE_FULL = "config-file";
	private static final String PROP_NAME_MODE_SHORT = "m";
	private static final String PROP_NAME_MODE_FULL = "mode";
	
	private static SchemaManagerMode mode;
	
	public static void main(String[] args) throws Exception{
		int result = init(args);
		if (result > 0) throw new RuntimeException("SchemaManager error code: " + result);
		if (result == 0) return;
		
		ISchemaManagerRunner schemaManager = null;
		switch(mode) {
		case PR_VALIDATION:{
			schemaManager = new SchemaManagerPrValidator();
			break;
		}
		case BUILD_VALIDATION:{
			throw new Exception("Build validation is not implemented yet");
		}
		case FULL: {
			schemaManager = new SchemaManagerTask();
			break;
			// throw new Exception("Full mode is not implemented yet");
		}
		default:{
			printHelp(options);
		}
		}
		
		schemaManager.run();
	}
	
	private static Options options;
	private static int init(String[] args) {
		options = new Options();
		options.addOption(Option.builder(PROP_NAME_HELP_SHORT)
				.argName(PROP_NAME_HELP_SHORT)
				.longOpt(PROP_NAME_HELP_FULL)
				.desc("prints this message")
				.build()
		);
		options.addOption(Option.builder(PROP_NAME_CONFIG_FILE_SHORT)
				.argName(PROP_NAME_CONFIG_FILE_SHORT)
				.longOpt(PROP_NAME_CONFIG_FILE_FULL)
				.hasArg(true)
				.desc("YAML property file with Schema Manager configuration. "
						+ "For sample, see sample_config.yml under src/main/resources.")
				.build()
		);
		options.addOption(Option.builder(PROP_NAME_MODE_SHORT)
				.argName(PROP_NAME_MODE_SHORT)
				.longOpt(PROP_NAME_MODE_FULL)
				.hasArg(true)
				.desc("define a scenario. Valid values are: `full` - for integrated full sync execution with reporting; "
						+ "`pr` - validate Pull Requests; `build` - run to validate post PR merge integrity and generate artifacts. "
						+ "Default (if not provided) is `full`.")
				.build()
		);
		
		CommandLineParser parser = new DefaultParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        if (line.hasOption(PROP_NAME_HELP_SHORT)) {
	        	printHelp(options);
		        return 0;
	        }else if (!line.hasOption(PROP_NAME_CONFIG_FILE_SHORT)){
	        	printHelp(options);
	        	List<Option> missingOptions = new ArrayList<Option>(1);
	        	missingOptions.add(options.getOption(PROP_NAME_CONFIG_FILE_SHORT));
	        	throw new MissingOptionException(missingOptions);
	        }
	        String configFilePath = line.getOptionValue(PROP_NAME_CONFIG_FILE_SHORT);
	        AppConfigHelper.initConfig(configFilePath);
	   
	        mode = line.hasOption(PROP_NAME_MODE_SHORT)
	        				? SchemaManagerMode.forName(line.getOptionValue(PROP_NAME_MODE_SHORT)) 
	        				: SchemaManagerMode.FULL;
	    }
	    catch( ParseException | IllegalArgumentException e ) {
	        // oops, something went wrong
	        log.error( "Parsing failed.  Reason: " + e.getMessage(), e);
	        return 500;
	    } catch (IOException e) {
			log.error("Couldn't initialize application config. Check the path to the file.", e);
			return 500;
		}
	    
	    return -1;
	}
	
	private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "SchemaManager", options );
	}
}
