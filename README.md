# use2java - USE tool to Java code generator
## (Java code from UML class diagrams)
This project was developed within the [Software Systems Engineering group](https://ciencia.iscte-iul.pt/centres/istar-iul/groups/sse) at the [ISTAR Research Center](https://ciencia.iscte-iul.pt/centres/istar-iul) at the [ISCTE-IUL university](https://www.iscte-iul.pt/) in Lisbon, Portugal.

## Introduction
**use2java** is a model-driven Java code and JUnit test cases generator that takes as input a domain model produced and validated with the [USE (UML-based Specification Environment)](http://useocl.sourceforge.net/w/index.php/Main_Page). The latter allows to specify models using features found in UML class diagrams (classes, associations, etc.), enriched with expressions in OCL (Object Constraint Language) to specify both integrity and business constraints. **use2java** is particularly suited for developing BIS (business information systems) applications, providing a quick prototyping framework. Generated code maps the navigations on class diagrams allowable in OCL, therefore increasing traceability between model and code and reversely. A seamlessness approach to persistence, based on a pure object-oriented database, is used.

## Code generator
The generated code is organized in three layers: the business layer, the presentation layer and the persistence layer, that will be described herein.

### Business Layer
This layer includes a Java class for each UML class in the domain model, holding the same name. Each generated class has one private attribute for every attribute and for every association in the domain model. Besides, it contains public constructors, selectors and modifiers for all attributes and associations (one to one, one to many, many to many, association class to their members and vice-versa) in the domain model. Object serializers and comparators are provided as well. The syntax of the generated code follows OCL naming conventions. Chosen Java collection types (Set/HashSet, List/ArrayList, SortedSet/TreeSet and Queue/ArrayDeque) match closely the ones found in OCL (Set, Bag, OrderedSet and Sequence). A public static allInstances() selector allows retrieving all instances of this class from the object oriented database.

### Presentation Layer
This layer includes a simple Swing interface that allows performing basic CRUD operations on all classes of the problem domain. This layer includes the class Main_<domainModelName> that contains the main() method.

### Persistence Layer
This layer provides a façade to interface Database for Objects (DB4O), an open source object database engine available at http://www.db4o.com This façade provides basic CRUD (create, read, update and delete) capabilities, along with cleanup and lookup ones.

### Test cases generator
This generator produces JUnit test cases that exercise all of the generated Java code. In other words, the generated test cases provide a 100% coverage.

## About
The use2java tool was developed by Fernando Brito e Abreu and has been used for teaching Software Engineering and MDD (model-driven development) topics at [ISCTE-IUL](http://www.iscte-iul.pt) where he serves as a teaching staff member.
