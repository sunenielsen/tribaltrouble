package com.oddlabs.tt.guievent;

public strictfp interface RowListener extends EventListener {
	public void rowDoubleClicked(Object row_context);
	public void rowChosen(Object row_context);
}
