/* 
 * Copyright 2017 Fondazione Istituto Italiano di Tecnologia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.simsearch.bundle;

import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.services.XServiceRegistrar;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import it.iit.genomics.cru.simsearch.bundle.utils.ServiceManager;

/**
 * @author Arnaud Ceol
 */
public class Activator  extends XServiceRegistrar<IgbService> implements BundleActivator  {

	public Activator() {
        super(IgbService.class);	
    }

	@Override
	protected ServiceRegistration<?>[] getServices(BundleContext bundleContext, IgbService igbService)
			throws Exception {

		ServiceManager.getInstance().setService(igbService);

		return new ServiceRegistration[] {
		};
	}

	@Override
	public void start(BundleContext _bundleContext) throws Exception {
		super.start(_bundleContext);	
	}
	
}
