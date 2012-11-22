package org.somox.metrics.hierarchy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.somox.metrics.AbstractMetric;
import org.somox.metrics.ClusteringRelation;

import de.fzi.gast.core.ModelElement;
import de.fzi.gast.types.GASTClass;


class TreeNode<T> {
	T element;
	List<TreeNode<T>> children;
	
	public TreeNode() {
		this.element = null;
		this.children = new LinkedList<TreeNode<T>>();
	}
	
	public TreeNode(T element) {
		if (element == null)
			throw new IllegalArgumentException("Element must not be null");
		
		this.element = element;
		this.children = new LinkedList<TreeNode<T>>();
	}
	
	public boolean isRoot() {
		return this.element == null;
	}
	
	public T getElement() {
		return element;
	}
	
	public void addChild(TreeNode<T> child) {
		this.children.add(child);
	}
	
	public List<TreeNode<T>> getChildren() {
		return Collections.unmodifiableList(this.children);
	}
	
	public int getHeight() {
		int result = 0;
		for (TreeNode<T> child : children) {
			result = Math.max(result, child.getHeight());
		}
		return result + (isRoot() ? 0 : 1);
	}
}

/**
 * Unified parent of package and directory mapping. Realizes hierarchy conformance mapping metrics.
 * Mapping metric: Checks how well component candidates are arranged in the same or at least comparable packages / directories.
 * <br>
 * Naming: PackageDirectory must be read as an entity: can be either package or directory at the instance level.
 * TODO: check efficiency
 * @author Klaus Krogmann
 *
 */
public abstract class AbstractHierarchyMapping<T extends ModelElement> extends AbstractMetric {

	/**
	 * Minimum likelihood of packages to be meaningfully considered somehow the same package
	 * TODO: make configurable 
	 */
	private static final double MINIMUM_PACKAGE_DIRECTORY_FITTING = 0.2;

	private Logger logger = Logger.getLogger(PackageMapping.class);
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isCommutative () {
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 		
	 * Idea: 
	 * 1) navigate to root for both elements
	 * 2) navigate down from root one individual of the both elements deviates
	 * 3) continue navigation to concrete element and memorize number of package steps. (optional)
	 * 4) map sum of deviating steps to result (improve weights with this)
	 *
	 */
	protected ClusteringRelation internalComputeDirected (
			ClusteringRelation relationToCompute) {
		
		Set<GASTClass> classes1 = this.getComponentToClassHelper().deriveImplementingClasses(relationToCompute.getComponentA());
		Set<GASTClass> classes2 = this.getComponentToClassHelper().deriveImplementingClasses(relationToCompute.getComponentB());
		
		TreeNode<T> element1RootPackagesPath = collectPaths(classes1);
		TreeNode<T> element2RootPackagesPath = collectPaths(classes2);
	
		int maxCommonPackageDirectoryHeigthOfElements = getMaxCommonPackageDirectory(element1RootPackagesPath, element2RootPackagesPath);		
		if(logger.isTraceEnabled()) {
			logger.trace("maxCommonPackageHeigth: " + maxCommonPackageDirectoryHeigthOfElements);
		}

		int maxHeight = Math.max(element1RootPackagesPath.getHeight(),element2RootPackagesPath.getHeight()); 

		if (maxHeight == 0) {
			relationToCompute.setResultMetric(getMID(),0.0);
		} else {
			assert maxCommonPackageDirectoryHeigthOfElements <= maxHeight;
			//normalize:
			double rawMeasure = (double)maxCommonPackageDirectoryHeigthOfElements / (double)maxHeight;
			relationToCompute.setResultMetric(getMID(), convertToNonLinearWeight(rawMeasure));
		}
		return relationToCompute;
	}
	
	/** Return the path segment in the subclasses' meaning of hierarchy where this 
	 * GAST class is directly attached to. For example, for the directory hierachy, return 
	 * the directory in which this GAST class is located
	 * @param clazz The GASTClass for which to retrieve the path segment
	 * @return The parent of the GASTClass as defined by our subclass or null if this class is not
	 * contained in the hierarchy as defined by the subclass
	 */
	protected abstract T getPath(GASTClass clazz);

	/** Return the parent path segment of the passed element T
	 * @param element The element (i.e. Directory or Package) for which to return the parent (i.e. parent package or
	 * parent directory)
	 * @return The parent if the element or null if there is no parent
	 */
	protected abstract T getPath(T element);

	/**
	 * Realizes non-linear weight
	 * @param rawMeasure
	 * @return
	 */
	private double convertToNonLinearWeight(double rawMeasure) {
		if(rawMeasure < MINIMUM_PACKAGE_DIRECTORY_FITTING) {
			return 0.0;
		} else {
			return rawMeasure;
		}
	}

	/**
	 * Calculates the maximum common hierarchy levels of the trees given (starting from root)
	 * For example, for a sets {a.b.c, a.b.d.e} and {a.b.g,a.b.h.z} the maximum common path is
	 * a.b and hence the result would be 2. 	
	 * @param tree1 A prefix tree-based representation of all elements of the first set of paths
	 * @param tree2 A prefix tree-based representation of all elements of the second set of paths 
	 * @return The maximum levels of the hierarchy which both trees have in common
	 */
	private int getMaxCommonPackageDirectory(
			TreeNode<T> tree1,
			TreeNode<T> tree2) {
		// Check method precondition
		if ((tree1.isRoot() && !tree2.isRoot()) || (!tree1.isRoot() && tree2.isRoot())) {
			throw new IllegalArgumentException("Both tree nodes have to be root or non-root");
		}
		
		// If both tree nodes are root nodes, make a step down the tree in both trees
		// but only if there are elements in both trees
		if (tree1.isRoot() && tree2.isRoot()) {
			if (tree1.getChildren().size() == 0 || tree2.getChildren().size() == 0)
				return 0;
			else 
				return getMaxCommonPackageDirectory(tree1.getChildren().get(0), tree2.getChildren().get(0));
		}

		// If there is a fork in one of the tree nodes, we consider them not to be the same
		// as the route in the source tree becomes ambiguous. This might be subject to change for
		// other comparison strategies
		if (tree1.getChildren().size() > 1 || tree2.getChildren().size() > 1)
			return 0;
		
		// If we reached a level where the tree node elements are different, we stop
		// descending the tree and return 0 for this level.
		if (tree1.getElement() != tree2.getElement())
			return 0;
		
		// Stop descending if one of the tree nodes is already a leaf node
		if (tree1.getChildren().size() == 0 || tree2.getChildren().size() == 0)
			return 1;
		
		// Return our own level plus the level of similar path segments down the tree
		return 1 + getMaxCommonPackageDirectory(tree1.getChildren().get(0), tree2.getChildren().get(0));
	}
	
	/**
	 * Given a set of paths, we add all of them into a single prefix tree. This allows efficient
	 * computation of the amount of similarity later in the algorithm. If we first add a.b.c to the 
	 * tree we get a->b->c. If we then add a.b.c.d we get a->b->c->d. If we then add a.b.g we get
	 * a->b->{c->d | g}
	 * @param elements The set of GAST classes for which we build the prefix tree of their hierarchy
	 * @return The constructed tree
	 */
	private TreeNode<T> collectPaths(Set<GASTClass> elements) {
		TreeNode<T> root = new TreeNode<T>(); // create a new tree root
		for(GASTClass currentClass : elements) {
			addToTree(root, getPath(currentClass));
		}
		return root;
	}
	
	/**
	 * Add the given element to the given tree. The method first recursively moves up in the list of
	 * path segments up until it reaches the root of the path. Then it moves down the tree and for
	 * each path element it adds it to the tree. If the node where the path segment should be added is
	 * not yet existing it will be added to the tree.
	 * @param root The tree to which we add the given elements path in the hierarchy
	 * @param element The element for which we generate its path through the hierarchy which we then add
	 * to the tree
	 * @return The last tree node in which we added the previous part of the element's path in the hierarchy
	 */
	private TreeNode<T> addToTree(TreeNode<T> root, T element) {
		if (element != null) {
			TreeNode<T> parent = addToTree(root, getPath(element));
			for (TreeNode<T> child : parent.getChildren()) {
				if (child.getElement() == element) {
					// We found the node matching the part of the path we need
					// nothing needs to be done for this, simply return the node we found
					return child;
				}
			}
			TreeNode<T> newChild = new TreeNode<T>(element);
			parent.addChild(newChild);
			return newChild;
		} else {
			return root;
		}
	}

	/* (non-Javadoc)
	 * @see org.somox.metrics.IMetric#isNormalised()
	 */
	@Override
	public boolean isNormalised() {
		return true;
	}
	
	
}