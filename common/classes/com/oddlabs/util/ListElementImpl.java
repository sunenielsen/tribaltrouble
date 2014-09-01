package com.oddlabs.util;

public abstract strictfp class ListElementImpl implements ListElement {
	private LinkedList parent;

	private ListElement next = null;
	private ListElement prior = null;

	public final void setListOwner(LinkedList owner) {
		parent = owner;
	}

	public final LinkedList getListOwner() {
		return parent;
	}

	public final void setPrior(ListElement prior) {
		this.prior = prior;
	}

	public final void setNext(ListElement next) {
		this.next = next;
	}

	public final ListElement getPrior() {
		return prior;
	}

	public final ListElement getNext() {
		return next;
	}
}
