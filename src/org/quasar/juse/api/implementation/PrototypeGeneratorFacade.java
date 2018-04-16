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

package org.quasar.juse.api.implementation;

import java.lang.reflect.Constructor;
// import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.quasar.juse.api.JUSE_PrototypeGeneratorFacade;
import org.quasar.juse.persistence.Database;
import org.quasar.toolkit.FileSystemUtilities;
import org.quasar.toolkit.SourceFileWriter;
import org.tzi.use.uml.mm.MAssociationClass;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.type.EnumType;
// import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.value.BooleanValue;
import org.tzi.use.uml.ocl.value.EnumValue;
import org.tzi.use.uml.ocl.value.IntegerValue;
import org.tzi.use.uml.ocl.value.RealValue;
import org.tzi.use.uml.ocl.value.StringValue;
import org.tzi.use.uml.sys.MLink;
import org.tzi.use.uml.sys.MLinkObject;
import org.tzi.use.uml.sys.MObject;
import org.tzi.use.uml.sys.MObjectState;

/***********************************************************
 * @author fba 25 de Abr de 2012
 * 
 ***********************************************************/
public class PrototypeGeneratorFacade extends BasicFacade implements
		JUSE_PrototypeGeneratorFacade {
	private Map<Integer, Object> objectMapper = null;

	public PrototypeGeneratorFacade() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.quasar.juse.api.JUSE_PrototypeGeneratorFacade#javaGeneration(java
	 * .lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void javaGeneration(String author, String javaWorkspace,
			String basePackageName, String businessLayerName,
			String presentationLayerName, String persistenceLayerName,
			String libraryDirectory, String db4oJar) {
		if (getSystem().model() == null) {
			System.out.println("Please compile the specification first!");
			return;
		}

		// AssociationInfo.testGetAssociationInfo(model);
		// ModelUtilities mu = new ModelUtilities(model);
		// mu.printModelUtilities();

		JavaVisitor visitor = new JavaBusinessVisitor(getSystem().model(),
				author, basePackageName, businessLayerName,
				persistenceLayerName, presentationLayerName);

		String targetDirectory = javaWorkspace + "/"
				+ getSystem().model().name() + "/src/"
				+ basePackageName.replace('.', '/') + "/" + businessLayerName;

		String presentationDirectory = javaWorkspace + "/"
				+ getSystem().model().name() + "/src/"
				+ basePackageName.replace('.', '/') + "/"
				+ presentationLayerName;

		String persistenceDirectory = javaWorkspace + "/"
				+ getSystem().model().name() + "/src/"
				+ basePackageName.replace('.', '/') + "/"
				+ persistenceLayerName;

		String libraryPath = javaWorkspace + "/" + getSystem().model().name()
				+ "/" + libraryDirectory;

		FileSystemUtilities.createDirectory(presentationDirectory);

		FileSystemUtilities.createDirectory(persistenceDirectory);
		// FileSystemUtilities.copyFile(javaWorkspace +
		// "/J-USE/src/org/quasar/juse/persistence/Database.java",
		// persistenceDirectory
		// + "/Database.java");
		FileSystemUtilities.copyFile(
				"src/org/quasar/juse/persistence/Database.java",
				persistenceDirectory + "/Database.java");

		FileSystemUtilities.replaceStringInFile(persistenceDirectory
				+ "/Database.java", "org.quasar.juse.persistence",
				basePackageName + "." + persistenceLayerName);

		FileSystemUtilities.createDirectory(libraryPath);
		FileSystemUtilities.copyFile(libraryDirectory + "/" + db4oJar,
				libraryPath + "/" + db4oJar);
		// visitAnnotations(e);

		// print user-defined data types
		for (EnumType t : getSystem().model().enumTypes()) {
			if (SourceFileWriter.openSourceFile(targetDirectory, t.name()
					+ ".java")) {
				// visitAnnotations(t);
				visitor.printEnumType(t, businessLayerName);
				SourceFileWriter.println();
				SourceFileWriter.closeSourceFile();
			}
		}

		// visit classes
		for (MClass cls : getSystem().model().classes()) {
			if (SourceFileWriter.openSourceFile(targetDirectory, cls.name()
					+ ".java")) {
				visitor.printClassHeader(cls, businessLayerName);

				SourceFileWriter.incIndent();

				visitor.printAllInstances(cls);

				visitor.printAttributes(cls);

				if (cls instanceof MAssociationClass)
					visitor.printAssociativeConstructor(cls);
				else
					visitor.printDefaultConstructor(cls);

				visitor.printParameterizedConstructor(cls);

				visitor.printBasicGettersSetters(cls);

				visitor.printNavigators(cls);

				boolean toStringInSOIL = false;
				for (MOperation op : cls.operations()) {
					visitor.printSoilOperation(op);
					if (op.name().equals("toString"))
						toStringInSOIL = true;
				}

				visitor.printInvariants(cls);

				visitor.printCompareTo(cls);

				visitor.printEquals(cls);

				if (!toStringInSOIL)
					visitor.printToString(cls);

				SourceFileWriter.decIndent();
				SourceFileWriter.println("}");

				SourceFileWriter.closeSourceFile();
			}
		}

		for (Integer n : JavaTypes.getTupleTypesCardinalities())
			if (SourceFileWriter.openSourceFile(targetDirectory, "Tuple" + n
					+ ".java")) {
				visitor.printTupleTypes(n, businessLayerName);
				SourceFileWriter.closeSourceFile();
			}

		if (SourceFileWriter.openSourceFile(presentationDirectory, "Main_"
				+ getSystem().model().name() + ".java")) {
			visitor.printMain();
			SourceFileWriter.closeSourceFile();
		}

		ModelUtilities util = new ModelUtilities(getSystem().model());
		System.out.println("\t - code generation concluded ("
				+ util.numberClasses() + " classes, " + util.numberAttributes()
				+ " attributes, " + util.numberOperations() + " operations)\n");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.quasar.juse.api.JUSE_PrototypeGeneratorFacade#storeState(java.lang
	 * .String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void storeState(String javaWorkspace, String basePackageName,
			String businessLayerName, String databaseDirectory) {
		String classPath = basePackageName + "." + businessLayerName;
		String databasePath = javaWorkspace + "/" + getSystem().model().name()
				+ "/" + databaseDirectory;

		if (getSystem() == null || getSystem().model() == null) {
			System.out.println("Please compile the specification first!");
			return;
		}

		objectMapper = new HashMap<Integer, Object>(getSystem().state()
				.numObjects());

		System.out.println();

		FileSystemUtilities.createDirectory(databasePath);

		Database.open(databasePath, getSystem().model().name(), "db4o");

		Database.cleanUp();

		System.out.println("\t Generating Java objects and storing them in "
				+ Database.currentDatabase());

		generateRegularObjects(classPath);

		generateLinkObjects(classPath);

		setObjectsState(classPath);

		generateLinks(classPath);

		saveObjectsInDatabase();

		Database.close();
	}

	/***********************************************************
	 * @param classpath
	 ***********************************************************/
	private void generateRegularObjects(String classpath) {
		int regularObjects = 0;
		for (MClass aClass : getSystem().model().classes()) {
			if (!(aClass instanceof MAssociationClass)) {
				Class<?> c;
				try {
					c = Class.forName(classpath + "." + aClass.name());

					Constructor<?> javaConstructor = c.getDeclaredConstructor();

					for (MObject useObject : getSystem().state()
							.objectsOfClass(aClass)) {
						// System.out.println(useObject);
						objectMapper.put(useObject.hashCode(),
								javaConstructor.newInstance());
						regularObjects++;
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		System.out
				.println("\t - stored " + regularObjects + " regular objects");
	}

	/***********************************************************
	 * @param aClass
	 * @return
	 ***********************************************************/
	private boolean isAssociationClassToRegularClasses(MClass aClass) {
		boolean result = true;
		for (MClass assClass : ((MAssociationClass) aClass).associatedClasses())
			if (assClass instanceof MAssociationClass)
				result = false;
		return result;
	}

	/***********************************************************
	 * @param classpath
	 ***********************************************************/
	private void generateLinkObjects(String classpath) {
		int totalLinkObjects = 0;

		// generates link objects whose connected objects are regular objects
		for (MClass aClass : getSystem().model().classes())
			if (aClass instanceof MAssociationClass)
				if (isAssociationClassToRegularClasses(aClass))
					totalLinkObjects += generateLinkObjectsFromClass(classpath,
							aClass);

		// generates link objects whose connected objects are link objects
		for (MClass aClass : getSystem().model().classes())
			if (aClass instanceof MAssociationClass)
				if (!isAssociationClassToRegularClasses(aClass))
					totalLinkObjects += generateLinkObjectsFromClass(classpath,
							aClass);

		System.out.println("\t - stored " + totalLinkObjects + " link objects");
	}

	/***********************************************************
	 * @param classpath
	 * @param aClass
	 ***********************************************************/
	private int generateLinkObjectsFromClass(String classpath, MClass aClass) {
		int linkObjects = 0;
		Class<?> c, ac1, ac2;
		try {
			c = Class.forName(classpath + "." + aClass.name());

			MAssociationClass associationClass = (MAssociationClass) aClass;

			ac1 = Class.forName(classpath + "."
					+ associationClass.associationEnds().get(0).cls().name());
			ac2 = Class.forName(classpath + "."
					+ associationClass.associationEnds().get(1).cls().name());

			Constructor<?> javaConstructor = c.getDeclaredConstructor(ac1, ac2);

			for (MObject useObject : getSystem().state().objectsOfClass(aClass)) {
				// System.out.println(useObject);
				MLinkObject linkObject = (MLinkObject) useObject;

				ArrayList<MObject> linked = new ArrayList<MObject>();
				for (MObject anObject : linkObject.linkedObjects())
					linked.add(anObject);

				objectMapper.put(useObject.hashCode(), javaConstructor
						.newInstance(
								objectMapper.get(linked.get(0).hashCode()),
								objectMapper.get(linked.get(1).hashCode())));

				linkObjects++;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return linkObjects;
	}

	/***********************************************************
	 * @param classpath
	 ***********************************************************/
	private void generateLinks(String classpath) {
		int totalLinks = 0;

		for (MLink theLink : getSystem().state().allLinks())
			if (!(theLink instanceof MObject)) {
				// System.out.println("!insert (" +
				// theLink.linkedObjects().get(0).name() + ", "
				// + theLink.linkedObjects().get(1).name() + ") into " +
				// theLink.association().name());

				Object target = objectMapper.get(theLink.linkedObjects().get(0)
						.hashCode());
				Object argument = objectMapper.get(theLink.linkedObjects()
						.get(1).hashCode());

				String argumentRole = theLink.association().associationEnds()
						.get(1).nameAsRolename();

				String methodName;
				if (theLink.association().associationEnds().get(1)
						.isCollection())
					methodName = "add"
							+ SourceFileWriter.capitalize(argumentRole);
				else
					methodName = "set"
							+ SourceFileWriter.capitalize(argumentRole);

				// System.out.println(theLink.linkedObjects().get(0).name() +
				// "." + methodName + "("
				// + theLink.linkedObjects().get(1) + ": "
				// +theLink.linkedObjects().get(1).type().shortName() +")");

				Class<?> c = null;
				try {
					c = Class.forName(classpath + "."
							+ theLink.linkedObjects().get(0).cls().name());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				if (invokeMethod(target, argument, argument.getClass(),
						methodName, c))
					totalLinks++;
			}
		System.out.println("\t - stored " + totalLinks
				+ " links (association instances)");
	}

	/***********************************************************
	 * @param target
	 * @param argument
	 * @param methodName
	 * @param c
	 * @return
	 ***********************************************************/
	private boolean invokeMethod(Object target, Object argument,
			Class<?> argumentClass, String methodName, Class<?> c) {
		Method m = null;
		try {
			m = c.getMethod(methodName, argumentClass);
			m.invoke(target, argument);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// System.out.println("invokeMethod(...): Seeking method " +
			// methodName + " in superclass " +
			// argumentClass.getSuperclass().getSimpleName());
			invokeMethod(target, argument, argumentClass.getSuperclass(),
					methodName, c);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return true;
	}

	/***********************************************************
	 * @param classpath
	 ***********************************************************/
	private void setObjectsState(String classpath) {
		for (MClass aClass : getSystem().model().classes()) {
			Class<?> c = null;
			try {
				c = Class.forName(classpath + "." + aClass.name());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			for (MObject useObject : getSystem().state().objectsOfClass(aClass)) {
				Object javaObject = objectMapper.get(useObject.hashCode());

				MObjectState useObjectState = useObject.state(getSystem()
						.state());

				for (MAttribute attribute : aClass.allAttributes())
					setJavaObjectAttribute(classpath, c, javaObject,
							useObjectState, attribute);
			}
		}
		System.out.println("\t - objects state has been set!");
	}

	/***********************************************************
	 * @param classpath
	 * @param c
	 * @param javaObject
	 * @param useObjectState
	 * @param attribute
	 ***********************************************************/
	private void setJavaObjectAttribute(String classpath, Class<?> c,
			Object javaObject, MObjectState useObjectState, MAttribute attribute) {
		// System.out.println("\t" + c.getSimpleName() + "." + attribute.name()
		// + " = " + useObjectState.attributeValue(attribute));

		Method m = null;
		Object argument = null;

		String methodName = "set"
				+ SourceFileWriter.capitalize(attribute.name());
		if (useObjectState.attributeValue(attribute).isDefined()) {
			try {
				// m = c.getDeclaredMethod(methodName, toClass(classpath,
				// attribute.type()));
				// m = c.getMethod(methodName, toClass(classpath,
				// attribute.type()));

				for (Method method : c.getMethods())
					if (method.getName().equals(methodName))
						m = method;

				if (m == null)
					throw new NoSuchMethodException();

				if (attribute.type().isTypeOfBoolean())
					m.invoke(javaObject, ((BooleanValue) useObjectState
							.attributeValue(attribute)).value());

				if (attribute.type().isTypeOfInteger())
					m.invoke(javaObject, ((IntegerValue) useObjectState
							.attributeValue(attribute)).value());

				if (attribute.type().isTypeOfReal())
					if (useObjectState.attributeValue(attribute).type()
							.isTypeOfInteger())
						m.invoke(javaObject, ((IntegerValue) useObjectState
								.attributeValue(attribute)).value() * 1.0);
					else
						m.invoke(javaObject, ((RealValue) useObjectState
								.attributeValue(attribute)).value());

				if (attribute.type().isTypeOfString())
					m.invoke(javaObject, ((StringValue) useObjectState
							.attributeValue(attribute)).value());

				if (attribute.type().isTypeOfEnum()) {
					String enumValue = ((EnumValue) useObjectState
							.attributeValue(attribute)).value();
					Class<?> enumClass = Class.forName(classpath + "."
							+ attribute.type().shortName());
					
		            for (Object constant : enumClass.getEnumConstants()) { 
		                Enum<?> enumConstant = (Enum<?>)constant;
		                if (enumConstant.name().equals(enumValue)) {
		                	m.invoke(javaObject, enumConstant);
		                    break;
		                }
		            }

//						@SuppressWarnings("unchecked")
//						Enum<?> e = Enum.valueOf(
//								enumClass.asSubclass(Enum.class), enumValue);
//
//			                
//						// Field f = c.getDeclaredField(attribute.name());
//						// f.setAccessible(true);
//						// f.set(javaObject, e);
//
//						m.invoke(javaObject, e);
				}

				// if (attribute.type().isObjectType())
				if (attribute.type().isTypeOfClass()) {
					argument = objectMapper.get(useObjectState.attributeValue(
							attribute).hashCode());
					m.invoke(javaObject, argument);
				}
			} catch (IllegalArgumentException e) {
				System.out.println("IllegalArgumentException: "
						+ javaObject.hashCode() + "." + m.getName() + "("
						+ argument.hashCode() + " : "
						+ argument.getClass().getSimpleName() + ")");
			} catch (NoSuchMethodException e) {
				System.out.println("NoSuchMethodException: " + c.getName()
						+ "." + methodName + "("
						+ useObjectState.attributeValue(attribute) + " : "
						+ attribute.type() + ")");
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException: " + c.getName()
						+ "." + m.getName() + "("
						+ useObjectState.attributeValue(attribute) + " : "
						+ attribute.type() + ")");
			} catch (IllegalAccessException e) {
				System.out.println("IllegalAccessException: " + c.getName()
						+ "." + m.getName() + "("
						+ useObjectState.attributeValue(attribute) + " : "
						+ attribute.type() + ")");
			} catch (InvocationTargetException e) {
				System.out.println("InvocationTargetException: " + c.getName()
						+ "." + m.getName() + "("
						+ useObjectState.attributeValue(attribute) + " : "
						+ attribute.type() + ")");
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			// catch (NoSuchFieldException e)
			// {
			// e.printStackTrace();
			// }
		}
	}

	/***********************************************************
	 * @param classpath
	 * @param oclType
	 * @return
	 * @throws ClassNotFoundException
	 ***********************************************************/
	/*
	 * private Class<?> toClass(String classpath, Type oclType) throws
	 * ClassNotFoundException { // System.out.println(oclType); // if
	 * (oclType.isNumber()) // return int.class;
	 * 
	 * if (oclType.isTypeOfInteger()) return int.class;
	 * 
	 * if (oclType.isTypeOfReal()) return double.class;
	 * 
	 * if (oclType.isTypeOfBoolean()) return boolean.class;
	 * 
	 * if (oclType.isTypeOfString()) return String.class;
	 * 
	 * if (oclType.isTypeOfEnum()) return Class.forName(classpath + "." +
	 * oclType.toString());
	 * 
	 * // if (oclType.isObjectType()) if (oclType.isTypeOfClass()) return
	 * Class.forName(classpath + "." + oclType.toString());
	 * 
	 * // if (oclType.isTrueObjectType()) // return Class.forName(classpath +
	 * "." + oclType.toString());
	 * 
	 * if (oclType.isTypeOfOclAny()) return Object.class;
	 * 
	 * if (oclType.isTypeOfVoidType()) return void.class;
	 * 
	 * if (oclType.isTypeOfOrderedSet()) return
	 * Class.forName(oclType.toString().substring(11,
	 * oclType.toString().length() - 1));
	 * 
	 * if (oclType.isTypeOfSet()) return
	 * Class.forName(oclType.toString().substring(4, oclType.toString().length()
	 * - 1));
	 * 
	 * // if (oclType.isCollection(true)) // return "Set<Object>"; // if
	 * (oclType.isTrueCollection()) // return "TrueCollection"; // if
	 * (oclType.isTrueSet()) // return "TrueSet"; // if (oclType.isSequence())
	 * // return "Sequence"; // if (oclType.isTrueSequence()) // return
	 * "TrueSequence"; // if (oclType.isTrueOrderedSet()) // return "Number"; //
	 * if (oclType.isBag()) // return "Bag"; // if (oclType.isTrueBag()) //
	 * return "TrueBag"; // if (oclType.isInstantiableCollection()) // return
	 * "InstantiableCollection"; // if (oclType.isTupleType(true)) // return
	 * "Tuple";
	 * 
	 * return null; }
	 */

	/***********************************************************
	* 
	***********************************************************/
	private void saveObjectsInDatabase() {
		System.out.println("\t - saving " + objectMapper.values().size()
				+ " objects to the database ");
		Database.insert(objectMapper.values());
	}
}