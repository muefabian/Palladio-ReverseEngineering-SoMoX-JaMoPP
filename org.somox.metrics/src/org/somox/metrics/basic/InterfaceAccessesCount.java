package org.somox.metrics.basic;

import java.util.Set;

import org.apache.log4j.Logger;
import org.somox.filter.BaseFilter;
import org.somox.filter.FilteredCollectionsFactory;
import org.somox.metrics.AbstractCountingMetric;
import org.somox.metrics.ClusteringRelation;
import org.somox.metrics.MetricID;

import de.fzi.gast.types.GASTClass;

public class InterfaceAccessesCount extends AbstractCountingMetric {

	private static final Logger logger = Logger.getLogger(InterfaceAccessesCount.class);
	
	public static final MetricID METRIC_ID = new MetricID("org.somox.metrics.basic.InterfaceAccessesCount");

	private final static BaseFilter<GASTClass> interfaceClassesFilter = new BaseFilter<GASTClass>() {

		@Override
		public boolean passes(GASTClass clazz) {
			return clazz.isInterface();
		}
	};
	
	@Override
	protected ClusteringRelation internalComputeDirected (
			ClusteringRelation relationToCompute) {
		Set<GASTClass> classes1 = this.getComponentToClassHelper().deriveImplementingClasses(relationToCompute.getComponentA());
		Set<GASTClass> classes2 = this.getComponentToClassHelper().deriveImplementingClasses(relationToCompute.getComponentB());

		long accessesToInterfaces = 
			getAccessGraphCache().calculateNumberOfAccessesToClassesInSet(
					classes1,
					FilteredCollectionsFactory.getFilteredHashSet(interfaceClassesFilter, classes2));		
		if(logger.isTraceEnabled()) {
			logger.trace(relationToCompute.getComponentA() + " --> "+relationToCompute.getComponentB() + " Interface Accesses = "+accessesToInterfaces);
		}
		
		relationToCompute.setResultMetric(getMID(), (double)accessesToInterfaces);
		return relationToCompute;
	}

	@Override
	public MetricID getMID() {
		return METRIC_ID;
	}

	@Override
	public boolean isCommutative() {
		return false;
	}

}