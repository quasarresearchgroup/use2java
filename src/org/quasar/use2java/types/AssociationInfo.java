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
import java.util.Collection;

import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MModel;

public class AssociationInfo
{
	private AssociationKind			kind;
	private MAssociationEnd		sourceAE, targetAE;
	private MAssociationClass		associationClass;

	/***********************************************************
	 * @param kind
	 * @param sourceAE
	 * @param targetAE
	 * @param associationClass
	 ***********************************************************/
	public AssociationInfo(AssociationKind kind, MAssociationEnd sourceAE, MAssociationEnd targetAE,
					MAssociationClass associationClass)
	{
		this.kind = kind;
		this.sourceAE = sourceAE;
		this.targetAE = targetAE;
		this.associationClass = associationClass;
	}

	/***********************************************************
	 * @return the kind
	 ***********************************************************/
	public AssociationKind getKind()
	{
		return kind;
	}

	/***********************************************************
	 * @return the sourceAE
	 ***********************************************************/
	public MAssociationEnd getSourceAE()
	{
		return sourceAE;
	}

	/***********************************************************
	 * @return the targetAE
	 ***********************************************************/
	public MAssociationEnd getTargetAE()
	{
		return targetAE;
	}

	/***********************************************************
	 * @return the sourceAE class
	 ***********************************************************/
	public MClass getSourceAEClass()
	{
		if (this.kind == AssociationKind.ASSOCIATIVE2MEMBER)
			return this.associationClass;
		else
			return sourceAE.cls();
	}

	/***********************************************************
	 * @return the targetAE class
	 ***********************************************************/
	public MClass getTargetAEClass()
	{
		if (this.kind == AssociationKind.MEMBER2ASSOCIATIVE)
			return this.associationClass;
		else
			return targetAE.cls();
	}

	/***********************************************************
	 * @return the associationClass
	 ***********************************************************/
	public MAssociationClass getAssociationClass()
	{
		return associationClass;
	}

	/***********************************************************
	 * @return
	 ***********************************************************/
	public AssociationInfo swapped()
	{
		return new AssociationInfo(this.kind, this.targetAE, this.sourceAE, this.associationClass);
	}

	/***********************************************************
	 * @param theClass
	 *            whose root we want
	 * @param the
	 *            Association whose root we want
	 * @return the root parent of the class and association passed as parameters
	 ***********************************************************/
	private static MClass associationBaseAncestor(MClass theClass, MAssociation ass)
	{
		return (!theClass.parents().isEmpty() && !theClass.associations().contains(ass)) ? associationBaseAncestor(theClass
						.parents().iterator().next(), ass) : theClass;
	}

	/***********************************************************
	 * same as getAssociationsInfo(MClass theClass) but works with all associations (also inherited associations) getSource not
	 * tested but very usefull when using getTarget with this method we can rapidly get all the possible targets of a sub-class
	 * ;)
	 * 
	 * @param theClass
	 * @return
	 ***********************************************************/
	public static Collection<AssociationInfo> getAllAssociationsInfo(MClass theClass)
	{
		Collection<AssociationInfo> result = new ArrayList<AssociationInfo>();

		if (theClass instanceof MAssociationClass)
		{
			MAssociationClass associationClass = (MAssociationClass) theClass;

			MAssociationEnd ae1 = associationClass.associationEnds().get(0);
			MAssociationEnd ae2 = associationClass.associationEnds().get(1);

			result.add(new AssociationInfo(AssociationKind.ASSOCIATIVE2MEMBER, ae1, ae2, associationClass));
			result.add(new AssociationInfo(AssociationKind.ASSOCIATIVE2MEMBER, ae2, ae1, associationClass));
		}

		for (MAssociation assoc : theClass.allAssociations())
		{
			if (assoc.associationEnds().size() > 2)
				System.err.println("ERROR (" + assoc.name() + "): Only binary associations are supported. "
								+ "Notice that n-ary associations with n>2 can always be broken down into binary associations!");
			else
			{
				MAssociationEnd ae1 = assoc.associationEnds().get(0);
				MAssociationEnd ae2 = assoc.associationEnds().get(1);
				MAssociationEnd sourceAE = ae1.cls() == associationBaseAncestor(theClass, assoc) ? ae1 : ae2;
				MAssociationEnd targetAE = ae1.cls() == associationBaseAncestor(theClass, assoc) ? ae2 : ae1;

				if (assoc instanceof MAssociationClass)
				{
					MAssociationClass targetAssociationClass = (MAssociationClass) assoc;

					result.add(new AssociationInfo(AssociationKind.MEMBER2ASSOCIATIVE, sourceAE, targetAE,
									targetAssociationClass));

					result.add(new AssociationInfo(AssociationKind.MEMBER2MEMBER, sourceAE, targetAE, targetAssociationClass));
				}
				else
				{
					if (!sourceAE.isCollection() && !targetAE.isCollection())
						result.add(new AssociationInfo(AssociationKind.ONE2ONE, sourceAE, targetAE, null));
					else
						if (sourceAE.isCollection() != targetAE.isCollection())
							result.add(new AssociationInfo(AssociationKind.ONE2MANY, sourceAE, targetAE, null));
						else
							if (sourceAE.isCollection() && targetAE.isCollection())
								result.add(new AssociationInfo(AssociationKind.MANY2MANY, sourceAE, targetAE, null));
							else
								result.add(new AssociationInfo(AssociationKind.UNKNOWN, sourceAE, targetAE, null));
				}
			}

		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return sourceAE.cls() + "[" + sourceAE.name() + ":" + sourceAE.multiplicity() + "] -> " + targetAE.cls() + "["
						+ targetAE.name() + ":" + targetAE.multiplicity() + "]   (" + kind + ")"
						+ (associationClass == null ? "" : " [" + associationClass + "]");
	}

	/***********************************************************
	 * @param theClass
	 * @return
	 ***********************************************************/
	public static Collection<AssociationInfo> getAssociationsInfo(MClass theClass)
	{
		Collection<AssociationInfo> result = new ArrayList<AssociationInfo>();

		if (theClass instanceof MAssociationClass)
		{
			MAssociationClass associationClass = (MAssociationClass) theClass;

			MAssociationEnd ae1 = associationClass.associationEnds().get(0);
			MAssociationEnd ae2 = associationClass.associationEnds().get(1);

			result.add(new AssociationInfo(AssociationKind.ASSOCIATIVE2MEMBER, ae1, ae2, associationClass));
			result.add(new AssociationInfo(AssociationKind.ASSOCIATIVE2MEMBER, ae2, ae1, associationClass));
		}

		for (MAssociation assoc : theClass.associations())
		{
			if (assoc.associationEnds().size() > 2)
				System.err.println("ERROR (" + assoc.name() + "): Only binary associations are supported. "
								+ "Notice that n-ary associations with n>2 can always be broken down into binary associations!");
			else
			{
				MAssociationEnd ae1 = assoc.associationEnds().get(0);
				MAssociationEnd ae2 = assoc.associationEnds().get(1);
				MAssociationEnd sourceAE = ae1.cls() == theClass ? ae1 : ae2;
				MAssociationEnd targetAE = ae1.cls() == theClass ? ae2 : ae1;

				if (assoc instanceof MAssociationClass)
				{
					MAssociationClass targetAssociationClass = (MAssociationClass) assoc;

					result.add(new AssociationInfo(AssociationKind.MEMBER2ASSOCIATIVE, sourceAE, targetAE,
									targetAssociationClass));

					result.add(new AssociationInfo(AssociationKind.MEMBER2MEMBER, sourceAE, targetAE, targetAssociationClass));
				}
				else
				{
					if (!sourceAE.isCollection() && !targetAE.isCollection())
						result.add(new AssociationInfo(AssociationKind.ONE2ONE, sourceAE, targetAE, null));
					else
						if (sourceAE.isCollection() != targetAE.isCollection())
							result.add(new AssociationInfo(AssociationKind.ONE2MANY, sourceAE, targetAE, null));
						else
							if (sourceAE.isCollection() && targetAE.isCollection())
								result.add(new AssociationInfo(AssociationKind.MANY2MANY, sourceAE, targetAE, null));
							else
								result.add(new AssociationInfo(AssociationKind.UNKNOWN, sourceAE, targetAE, null));
				}
			}
		}
		return result;
	}

	/***********************************************************
	* 
	***********************************************************/
	public static void testGetAssociationInfo(MModel model)
	{
		for (MClass cls : model.classes())
			for (AssociationInfo ai : getAssociationsInfo(cls))
				System.out.println(ai);
	}
}