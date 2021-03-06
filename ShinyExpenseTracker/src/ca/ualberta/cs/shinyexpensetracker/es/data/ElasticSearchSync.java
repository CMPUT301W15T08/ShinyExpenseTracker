package ca.ualberta.cs.shinyexpensetracker.es.data;

import ca.ualberta.cs.shinyexpensetracker.framework.ValidationException;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimList;
import ca.ualberta.cs.shinyexpensetracker.models.Status;

/**
 * This class will sync any changes the user made with any changes the in the
 * server
 * 
 */
public class ElasticSearchSync {
	ExpenseClaimList localList;
	ExpenseClaimList cloudList;

	public ElasticSearchSync(ExpenseClaimList localList, ExpenseClaimList cloudList) {
		this.cloudList = cloudList;
		this.localList = localList;
	}

	/**
	 * sync two lists. The local list will only change data that the approval
	 * has changed
	 * 
	 * @return The synced expense claim list
	 */
	public ExpenseClaimList sync() {
		for (ExpenseClaim claim : cloudList.getClaims()) {
			ExpenseClaim localClaim = localList.getClaim(claim.getID());
			if (localClaim != null) {
				localClaim.setComments(claim.getComments());

				if (localClaim.getStatus() == Status.SUBMITTED) {
					try {
						localClaim.setStatus(claim.getStatus());
					} catch (ValidationException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		return localList;
	}
}
