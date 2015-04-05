package ca.ualberta.cs.shinyexpensetracker.decorators;

import ca.ualberta.cs.shinyexpensetracker.framework.IView;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimList;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimListViewer;

/**
 * This class is the superclass for all expense claim list
 * filters and decorators. 
 * 
 * Subclasses must implement the getters for claims and count
 * of claims. Subclasses have access to the source object which
 * they can use to get the data from the thing they're decorating.
 */
public abstract class ExpenseClaimFilter extends ExpenseClaimList
		implements IView<ExpenseClaimList>, ExpenseClaimListViewer {
	protected ExpenseClaimList source = null;
	
	public final ExpenseClaimList decorate(ExpenseClaimList source) {
		if (this.source != null) {
			throw new RuntimeException("Cannot replace the decorator source");
		}
		this.source = source;
		source.addView(this);
		return this;
	}

	@Override
	public void addClaim(ExpenseClaim claim) {
		source.addClaim(claim);
		onDatasetChanged();
	}

	@Override
	public void removeClaim(ExpenseClaim claim) {
		source.removeClaim(claim);
		onDatasetChanged();
	}

	@Override
	public void updateExpenseClaim(ExpenseClaim oldClaim, ExpenseClaim newClaim) {
		source.updateExpenseClaim(oldClaim, newClaim);
		onDatasetChanged();
	}
	
	@Override
	public void update(ExpenseClaimList m) {
		onDatasetChanged();
	}
	
	/**
	 * Allows subclasses to be notified that the dataset has
	 * been changed and that they need to be updated.
	 * 
	 * It should be overridden if you need to know when the
	 * source object has changed.
	 */
	protected void onDatasetChanged() {}
}