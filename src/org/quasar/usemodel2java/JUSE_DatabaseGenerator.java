/***********************************************************
 * Filename: MainExample.java
 * Created:  24 de Mar de 2013
 ***********************************************************/
package org.quasar.usemodel2java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.util.regex.Pattern;

import org.quasar.juse.api.JUSE_ProgramingFacade;
import org.quasar.juse.api.JUSE_PrototypeGeneratorFacade;
import org.quasar.juse.api.implementation.ProgramingFacade;
import org.quasar.juse.api.implementation.PrototypeGeneratorFacade;


/***********************************************************
 * @author fba 2013-2015
 * 
 ***********************************************************/
public final class JUSE_DatabaseGenerator extends JUSE_PrototypeGeneratorConstants
{
	/***********************************************************
	 * @param args
	 * @throws InterruptedException
	 ***********************************************************/
	public static void main(String[] args) throws InterruptedException
	{
		// System.out.println(d.replaceAll("\\\\", "\\\\\\\\"));

		// mainLoadData();

		mainRoundDataTrip();
	}

	/***********************************************************
	 * @param args
	 * @throws InterruptedException
	 ***********************************************************/
	public static void mainLoadData()
	{
		JUSE_ProgramingFacade api = new ProgramingFacade();

		String[] args = new String[0];

		api.initialize(args, USE_BASE_DIRECTORY, MODEL_DIRECTORY);

		api.compileSpecification(MODEL_FILE, true);

		if (api.readSOIL(MODEL_DIRECTORY + "/", SOIL_FILE, false))
		{
			String oclExpression = null;
			do
			{
				System.out.print("Enter OCL expression: ");

				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

				try
				{
					oclExpression = br.readLine();
					System.out.println(api.oclEvaluator(oclExpression));
				}
				catch (IOException ioe)
				{
					System.out.println("IO error trying to read oclExpression!");
					System.exit(1);
				}
			}
			while (!oclExpression.equals("0"));

		}

	}

	/***********************************************************
	 * @param args
	 * @throws InterruptedException
	 ***********************************************************/
	public static void mainRoundDataTrip()
	{
		JUSE_PrototypeGeneratorFacade api = new PrototypeGeneratorFacade();

		String[] args = new String[0];

		api.initialize(args, USE_BASE_DIRECTORY, MODEL_DIRECTORY);

		api.compileSpecification(MODEL_FILE, true);

		System.out.println("\n" + PLUGIN_ID + ", " + COPYRIGHT);

		if (api.readSOIL(MODEL_DIRECTORY, SOIL_FILE, true))
		{
//			api.command("info state");

			api.storeState(JAVA_WORKSPACE, TARGET_PACKAGE, BUSINESSLAYER_NAME, DATABASE_DIRECTORY);
		}

		// api.dumpState("Fernando Brito e Abreu", MODEL_DIRECTORY, CMD_FILE, false);
		//
		// api.command("reset");
		// api.command("info state");
		//
		// api.readSOIL(MODEL_DIRECTORY, CMD_FILE, false);
		// api.command("info state");
	}
}