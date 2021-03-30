package org.palladiosimulator.jdt.generator.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.palladiosimulator.jdt.core.visitor.FieldDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.MethodDeclarationVisitor;
import org.palladiosimulator.jdt.core.visitor.EnumConstantDeclarationVisitor;
import org.palladiosimulator.jdt.metamodel.javamodel.arrays.ArraysFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.arrays.impl.ArrayDimensionImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.ClassImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.ConcreteClassifierImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.classifiers.impl.EnumerationImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.generics.GenericsFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.generics.impl.QualifiedTypeArgumentImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.instantiations.InstantiationsFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.instantiations.impl.NewConstructorCallImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.members.MembersFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.members.impl.ClassMethodImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.members.impl.EnumConstantImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.members.impl.FieldImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.members.impl.MethodImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.modifiers.Modifier;
import org.palladiosimulator.jdt.metamodel.javamodel.modifiers.ModifiersFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.modifiers.impl.ModifierImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.parameters.ParametersFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.parameters.impl.OrdinaryParameterImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.references.ReferencesFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.references.impl.StringReferenceImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.TypesFactory;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.BooleanImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.ByteImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.CharImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.ClassifierReferenceImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.DoubleImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.FloatImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.IntImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.LongImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.PrimitiveTypeImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.ShortImpl;
import org.palladiosimulator.jdt.metamodel.javamodel.types.impl.VoidImpl;

public class MemberGenerator {
	
	ClassifierGenerator classGenerator;
	Map<FieldDeclaration, FieldImpl> fields = new HashMap<FieldDeclaration, FieldImpl>();
    Map<MethodDeclaration, MethodImpl> methods = new HashMap<MethodDeclaration, MethodImpl>();
    
    public MemberGenerator(ClassifierGenerator classGenerator) {
    	this.classGenerator = classGenerator;
    }
    
    //TODO: differentiate between class, interface and enum
    public void addFields(Entry<AbstractTypeDeclaration, ConcreteClassifierImpl> entry) {
    	final FieldDeclarationVisitor fieldVisitor = new FieldDeclarationVisitor();
    	entry.getKey().accept(fieldVisitor);
    	
    	fieldVisitor.getVisitedNodes().stream().forEach(f -> {
    		VariableDeclarationFragment fragment = (VariableDeclarationFragment) f.fragments().get(0);
    		FieldImpl newField = createField(fragment.getName());
    		
    		// add modifiers
    		for(int i = 0; i < f.modifiers().size(); i++) {
    			if(((IExtendedModifier) f.modifiers().get(i)).isModifier()) {
    				newField.getAnnotationsAndModifiers().add(createModifier(f.modifiers().get(i)));
//    				newField.addModifier(createModifier(f.modifiers().get(i)));
    			}
    		}
    		
    		
    		if(f.getType().isPrimitiveType()) {
    			newField.setTypeReference(createPrimitiveType(f.getType()));
    		} else if(f.getType().isSimpleType()) {
    			newField.setTypeReference(classGenerator.createClassifierReference(f.getType().resolveBinding()));
    		} else if(f.getType().isArrayType()) {
    			newField.setTypeReference(classGenerator.createClassifierReference(f.getType().resolveBinding().getElementType()));
    			
    			ArrayDimensionImpl dim = (ArrayDimensionImpl) ArraysFactory.eINSTANCE.createArrayDimension();
    			newField.getArrayDimensionsBefore().add(dim);
    		} else if(f.getType().isParameterizedType()) {
    			ClassifierReferenceImpl ref = classGenerator.createClassifierReference(f.getType().resolveBinding().getTypeDeclaration());
    			
    			resolveTypeArguments(f.getType().resolveBinding().getTypeArguments()).forEach(arg -> ref.getTypeArguments().add(arg));
    			newField.setTypeReference(ref);
    			
//    			if(fragment.getInitializer() != null) {
//    				ClassifierReferenceImpl inst_ref = classGenerator.createClassifierReference(fragment.getInitializer().resolveTypeBinding().getTypeDeclaration());
//    				
//        			resolveTypeArguments(fragment.getInitializer().resolveTypeBinding().getTypeArguments()).forEach(arg -> inst_ref.getTypeArguments().add(arg));            			
//        			
//        			NewConstructorCallImpl newInstantiation = (NewConstructorCallImpl) InstantiationsFactory.eINSTANCE.createNewConstructorCall();
//        			newInstantiation.setTypeReference(inst_ref);
//        			
//        			newField.setInitialValue(newInstantiation);            			
//    			}    			
    		}
    		
    		// add instantiation / initialization to field
    		if(fragment.getInitializer() != null) {
    			initializeField(newField, fragment.getInitializer());
    		}
    		
    		entry.getValue().getMembers().add(newField);
    		fields.put(f, newField);
    	});
    }
    
    private void initializeField(FieldImpl newField, Expression init) {
    	ITypeBinding binding = init.resolveTypeBinding();
    	
    	if(init.getNodeType() == ASTNode.STRING_LITERAL) {
    		
    	} else if(init.getNodeType() == ASTNode.NUMBER_LITERAL) {
    		
    	} else if(init.getNodeType() == ASTNode.BOOLEAN_LITERAL) {
    		
    	} else if(init.getNodeType() == ASTNode.SIMPLE_NAME) {
    		
    	} else if(init.getNodeType() == ASTNode.QUALIFIED_NAME) {
    		
    	} else if(init.getNodeType() == ASTNode.ARRAY_CREATION) {
    		
    	} else if(init.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
    		
    	} else if(init.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
    		NewConstructorCallImpl newInstantiation = (NewConstructorCallImpl) InstantiationsFactory.eINSTANCE.createNewConstructorCall();
    		ClassifierReferenceImpl inst_ref = classGenerator.createClassifierReference(binding.getTypeDeclaration());
    		
    		if(binding.isParameterizedType()) {
    			resolveTypeArguments(binding.getTypeArguments()).forEach(arg -> inst_ref.getTypeArguments().add(arg));
    		}
    		
    		newField.setInitialValue(newInstantiation);
    	}
    }
	
	private Modifier createModifier(Object object) {
		ModifierImpl newModifier;
		switch(object.toString()) {
			case "public":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createPublic();
				return newModifier;
			case "private":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createPrivate();
				return newModifier;
			case "protected":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createProtected();
				return newModifier;
			case "static":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createStatic();
				return newModifier;
			case "final":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createFinal();
				return newModifier;
			case "abstract":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createAbstract();
				return newModifier;
			case "native":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createNative();
				return newModifier;
			case "synchronized":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createSynchronized();
				return newModifier;
			case "transient":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createTransient();
				return newModifier;
			case "volatile":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createVolatile();
				return newModifier;
			case "strictfp":
				newModifier = (ModifierImpl) ModifiersFactory.eINSTANCE.createStrictfp();
				return newModifier;
		}
		return null;
	}

	//TODO: differentiate between class, interface and enum
	@SuppressWarnings("unchecked")
	public void addMethods(Entry<AbstractTypeDeclaration, ConcreteClassifierImpl> entry) {
		final MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
    	entry.getKey().accept(methodVisitor);
    	methodVisitor.getVisitedNodes().stream().forEach(m -> {
    		if(!m.isConstructor()) {
    			MethodImpl newMethod = createClassMethod(m.getName());
    			
    			// add modifiers
        		for(int i = 0; i < m.modifiers().size(); i++) {
        			if(((IExtendedModifier) m.modifiers().get(i)).isModifier()) {
        				newMethod.getAnnotationsAndModifiers().add(createModifier(m.modifiers().get(i)));
        			}
        		}
        		
    			// return type
    			if(m.getReturnType2().isPrimitiveType()) {
    				newMethod.setTypeReference(createPrimitiveType(m.getReturnType2()));
    			}
    			else if(m.getReturnType2().isSimpleType()) {
    				newMethod.setTypeReference(classGenerator.createClassifierReference(m.getReturnType2().resolveBinding()));
    			}
    			else if(m.getReturnType2().isArrayType()) {
        			newMethod.setTypeReference(classGenerator.createClassifierReference(m.getReturnType2().resolveBinding().getElementType()));
        			
        			ArrayDimensionImpl dim = (ArrayDimensionImpl) ArraysFactory.eINSTANCE.createArrayDimension();
        			newMethod.getArrayDimensionsBefore().add(dim);
    			}
    			else if(m.getReturnType2().isParameterizedType()) {
    				ClassifierReferenceImpl ref = classGenerator.createClassifierReference(m.getReturnType2().resolveBinding().getTypeDeclaration());
        			
        			resolveTypeArguments(m.getReturnType2().resolveBinding().getTypeArguments()).forEach(arg -> ref.getTypeArguments().add(arg));
        			newMethod.setTypeReference(ref);
    			}
    			
    			// parameters
    			m.parameters().forEach(p -> {
    				OrdinaryParameterImpl param = (OrdinaryParameterImpl) ParametersFactory.eINSTANCE.createOrdinaryParameter();
    				SingleVariableDeclaration dec = (SingleVariableDeclaration) p; 
    				
    				param.setName(String.valueOf(dec.getName()));
    				if(dec.getType().isPrimitiveType()) {
    					param.setTypeReference(createPrimitiveType(dec.getType()));
    				}
    				else if(dec.getType().isSimpleType()){
    					param.setTypeReference(classGenerator.createClassifierReference(dec.getType().resolveBinding()));
    				}
    				else if(dec.getType().isArrayType()) {
            			param.setTypeReference(classGenerator.createClassifierReference(dec.getType().resolveBinding().getElementType()));
            			
            			ArrayDimensionImpl dim = (ArrayDimensionImpl) ArraysFactory.eINSTANCE.createArrayDimension();
            			param.getArrayDimensionsBefore().add(dim);
    				}
    				else if(dec.getType().isParameterizedType()) {
    					ClassifierReferenceImpl ref = classGenerator.createClassifierReference(dec.getType().resolveBinding().getTypeDeclaration());
            			
            			resolveTypeArguments(dec.getType().resolveBinding().getTypeArguments()).forEach(arg -> ref.getTypeArguments().add(arg));
            			param.setTypeReference(ref);
    				}
    				newMethod.getParameters().add(param);
    			});
    			
    			entry.getValue().getMembers().add(newMethod);
    			methods.put(m, newMethod);
    		}
    	});
	}
	
	public void addConstants(Entry<AbstractTypeDeclaration, ConcreteClassifierImpl> entry) {
		final EnumConstantDeclarationVisitor constantVisitor = new EnumConstantDeclarationVisitor();
    	entry.getKey().accept(constantVisitor);
    	constantVisitor.getVisitedNodes().stream().forEach(e -> {
    		EnumConstantImpl newConstant = (EnumConstantImpl) MembersFactory.eINSTANCE.createEnumConstant();
    		newConstant.setName(String.valueOf(e.getName()));
    		
    		//TODO: proper resolution of arguments (type)
    		if(e.arguments().size()>0) {
    			StringReferenceImpl stringref = (StringReferenceImpl) ReferencesFactory.eINSTANCE.createStringReference();
    			stringref.setValue(String.valueOf(e.arguments().get(0)));
    		
    			newConstant.getArguments().add(stringref);
    		}
    		
    		EnumerationImpl myEnum = (EnumerationImpl) entry.getValue();
    		myEnum.getConstants().add(newConstant);
    		
    	});
	}
	
	private List<QualifiedTypeArgumentImpl> resolveTypeArguments(ITypeBinding[] bindings) {
    	List<QualifiedTypeArgumentImpl> arguments = new ArrayList<QualifiedTypeArgumentImpl>();
    	for(int i = 0; i < bindings.length; i++) {
			ClassifierReferenceImpl argref = (ClassifierReferenceImpl) TypesFactory.eINSTANCE.createClassifierReference();
			argref = classGenerator.createClassifierReference(bindings[i]);
			
			QualifiedTypeArgumentImpl arg = (QualifiedTypeArgumentImpl) GenericsFactory.eINSTANCE.createQualifiedTypeArgument();
			arg.setTypeReference(argref);
			arguments.add(arg);
		}
    	return arguments;
    }
	
	public Map<MethodDeclaration, MethodImpl> getMethods() {
		return methods;
	}
	
	private MethodImpl createClassMethod(final Object name) {
    	ClassMethodImpl newMethod = (ClassMethodImpl) MembersFactory.eINSTANCE.createClassMethod();
    	newMethod.setName(String.valueOf(name));
    	return newMethod;
    }
	
	private FieldImpl createField(final Object name) {
    	FieldImpl newField = (FieldImpl) MembersFactory.eINSTANCE.createField();
    	newField.setName(String.valueOf(name));
    	return newField;
    }
	
	private PrimitiveTypeImpl createPrimitiveType(Type type) {
    	PrimitiveTypeImpl newType;
    	String typeString = type.toString();
    	
    	switch(typeString)
    	{
    		case "int":
    			newType = (IntImpl) TypesFactory.eINSTANCE.createInt();
        		return newType;
    		case "boolean":
    			newType = (BooleanImpl) TypesFactory.eINSTANCE.createBoolean();
        		return newType;
    		case "byte":
    			newType = (ByteImpl) TypesFactory.eINSTANCE.createByte();
        		return newType;
    		case "char":
    			newType = (CharImpl) TypesFactory.eINSTANCE.createChar();
        		return newType;
    		case "double":
    			newType = (DoubleImpl) TypesFactory.eINSTANCE.createDouble();
        		return newType;
    		case "float":
    			newType = (FloatImpl) TypesFactory.eINSTANCE.createFloat();
        		return newType;
    		case "long":
    			newType = (LongImpl) TypesFactory.eINSTANCE.createLong();
        		return newType;
    		case "short":
    			newType = (ShortImpl) TypesFactory.eINSTANCE.createShort();
        		return newType;
    		case "void":
    			newType = (VoidImpl) TypesFactory.eINSTANCE.createVoid();
        		return newType;
    		default:
    			System.out.println("Not a primitive type!");
    	}
    	return null;
    }
}
