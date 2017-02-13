/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.main;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.SharedResources;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.ResourceReference;

import de.agilecoders.wicket.markup.html.bootstrap.carousel.CarouselImage;
import de.agilecoders.wicket.markup.html.bootstrap.carousel.ICarouselImage;
import de.hpi.unicorn.application.images.ImageReference;

/**
 * This class is a {@link IModel} and provides the images for the web
 * application.
 * 
 * @author micha
 */
public class ImageModel implements IModel<List<? extends ICarouselImage>> {

	private static final long serialVersionUID = 6701834806582376422L;
	List<ICarouselImage> images = new ArrayList<ICarouselImage>();

	/**
	 * The constructor for this class, which provides the images for the web
	 * application.
	 * 
	 * @param component
	 */
	public ImageModel(final Component component) {
		final SharedResources sharedResources = WebApplication.get().getSharedResources();
		final ResourceReference aligmentImageReference = sharedResources.get(ImageReference.class, "alignment.jpg",
				null, null, null, false);
		final ResourceReference eventStreamImageReference = sharedResources.get(ImageReference.class,
				"eventStream.jpg", null, null, null, false);
		final ResourceReference groupImageReference = sharedResources.get(ImageReference.class, "group.jpg", null,
				null, null, false);
		final ResourceReference processImageReference = sharedResources.get(ImageReference.class, "process.jpg", null,
				null, null, false);

		this.images.add(new CarouselImage(component.getRequestCycle().urlFor(eventStreamImageReference, null)
				.toString(), "Event streaming platform", "Capture all your event streams in one platform"));

		this.images.add(new CarouselImage(component.getRequestCycle().urlFor(groupImageReference, null).toString(),
				"Next generation process management", "Monitor and analyze your processes"));

		this.images.add(new CarouselImage(component.getRequestCycle().urlFor(processImageReference, null).toString(),
				"Process optimization", "Capture running process instances to optimize them"));

		this.images.add(new CarouselImage(component.getRequestCycle().urlFor(aligmentImageReference, null).toString(),
				"Business and IT alignment", "Gain knowlegde through combining business and IT"));
	}

	@Override
	public void detach() {

	}

	@Override
	public List<? extends ICarouselImage> getObject() {
		return this.images;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setObject(final List<? extends ICarouselImage> images) {
		this.images = (List<ICarouselImage>) images;
	}

}
