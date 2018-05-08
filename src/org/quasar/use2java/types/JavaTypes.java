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

package org.quasar.use2java.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.tzi.use.uml.ocl.type.*;
import org.tzi.use.uml.ocl.type.TupleType.Part;
import org.tzi.use.uml.ocl.type.Type.VoidHandling;

/***********************************************************
 * @author fba 26 de Mai de 2013
 * 
 ***********************************************************/
public abstract class JavaTypes
{
	private static Set<Integer>	tupleTypesCardinalities	= new HashSet<Integer>();

	/***********************************************************
	 * @return
	 ***********************************************************/
	public static Set<Integer> getTupleTypesCardinalities()
	{
		return tupleTypesCardinalities;
	}

	/***********************************************************
	 * @param collectionType
	 * @return
	 ***********************************************************/
	public static Type oclCollectionInnerType(CollectionType collectionType)
	{
		return collectionType.elemType();
	}

	/***********************************************************
	 * @param oclType
	 * @return
	 ***********************************************************/
	public static String javaInterfaceType(Type oclType)
	{
		if (oclType.isTypeOfOrderedSet())
			return "SortedSet<" + javaInterfaceType(((OrderedSetType) oclType).elemType()) + ">";
		
		if (oclType.isTypeOfSet())
			return "Set<" + javaInterfaceType(((SetType) oclType).elemType()) + ">";
		
		if (oclType.isTypeOfSequence())
			return "Queue<" + javaInterfaceType(((SequenceType) oclType).elemType()) + ">";
		
		if (oclType.isTypeOfBag())
			return "List<" + javaInterfaceType(((BagType) oclType).elemType()) + ">";
		
		if (oclType.isKindOfTupleType(VoidHandling.INCLUDE_VOID))
			return javaTupleType((TupleType) oclType);

		return javaPrimitiveType(oclType);
	}

	/***********************************************************
	 * @param oclType
	 * @return
	 ***********************************************************/
	public static String javaImplementationType(Type oclType)
	{
		if (oclType.isTypeOfOrderedSet())
			return "TreeSet<" + javaImplementationType(((OrderedSetType) oclType).elemType()) + ">";

		if (oclType.isTypeOfSet())
			return "HashSet<" + javaImplementationType(((SetType) oclType).elemType()) + ">";
		
		if (oclType.isTypeOfSequence())
			return "ArrayDeque<" + javaImplementationType(((SequenceType) oclType).elemType()) + ">";
		
		if (oclType.isTypeOfBag())
			return "ArrayList<" + javaImplementationType(((BagType) oclType).elemType()) + ">";
		
		if (oclType.isKindOfTupleType(VoidHandling.INCLUDE_VOID))
			return javaTupleType((TupleType) oclType);

		return javaPrimitiveType(oclType);
	}

	/***********************************************************
	 * @param oclType
	 * @return
	 ***********************************************************/
	private static String javaTupleType(TupleType tupleType)
	{
		ArrayList<Part> tupleParts = new ArrayList<Part>(tupleType.getParts().values());
		tupleTypesCardinalities.add(tupleParts.size());

		String result = "Tuple" + tupleParts.size() + "<";
		for (int i = 0; i < tupleParts.size(); i++)
		{
			result += javaInterfaceType(tupleParts.get(i).type());
			if (i < tupleParts.size() - 1)
				result += ", ";
		}
		result += ">";
		return result;
	}

	/***********************************************************
	 * @param oclType
	 * @return
	 ***********************************************************/
	public static String javaPrimitiveType(Type oclType)
	{
//		if (oclType.isTypeOfInteger())
//			return "int";
//		if (oclType.isTypeOfReal())
//			return "double";
//		if (oclType.isTypeOfBoolean())
//			return "boolean";
		if (oclType.isTypeOfInteger())
			return "Integer";
		if (oclType.isTypeOfReal())
			return "Double";
		if (oclType.isTypeOfBoolean())
			return "Boolean";
		if (oclType.isTypeOfString())
			return "String";
		if (oclType.isTypeOfEnum())
			return oclType.shortName();
		if (oclType.isTypeOfClass())
			return oclType.toString();
		// if (oclType.isObjectType())
		// return oclType.toString();
		// if (oclType.isTrueObjectType())
		// return oclType.toString();
		if (oclType.isTypeOfOclAny())
			return "Object";
		if (oclType.isTypeOfVoidType())
			return "void";
		// if (oclType.isDate())
		// return "Date";

		return "ERROR!";
	}

	/***********************************************************
	 * @param oclType
	 * @return
	 ***********************************************************/
	public static String javaDummyValue(Type oclType)
	{
//		if (oclType.isTypeOfInteger())
//			return "-1";
//		if (oclType.isTypeOfReal())
//			return "-1.0";
//		if (oclType.isTypeOfBoolean())
//			return "true";
		if (oclType.isTypeOfInteger())
			return "null";
		if (oclType.isTypeOfReal())
			return "null";
		if (oclType.isTypeOfBoolean())
			return "null";
		if (oclType.isTypeOfString())
			return "null";
		if (oclType.isTypeOfEnum())
			return "null";
		if (oclType.isKindOfCollection(null))
			return "null";
		if (oclType.isTypeOfTupleType())
			return "null";
		if (oclType.isTypeOfClass())
			return "null";
		// if (oclType.isObjectType())
		// return "null";
		// if (oclType.isTrueObjectType())
		// return "null";
		if (oclType.isTypeOfOclAny())
			return "null";
		if (oclType.isTypeOfVoidType())
			return "";
		// if (oclType.isDate())
		// return "null";

		return "ERROR!";
	}

	/***********************************************************
	 * @param oclTypes
	 * @return
	 ***********************************************************/
	public static Set<String> javaImportDeclarations(Set<Type> oclTypes)
	{
		Set<String> result = new HashSet<String>();

		// compulsory because of "allInstances()"
		result.add("import java.util.Set;");

		// compulsory because of "allInstancesSorted()"
		result.add("import java.util.SortedSet;");
		result.add("import java.util.TreeSet;");

		for (Type oclType : oclTypes)
		{
			if (oclType != null)
			{
				if (oclType.isTypeOfSequence())
				{
					result.add("import java.util.Queue;");
					result.add("import java.util.ArrayDeque;");
				}
				if (oclType.isTypeOfOrderedSet())
				{
					result.add("import java.util.SortedSet;");
					result.add("import java.util.TreeSet;");
				}
				if (oclType.isTypeOfBag())
				{
					result.add("import java.util.List;");
					result.add("import java.util.ArrayList;");
				}
				if (oclType.isTypeOfSet())
				{
					result.add("import java.util.HashSet;");
				}
			}
		}
		return result;
	}

}