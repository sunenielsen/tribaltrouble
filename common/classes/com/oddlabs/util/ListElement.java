package com.oddlabs.util;

public strictfp interface ListElement {
	public void setNext(ListElement next);
	public void setPrior(ListElement prior);
	public ListElement getNext();
	public ListElement getPrior();
	public void setListOwner(LinkedList list);
	public LinkedList getListOwner();
}
