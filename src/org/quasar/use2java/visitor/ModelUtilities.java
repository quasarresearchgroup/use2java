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

package org.quasar.use2java.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.quasar.use2java.types.AssociationInfo;
import org.quasar.use2java.types.AssociationKind;
import org.quasar.use2java.types.AttributeInfo;
import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.type.Type;

/***********************************************************
 * @author fba 6 de Abr de 2012
 * 
 ***********************************************************/
public class ModelUtilities
{
	private MModel	model;

	/***********************************************************
	 * @param model
	 ***********************************************************/
	public ModelUtilities(MModel model)
	{
		this.model = model;
	}

	/***********************************************************
	 * @return
	 ***********************************************************/
	public int numberClasses()
	{
		return model.classes().size();
	}

	/***********************************************************
	 * @return
	 ***********************************************************/
	public int numberAttributes()
	{
		int result = 0;
		for (MClass aClass : model.classes())
			result += AttributeInfo.getAttributesInfo(aClass).size();
		return result;
	}

	/***********************************************************
	 * @return
	 ***********************************************************/
	public int numberOperations()
	{
		int result = 0;
		for (MClass aClass : model.classes())
		{
			result++; // allInstances() static operation
			
			if (AttributeInfo.getAttributesInfo(aClass).size() > 0)
				result++; // DefaultConstructor;

			result++; // Parameterized Constructor

			result += 2 * aClass.attributes().size(); // Getters & Setters for "native" attributes

			if (aClass instanceof MAssociationClass)
				result += 4; // Each association class adds 4 more navigators (towards the 2 members, in both directions)

			result += aClass.operations().size(); // Operations specified in OCL / SOIL
			
			result += 3; // Each class overrides compareTo(other), equals(other) and toString()
			
			// An operation is generated for each invariant, plus a generic one that calls all existing invariants 
			result +=  model.classInvariants(aClass).size() + 1;		
		}

		result += 2 * 3 * model.associations().size(); // One getter, one setter and one remover for each navigation direction

		return result;
	}

	/***********************************************************
	 * @param theClass
	 * @return
	 ***********************************************************/
	public Set<Type> getClassOutboundDependencies(MClass theClass)
	{
		Set<Type> result = new HashSet<Type>();

		for (MClass p : theClass.parents())
		{
			result.addAll(getClassOutboundDependencies(p));
			result.add(p);
		}

		for (MAttribute a : theClass.attributes())
			if (a.type().isTypeOfClass())
				result.add(a.type());

		for (MAttribute a : theClass.attributes())
			if (a.type().isTypeOfClass())
				result.add(a.type());

		if (theClass instanceof MAssociationClass)
			for (MClass member : ((MAssociationClass) theClass).associatedClasses())
				result.add(member);

		for (MOperation op : theClass.allOperations())
		{
			if (op.hasResultType() && op.resultType().isTypeOfClass())
				result.add(op.resultType());
			for (VarDecl v : op.paramList())
				if (v.type().isTypeOfClass())
					result.add(v.type());
		}

		result.remove(null);

		return result;
	}

	/***********************************************************
	 * @param theClass
	 * @return
	 ***********************************************************/
	public Set<Type> getClassInboundDependencies(MClass theClass)
	{
		Set<Type> result = new HashSet<Type>();

		for (MClass other : model.classes())
			if (other != theClass && getClassOutboundDependencies(other).contains(theClass))
				result.add(other);

		return result;
	}

	/***********************************************************
	 * @param theClass
	 * @return
	 ***********************************************************/
	public int outgoingCoupling(MClass theClass)
	{
		Set<Type> tmp = getClassOutboundDependencies(theClass);
		tmp.remove(theClass);
		return tmp.size();
	}

	/***********************************************************
	 * @param theClass
	 * @return
	 ***********************************************************/
	public int incomingCoupling(MClass theClass)
	{
		Set<Type> tmp = getClassInboundDependencies(theClass);
		tmp.remove(theClass);
		return tmp.size();
	}

	public static MClass getAssociativeClass(MClass leftMAClass, MClass rightMAClass)
	{
		for (AssociationInfo sourceAss : AssociationInfo.getAssociationsInfo(leftMAClass))
			if (sourceAss.getKind() == AssociationKind.MEMBER2ASSOCIATIVE)
				for (AssociationInfo targetAss : AssociationInfo.getAssociationsInfo(rightMAClass))
					if (targetAss.getKind() == AssociationKind.MEMBER2ASSOCIATIVE
									&& sourceAss.getTargetAEClass() == targetAss.getTargetAEClass())
						return sourceAss.getTargetAEClass();
		return null;
	}

	public static MClass getOtherMember(MAssociationClass associative, MClass member)
	{
		for (AssociationInfo sourceAss : AssociationInfo.getAssociationsInfo(member))
			if (sourceAss.getKind() == AssociationKind.MEMBER2ASSOCIATIVE && sourceAss.getTargetAEClass() == associative)
				for (AssociationInfo targetAss : AssociationInfo.getAssociationsInfo(associative))
					if (targetAss.getKind() == AssociationKind.ASSOCIATIVE2MEMBER
									&& targetAss.getSourceAEClass() == associative && targetAss.getTargetAEClass() != member)
						return targetAss.getTargetAEClass();
		return null;
	}

	public static MAssociationEnd getOtherMemberAssociation(MAssociationClass associative, MClass member)
	{
		for (AssociationInfo sourceAss : AssociationInfo.getAssociationsInfo(member))
			if (sourceAss.getKind() == AssociationKind.MEMBER2ASSOCIATIVE && sourceAss.getTargetAEClass() == associative)
				for (AssociationInfo targetAss : AssociationInfo.getAssociationsInfo(associative))
					if (targetAss.getKind() == AssociationKind.ASSOCIATIVE2MEMBER
									&& targetAss.getSourceAEClass() == associative && targetAss.getTargetAEClass() != member)
						return targetAss.getTargetAE();
		return null;
	}

	public static boolean hasAssociations(MClass cls)
	{
		if (AssociationInfo.getAllAssociationsInfo(cls).isEmpty())
			return false;
		else
			return true;
	}

	public static boolean isSpecialPrimitive(MClass theClass)
	{
		if (theClass.name().equals("CalendarDate") || theClass.name().equals("CalendarTime"))
			return true;
		else
			return false;
	}

	public static List<MClass> getAttributeObjectTypeOwners(MClass theClass)
	{
		List<MClass> list = new ArrayList<MClass>();
		for (MClass clazz : theClass.model().classes())
			if (theClass != clazz && clazz.isAnnotated() && clazz.getAnnotation("domain") != null)
				for (MAttribute att : clazz.allAttributes())
					if (att.type().isTypeOfClass() && att.type().toString().equals(theClass.name()))
						list.add(clazz);
		return list;
	}

	/***********************************************************
	 * @param theClass
	 * @return
	 ***********************************************************/
	public double typePriorityFactor(MClass theClass)
	{
		return outgoingCoupling(theClass) == 0 ? Double.POSITIVE_INFINITY : incomingCoupling(theClass)
						/ outgoingCoupling(theClass);
	}

	/***********************************************************
	 * @param classes
	 * @return
	 ***********************************************************/
	public MClass lessComplexClass(MClass... classes)
	{
		MClass lessComplex = classes[0];
		for (MClass aClass : classes)
			if (typePriorityFactor(aClass) == typePriorityFactor(lessComplex))
				if (outgoingCoupling(aClass) == outgoingCoupling(lessComplex))
					if (incomingCoupling(aClass) == incomingCoupling(lessComplex))
						if (aClass.allAttributes().size() == lessComplex.allAttributes().size())
							if (aClass.allOperations().size() == lessComplex.allOperations().size())
								lessComplex = aClass;
							else
								lessComplex = aClass.allOperations().size() < lessComplex.allOperations().size() ? aClass
												: lessComplex;
						else
							lessComplex = aClass.allAttributes().size() < lessComplex.allAttributes().size() ? aClass
											: lessComplex;
					else
						lessComplex = incomingCoupling(aClass) > incomingCoupling(lessComplex) ? aClass : lessComplex;
				else
					lessComplex = outgoingCoupling(aClass) < outgoingCoupling(lessComplex) ? aClass : lessComplex;
			else
				lessComplex = typePriorityFactor(aClass) > typePriorityFactor(lessComplex) ? aClass : lessComplex;
		return lessComplex;
	}

	/***********************************************************
	 * @param classes
	 * @return
	 ***********************************************************/
	public MClass moreComplexClass(MClass... classes)
	{
		MClass lessComplex = classes[0];
		for (MClass aClass : classes)
			if (typePriorityFactor(aClass) == typePriorityFactor(lessComplex))
				if (outgoingCoupling(aClass) == outgoingCoupling(lessComplex))
					if (incomingCoupling(aClass) == incomingCoupling(lessComplex))
						if (aClass.allAttributes().size() == lessComplex.allAttributes().size())
							if (aClass.allOperations().size() == lessComplex.allOperations().size())
								lessComplex = aClass;
							else
								lessComplex = aClass.allOperations().size() > lessComplex.allOperations().size() ? aClass
												: lessComplex;
						else
							lessComplex = aClass.allAttributes().size() > lessComplex.allAttributes().size() ? aClass
											: lessComplex;
					else
						lessComplex = incomingCoupling(aClass) < incomingCoupling(lessComplex) ? aClass : lessComplex;
				else
					lessComplex = outgoingCoupling(aClass) > outgoingCoupling(lessComplex) ? aClass : lessComplex;
			else
				lessComplex = typePriorityFactor(aClass) < typePriorityFactor(lessComplex) ? aClass : lessComplex;
		return lessComplex;
	}

	/***********************************************************
	* 
	***********************************************************/
	public void printModelUtilities()
	{
		for (MClass cls : model.classes())
		{
			System.out.println(cls + " outbound dependencies: " + getClassOutboundDependencies(cls) + outgoingCoupling(cls));
			System.out.println(cls + " inbound dependencies: " + getClassInboundDependencies(cls) + incomingCoupling(cls));
		}
		System.out.println("Less compless class: " + lessComplexClass(model.classes().toArray(new MClass[0])));
		System.out.println("More compless class: " + moreComplexClass(model.classes().toArray(new MClass[0])));
	}
}