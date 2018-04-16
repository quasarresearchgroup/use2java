/***********************************************************
 * Filename: MainExample.java
 * Created:  24 de Mar de 2012
 ***********************************************************/
package org.quasar.usemodel2java;

import org.quasar.juse.api.JUSE_PrototypeGeneratorFacade;
import org.quasar.juse.api.implementation.PrototypeGeneratorFacade;
import org.tzi.use.uml.sys.MSystem;

/***********************************************************
 * @author fba 2012-2015
 * 
 ***********************************************************/
public final class JUSE_CodeGenerator extends JUSE_PrototypeGeneratorConstants
{

	
	/***********************************************************
	 * @param args
	 * @throws InterruptedException
	 ***********************************************************/
	public static void main(String[] args) throws InterruptedException
	{
		JUSE_PrototypeGeneratorFacade api = new PrototypeGeneratorFacade();

		api.initialize(args, USE_BASE_DIRECTORY, MODEL_DIRECTORY);

		MSystem theSystem = api.compileSpecification(MODEL_FILE, true);

		System.out.println("\n" + PLUGIN_ID + ", " + COPYRIGHT);
		System.out.println("\n\t Generating Java code for the " + theSystem.model().name() + " prototype");
		
		api.readSOIL(MODEL_DIRECTORY, SOIL_FILE, true);

		api.command("info state");
		
		api.javaGeneration(AUTHORS, JAVA_WORKSPACE, TARGET_PACKAGE, BUSINESSLAYER_NAME,
						PRESENTATIONLAYER_NAME, PERSISTENCELAYER_NAME, LIBRARY_DIRECTORY, DB4O_JAR);
		
		 api.dumpState("Fernando Brito e Abreu", JAVA_WORKSPACE, CMD_FILE, false);
	}

}
