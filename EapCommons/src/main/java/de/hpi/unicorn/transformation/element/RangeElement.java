/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Range element for filter expressions.
 */
@Entity
@Table(name = "RangeElement")
public class RangeElement implements Serializable {

	private static final long serialVersionUID = 8502951013262132211L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "RangeElement_ID")
	private final int ID;

	@Column(name = "LeftEndpoint")
	private int leftEndpoint;

	@Column(name = "LeftEndpointOpen")
	private boolean leftEndpointOpen;

	@Column(name = "RightEndpoint")
	private int rightEndpoint;

	@Column(name = "RightEndpointOpen")
	private boolean rightEndpointOpen;

	public RangeElement() {
		this.ID = 0;
		this.leftEndpoint = 0;
		this.leftEndpointOpen = false;
		this.rightEndpoint = 1;
		this.rightEndpointOpen = false;
	}

	/**
	 * Constructor.
	 * 
	 * @param leftEndpoint
	 *            left endpoint of a range
	 * @param leftEndpointOpen
	 *            true if left endpoint is open
	 * @param rightEndpoint
	 *            right endpoint of a range
	 * @param rightEndpointOpen
	 *            true if right endpoint is open
	 */
	public RangeElement(final int leftEndpoint, final boolean leftEndpointOpen, final int rightEndpoint,
			final boolean rightEndpointOpen) {
		this();
		this.leftEndpoint = leftEndpoint;
		this.leftEndpointOpen = leftEndpointOpen;
		this.rightEndpoint = rightEndpoint;
		this.rightEndpointOpen = rightEndpointOpen;
	}

	public int getLeftEndpoint() {
		return this.leftEndpoint;
	}

	public void setLeftEndpoint(final int leftEndpoint) {
		this.leftEndpoint = leftEndpoint;
	}

	public boolean isLeftEndpointOpen() {
		return this.leftEndpointOpen;
	}

	public void setLeftEndpointOpen(final boolean leftEndpointOpen) {
		this.leftEndpointOpen = leftEndpointOpen;
	}

	public int getRightEndpoint() {
		return this.rightEndpoint;
	}

	public void setRightEndpoint(final int rightEndpoint) {
		this.rightEndpoint = rightEndpoint;
	}

	public boolean isRightEndpointOpen() {
		return this.rightEndpointOpen;
	}

	public void setRightEndpointOpen(final boolean rightEndpointOpen) {
		this.rightEndpointOpen = rightEndpointOpen;
	}
}
