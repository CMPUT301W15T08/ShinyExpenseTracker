package ca.ualberta.cs.shinyexpensetracker;

import java.util.ArrayList;

public class ExpenseClaimList implements IModel {
	private ArrayList<ExpenseClaim> claims;
	
	private ArrayList<IView> views; // FIXME: Not initialized
	/* FIXME
	 * UML says 0..* ExpenseClaimList's are composed of
	 * 			1 ExpenseClaimController,
	 * and that 1 ExpenseClaimList is composed of ? Expense Claims
	 */
	
	// FIXME UML says this takes no args
	public ExpenseClaim getClaim(int index) {
		return claims.get(index);
	}
	
	public void addClaim(ExpenseClaim claim) {
		claims.add(claim);
	}
	
	// FIXME What does this do?
	// Assuming it takes the argument claim.
	public void editClaim(ExpenseClaim claim) {
		return;
	}
	
	public void removeClaim(ExpenseClaim claim) {
		claims.remove(claim);
	}

	@Override
	public void addView(IView<IModel> v) {
		views.add(v);
	}

	@Override
	public void removeView(IView<IModel> v) {
		views.remove(v);
	}

	@Override
	public void notifyViews() {
		for (IView v : views) {
			v.update(this);
		}
	}
}
