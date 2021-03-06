/***********************************************************
 * Filename: MainExample.java
 * Created:  24 de Mar de 2012
 ***********************************************************/
package org.quasar.use2java;

import org.quasar.use2java.generator.IPrototypeGenerator;
import org.quasar.use2java.generator.PrototypeGenerator;
import org.tzi.use.uml.sys.MSystem;

/***********************************************************
 * @author fba 2012-2015
 * 
 ***********************************************************/
public final class MAIN_CodeGenerator extends MAIN_PrototypeGeneratorConstants
{

	
	/***********************************************************
	 * @param args
	 * @throws InterruptedException
	 ***********************************************************/
	public static void main(String[] args) throws InterruptedException
	{
		IPrototypeGenerator api = new PrototypeGenerator();

		api.initialize(args, USE_BASE_DIRECTORY, MODEL_DIRECTORY);

		MSystem theSystem = api.compileSpecification(MODEL_FILE, true);

		System.out.println("\n" + PLUGIN_ID + ", " + COPYRIGHT);
		System.out.println("\n\t Generating Java code for the " + theSystem.model().name() + " prototype");
		
//		api.readSOIL(MODEL_DIRECTORY, SOIL_FILE, false);
//
//		api.command("info state");
		
		api.javaGeneration(AUTHORS, JAVA_WORKSPACE, TARGET_PACKAGE, BUSINESSLAYER_NAME,
						PRESENTATIONLAYER_NAME, PERSISTENCELAYER_NAME, LIBRARY_DIRECTORY, DB4O_JAR);
		
//		api.dumpState("Fernando Brito e Abreu", JAVA_WORKSPACE, CMD_FILE, false);
	}

}
