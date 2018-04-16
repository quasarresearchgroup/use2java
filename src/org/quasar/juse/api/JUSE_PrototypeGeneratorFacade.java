/*
 * J-USE - Java prototyping for the UML based specification environment (USE)
 * Copyright (C) 2012 Fernando Brito e Abrey, QUASAR research group
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.quasar.juse.api;

/***********************************************************
* @author fba
* 19 de Abr de 2012
*
***********************************************************/
public interface JUSE_PrototypeGeneratorFacade extends JUSE_BasicFacade
{
	/***********************************************************
	 * @param author
	 *            The author of the specification
	 * @param javaWorkspace
	 *            Workspace directory where the generated Java prototype is to be created
	 * @param basePackageName
	 *            Full name of the base package where the code of the generated Java prototype will be placed
	 * @param businessLayerName
	 *            Relative name of the layer package where the source code for the business layer is to be placed
	 * @param presentationLayerName
	 *            Relative name of the layer package where the source code for the presentation layer is to be placed
	 * @param persistenceLayerName
	 *            Relative name of the layer package where the source code for the persistence layer is to be placed	
	 * @param libraryDirectory
	 *           Relative name of the library directory
	 * @param db4oJar
	 *           DB4Objects jar filename to be put on the library directory
	 ************************************************************/
	public void javaGeneration(String author, String javaWorkspace, String basePackageName, 
					String businessLayerName, String presentationLayerName, String persistenceLayerName, String libraryDirectory, String db4oJar);

	/***********************************************************
	 * @param javaWorkspace
	 *            Workspace directory where the generated Java prototype was created
	 * @param basePackageName
	 *            Full name of the base package where the code of the generated Java prototype was placed
	 * @param businessLayerName
	 *            Relative name of the layer package where the source code for the business layer was placed
	 * @param databaseDirectory
	*			 Relative name of the database directory 
	***********************************************************/
	public void storeState(String javaWorkspace, String basePackageName, String businessLayerName, String databaseDirectory);
}