package org.palladiosimulator.jdt.generator.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.Type;
import org.palladiosimulator.jdt.core.visitor.AnnotationVisitor;
import org.palladiosimulator.jdt.core.visitor.TypeDeclarationVisitor;
import org.palladiosimulator.jdt.metamodel.javamodel.annotations.AnnotationsFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.annotations.impl.AnnotationAttributeImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.annotations.impl.AnnotationAttributeSettingImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.annotations.impl.AnnotationInstanceImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.annotations.impl.AnnotationParameterListImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.annotations.impl.SingleAnnotationParameterImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.ClassifiersFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.AnnotationImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.ClassImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.ConcreteClassifierImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.EnumerationImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.InterfaceImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.containers.impl.CompilationUnitImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.generics.GenericsFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.generics.impl.QualifiedTypeArgumentImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.generics.impl.TypeParameterImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.references.ReferencesFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.references.impl.StringReferenceImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.TypesFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.ClassifierReferenceImpl;
import org.eclipse.jdt.core.dom.MemberValuePair;

public class ClassifierGenerator {
	
	ModelGenerator modelGenerator;
	MemberGenerator memberGenerator;
	Map<AbstractTypeDeclaration, ConcreteClassifierImpl> classes = new HashMap<AbstractTypeDeclaration, ConcreteClassifierImpl>();
	static Map<String, ClassImpl> internals = new HashMap<String, ClassImpl>();
	static Map<String, ClassImpl> externals = new HashMap<String, ClassImpl>();
	static Map<String, AnnotationImpl> resolvedAnnotations = new HashMap<String, AnnotationImpl>();
    static Map<String, InterfaceImpl> interfaces = new HashMap<String, InterfaceImpl>();
    static Map<String, EnumerationImpl> enums = new HashMap<String, EnumerationImpl>();
    static Map<String, TypeParameterImpl> typeparams = new HashMap<String, TypeParameterImpl>();
    
	
	public ClassifierGenerator(ModelGenerator modelGenerator) {
		this.modelGenerator = modelGenerator;
		memberGenerator = new MemberGenerator(this);
	}

	public void addClassifiersToCompUnit(Entry<CompilationUnit, CompilationUnitImpl> entry) {

		final TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
		entry.getKey().accept(visitor);
		
		// create class
		visitor.getClassDeclarations().forEach(c -> {
			ClassImpl newClass = createClass(c.getName());
            entry.getValue().getClassifiers().add(newClass);                
            classes.put(c, newClass);
            internals.put(newClass.getName(), newClass);
		});
		
		// create interface
		visitor.getInterfaceDeclarations().forEach(i -> {
			InterfaceImpl newInterface = createInterface(i.getName());
			entry.getValue().getClassifiers().add(newInterface);
			classes.put(i, newInterface);
		});		
		
		visitor.getEnumDeclarations().forEach(e -> {
			EnumerationImpl newEnum = createEnum(e.getName());
			entry.getValue().getClassifiers().add(newEnum);
			classes.put(e, newEnum);
		});
	}
	
	
	
	public void completeClassifiers() {
		for (Entry<AbstractTypeDeclaration, ConcreteClassifierImpl> entry : classes.entrySet()) {
			// add enum constants
			if(entry.getKey().getNodeType() == ASTNode.ENUM_DECLARATION) {
        		memberGenerator.addConstants(entry);
        		addImplements((EnumDeclaration) entry.getKey(), (EnumerationImpl) entry.getValue());
        	}
			else if(entry.getKey().getNodeType() == ASTNode.TYPE_DECLARATION && !((TypeDeclaration)entry.getKey()).isInterface()) {
				addTypeParameters((TypeDeclaration) entry.getKey(), (ClassImpl) entry.getValue());
				addImplements((TypeDeclaration) entry.getKey(), (ClassImpl) entry.getValue());
				addExtends((TypeDeclaration) entry.getKey(), (ClassImpl) entry.getValue());
			}
			else if(entry.getKey().getNodeType() == ASTNode.TYPE_DECLARATION){
				addTypeParameters((TypeDeclaration) entry.getKey(), (InterfaceImpl) entry.getValue());
				addExtends((TypeDeclaration) entry.getKey(), (InterfaceImpl) entry.getValue());
			}
			
			memberGenerator.addFields(entry);
        	memberGenerator.addMethods(entry);
        	addAnnotations(entry);
        }

	}
	
	private void addTypeParameters(TypeDeclaration dec, ConcreteClassifierImpl classImpl) {
		@SuppressWarnings("unchecked")
		List<TypeParameter> paramList = dec.typeParameters();
		for(TypeParameter param : paramList) {
			TypeParameterImpl paramImpl = (TypeParameterImpl) GenericsFactory.eINSTANCE.createTypeParameter();
			paramImpl.setName(String.valueOf(param.getName()));
			typeparams.put(paramImpl.getName(), paramImpl);	
			
			@SuppressWarnings("unchecked")
			List<Type> typeList = param.typeBounds();
			for(Type t : typeList) {
				paramImpl.getExtendTypes().add(createClassifierReference(t.resolveBinding()));
			}
			
			classImpl.getTypeParameters().add(paramImpl);
			
		}
	}
	
	private void addImplements(EnumDeclaration dec, EnumerationImpl enumImpl) {
		if(dec.superInterfaceTypes().isEmpty()) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<Type> interfaceTypes = (List<Type>) dec.superInterfaceTypes();
		for(Type t : interfaceTypes) {
			enumImpl.getImplements().add(createClassifierReference(t.resolveBinding()));
		}
	}
	
	private void addImplements(TypeDeclaration dec, ClassImpl classImpl) {
		if(dec.superInterfaceTypes().isEmpty()) {
			return;
		}
		@SuppressWarnings("unchecked")
		List<Type> interfaceTypes = (List<Type>) dec.superInterfaceTypes();
		for(Type t : interfaceTypes) {
			classImpl.getImplements().add(createClassifierReference(t.resolveBinding()));
		}
	}
	
	private void addExtends(TypeDeclaration dec, ClassImpl classImpl) {
		
		if(dec.getSuperclassType() == null) {
			return;
		}		
		
		Type classType = dec.getSuperclassType();
		classImpl.setExtends(createClassifierReference(classType.resolveBinding()));

		//TODO: ParameterizedType
	}
	
	private void addExtends(TypeDeclaration dec, InterfaceImpl interfaceImpl) {
		
		// interfaces can extend multiple interfaces!
		@SuppressWarnings("unchecked")
		List<Type> interfaceTypes = (List<Type>) dec.superInterfaceTypes();
		for(Type t : interfaceTypes) {
			interfaceImpl.getExtends().add(createClassifierReference(t.resolveBinding()));
		}
				
		//TODO: ParameterizedType
	}
	
	@SuppressWarnings("unchecked")
	private void addAnnotations(Entry<AbstractTypeDeclaration, ConcreteClassifierImpl> entry) {
		final AnnotationVisitor annoVisitor = new AnnotationVisitor();
    	entry.getKey().accept(annoVisitor);
    	annoVisitor.getVisitedNodes().stream().forEach(a -> {
    		
    		System.out.println(a);
    		System.out.println(a.getParent());
    		System.out.println(a.getTypeNameProperty());
    		AnnotationImpl anno = createAnnotation(a.getTypeName().toString(), false);
			AnnotationInstanceImpl annoinstance = createAnnotationInstance(anno);
			
			if(a.isSingleMemberAnnotation()) {
				System.out.println("SMA:  "+a);
				SingleMemberAnnotation sma = (SingleMemberAnnotation) a;
				SingleAnnotationParameterImpl param = (SingleAnnotationParameterImpl) AnnotationsFactory.eINSTANCE.createSingleAnnotationParameter();
				
				//TODO: Resolve Expression properly
				StringReferenceImpl stringref = (StringReferenceImpl) ReferencesFactory.eINSTANCE.createStringReference();
				stringref.setValue(String.valueOf(sma.getValue()));
				
				param.setValue(stringref);    				
				annoinstance.setParameter(param);
				
			}
			else if(a.isNormalAnnotation()) {
				System.out.println("NA:   "+a);
				NormalAnnotation na = (NormalAnnotation) a;
				
				AnnotationParameterListImpl paramList = (AnnotationParameterListImpl) AnnotationsFactory.eINSTANCE.createAnnotationParameterList();
				
				na.values().forEach(mvPair -> {
					AnnotationAttributeSettingImpl setting = (AnnotationAttributeSettingImpl) AnnotationsFactory.eINSTANCE.createAnnotationAttributeSetting();
					
					AnnotationAttributeImpl attribute = (AnnotationAttributeImpl) AnnotationsFactory.eINSTANCE.createAnnotationAttribute();					
					attribute.setName(String.valueOf(((MemberValuePair) mvPair).getName()));
					StringReferenceImpl attributeRef = (StringReferenceImpl) ReferencesFactory.eINSTANCE.createStringReference();
					attributeRef.setValue(String.valueOf(((MemberValuePair) mvPair).getName()));
					attribute.setDefaultValue(attributeRef);
					setting.setAttribute(attribute);
					
					StringReferenceImpl valueRef = (StringReferenceImpl) ReferencesFactory.eINSTANCE.createStringReference();
					valueRef.setValue(String.valueOf(((MemberValuePair) mvPair).getValue()));
					setting.setValue(valueRef);
					
					paramList.getSettings().add(setting);
				});
					
				annoinstance.setParameter(paramList);				
			}
    		
    		// class annotation 
    		if(a.getParent().getClass().isInstance(entry.getKey())) {
    			     			
    			if(!resolvedAnnotations.containsKey(anno.getName())) {
    				modelGenerator.getCompilationUnits().get(a.getParent().getParent()).getClassifiers().add(anno);
    			}
    			// add annotation to compilation unit
    			//units.get(a.getParent().getParent()).getClassifiers().add(anno);
    			//annotations.put(anno.getName(), anno);
    			// add annotation instance to class
    			classes.get(a.getParent()).getAnnotationsAndModifiers().add(annoinstance);
    			
    			
    		}
    		//else if(a.getParent().getClass().isInstance(fields.keySet().stream().findFirst().get())) {		//ACHTUNG NULLPOINTEREXCEPTION WENN LEER -> TODO
    			// TODO field annotation
    		//}
    		// method annotation
    		else if(a.getParent().getClass().isInstance(memberGenerator.getMethods().keySet().stream().findFirst().get())) {		//ACHTUNG NULLPOINTEREXCEPTION WENN LEER -> TODO
    			   			
    			if(!resolvedAnnotations.containsKey(anno.getName())) {
    				System.out.println(a.getParent());
    				modelGenerator.getCompilationUnits().get(a.getParent().getParent().getParent()).getClassifiers().add(anno);
    			}        			
    			// add annotation to compilation unit
    			//units.get(a.getParent().getParent().getParent()).getClassifiers().add(anno);
    			//annotations.put(anno.getName(), anno);
    			// add annotation instance to method
    			if(memberGenerator.getMethods().get(a.getParent()) != null) {
    				memberGenerator.getMethods().get(a.getParent()).getAnnotationsAndModifiers().add(annoinstance);
    			}    			
    		}
    	});
	}
	
	public boolean containsInternalClass(String name) {
		return internals.containsKey(name);
	}
	
	public ClassImpl getInternalClass(String name) {
		return internals.get(name);
	}
	
	public boolean containsExternalClass(String name) {
		return externals.containsKey(name);
	}
	
	public ClassImpl getExternalClass(String name) {
		return externals.get(name);
	}
	
	public boolean containsInterface(String name) {
		return interfaces.containsKey(name);
	}
	
	public InterfaceImpl getInterface(String name) {
		return interfaces.get(name);
	}
	
	public boolean containsAnnotation(String name) {
		return resolvedAnnotations.containsKey(name);
	}
	
	public AnnotationImpl getAnnotation(String name) {
		return resolvedAnnotations.get(name);
	}
	
	public ClassImpl createExternalClass(final Object name) {
		if(externals.containsKey(String.valueOf(name))) {
    		return externals.get(String.valueOf(name));
    	}
		ClassImpl newClass = (ClassImpl) ClassifiersFactory.eINSTANCE.createClass();
        newClass.setName(String.valueOf(name));
		externals.put(String.valueOf(name), newClass);
        return newClass;
	}
		
	private static ClassImpl createClass(final Object name) {
    	if(internals.containsKey(String.valueOf(name))) {
    		return internals.get(String.valueOf(name));
    	}
        ClassImpl newClass = (ClassImpl) ClassifiersFactory.eINSTANCE.createClass();
        newClass.setName(String.valueOf(name));
        internals.put(String.valueOf(name), newClass);
        return newClass;
    }
	
	public InterfaceImpl createInterface(final Object name) {
    	if(interfaces.containsKey(String.valueOf(name))) {
    		return interfaces.get(String.valueOf(name));
    	}
        InterfaceImpl newInterface = (InterfaceImpl) ClassifiersFactory.eINSTANCE.createInterface();
        newInterface.setName(String.valueOf(name));
        interfaces.put(String.valueOf(name), newInterface);
        return newInterface;
    }
	
	public EnumerationImpl createEnum(final Object name) {
    	if(enums.containsKey(String.valueOf(name))) {
    		return enums.get(String.valueOf(name));
    	}
    	EnumerationImpl newEnum = (EnumerationImpl) ClassifiersFactory.eINSTANCE.createEnumeration();
    	newEnum.setName(String.valueOf(name));
        enums.put(String.valueOf(name), newEnum);
        return newEnum;
    }
	
	public AnnotationImpl createAnnotation(final Object name, boolean resolved) {
    	if(resolvedAnnotations.containsKey(String.valueOf(name))) {
    		return resolvedAnnotations.get(String.valueOf(name));
    	}
    	AnnotationImpl newAnno = (AnnotationImpl) ClassifiersFactory.eINSTANCE.createAnnotation();
		newAnno.setName(String.valueOf(name));
		if(resolved) {
			resolvedAnnotations.put(String.valueOf(name), newAnno);
		}
    	return newAnno;
    }
	
	
    private AnnotationInstanceImpl createAnnotationInstance(AnnotationImpl anno) {
    	AnnotationInstanceImpl newAnnoInstance = (AnnotationInstanceImpl) AnnotationsFactory.eINSTANCE.createAnnotationInstance();
    	newAnnoInstance.setAnnotation(anno);
    	return newAnnoInstance;
    }
    
	
	public ClassifierReferenceImpl createClassifierReference(ITypeBinding binding) {
    	ClassifierReferenceImpl ref = (ClassifierReferenceImpl) TypesFactory.eINSTANCE.createClassifierReference();
    	
    	String name = binding.getTypeDeclaration().getName();
    	
		if(internals.containsKey(name)) {
			ref.setTarget(internals.get(name));
		}
		else if(externals.containsKey(name)) {
			ref.setTarget(externals.get(name));
		}
		else if(interfaces.containsKey(name)) {
			ref.setTarget(interfaces.get(name));
		}
		else if(resolvedAnnotations.containsKey(name)) {
			ref.setTarget(resolvedAnnotations.get(name));
		}
		else if(enums.containsKey(name)) {
			ref.setTarget(enums.get(name));
		}
		else if(typeparams.containsKey(name)) {
			ref.setTarget(typeparams.get(name));
		}
		else {
			CompilationUnitImpl newCompUnit = modelGenerator.createCompilationUnit(binding.getQualifiedName()+".java");
			if(binding.getPackage()!=null) {
				modelGenerator.addNamespaces(newCompUnit, binding.getPackage().getNameComponents());
			}
			if(binding.isClass()) {
				ClassImpl newClass = createExternalClass(binding.getName());
				newCompUnit.getClassifiers().add(newClass);
    			ref.setTarget(newClass);
    			externals.put(binding.getName(), newClass);
			}
			else if(binding.isInterface() && !(binding.isAnnotation())) {
				InterfaceImpl newInterface = createInterface(binding.getName());
				newCompUnit.getClassifiers().add(newInterface);
				ref.setTarget(newInterface);
			}
			else if(binding.isAnnotation()) {
				AnnotationImpl newAnno = createAnnotation(binding.getName(), true);
				newCompUnit.getClassifiers().add(newAnno);
				ref.setTarget(newAnno);
			}    	
		}
		
		if(binding.isParameterizedType()) {			
			for(ITypeBinding argbinding : binding.getTypeArguments()) {
				ClassifierReferenceImpl argref = createClassifierReference(argbinding);				
				QualifiedTypeArgumentImpl arg = (QualifiedTypeArgumentImpl) GenericsFactory.eINSTANCE.createQualifiedTypeArgument();
				arg.setTypeReference(argref);
				ref.getTypeArguments().add(arg);
			}
    	}
		if(binding.isGenericType()) {
//			System.out.println("GENERIC: "+name);
    	}
		
		/*else {
			ref = createExternalClassifierReference(binding);
		}*/
		
		
    	return ref;
    }
    
    /*private ClassifierReferenceImpl createExternalClassifierReference(ITypeBinding binding) {
    	
    	ClassifierReferenceImpl ref = (ClassifierReferenceImpl) TypesFactory.eINSTANCE.createClassifierReference();
		PackageImpl newPackage;
		if(externals.containsKey(binding.getName())) {
			ref.setTarget(externals.get(binding.getName()));
			if(externals.containsKey(binding.getName())) {        				
	    		ref.setTarget(externals.get(binding.getName()));
	    	}
			else {				
				newPackage = modelGenerator.getExternalPackage(binding.getQualifiedName());
				CompilationUnitImpl newCompUnit = modelGenerator.createCompilationUnit(binding.getQualifiedName()+".java");
				modelGenerator.addNamespaces(newCompUnit, binding.getPackage().getNameComponents());
				newPackage.getCompilationUnits().add(newCompUnit);
				if(binding.isClass()) {
					System.out.println("interface: "+binding.getPackage());
					ClassImpl newClass = createExternalClass(binding.getName());
					newCompUnit.getClassifiers().add(newClass);
	    			ref.setTarget(newClass);
	    			externals.put(binding.getName(), newClass);
				}
				else if(binding.isInterface() && !(binding.isAnnotation())) {
					System.out.println("interface: "+binding.getPackage());
					InterfaceImpl newInterface = createInterface(binding.getName());
					newCompUnit.getClassifiers().add(newInterface);
					ref.setTarget(newInterface);
				}
				else if(binding.isAnnotation()) {
					System.out.println("anno: "+binding.getPackage());
					AnnotationImpl newAnno = createAnnotation(binding.getName(), true);
					newCompUnit.getClassifiers().add(newAnno);
					ref.setTarget(newAnno);
				}    			
			}
		}
		else {
			newPackage = modelGenerator.createPackage(binding.getQualifiedName());
			CompilationUnitImpl newCompUnit = modelGenerator.createCompilationUnit(binding.getQualifiedName()+".java");
			modelGenerator.addNamespaces(newCompUnit, binding.getPackage().getNameComponents());
			newPackage.getCompilationUnits().add(newCompUnit);
			
			if(binding.isClass()) {
				ClassImpl newClass = createExternalClass(binding.getName());
				newCompUnit.getClassifiers().add(newClass);
    			ref.setTarget(newClass);
    			externals.put(binding.getName(), newClass);
			}
			else if(binding.isInterface() && !(binding.isAnnotation())) {
				InterfaceImpl newInterface = createInterface(binding.getName());
				newCompUnit.getClassifiers().add(newInterface);
				ref.setTarget(newInterface);
			}
			else if(binding.isAnnotation()) {
				AnnotationImpl newAnno = createAnnotation(binding.getName(), true);
				newCompUnit.getClassifiers().add(newAnno);
				ref.setTarget(newAnno);
			}    	
			
			modelGenerator.addPackage(newPackage);			
		}
		// TODO resolve classes further
		return ref;
    	
    }*/

}
