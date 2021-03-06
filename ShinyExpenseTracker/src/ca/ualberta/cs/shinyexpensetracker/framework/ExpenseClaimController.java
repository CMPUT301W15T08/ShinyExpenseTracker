package ca.ualberta.cs.shinyexpensetracker.framework;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import ca.ualberta.cs.shinyexpensetracker.es.data.ConnectivityChecker;
import ca.ualberta.cs.shinyexpensetracker.models.Coordinate;
import ca.ualberta.cs.shinyexpensetracker.models.Destination;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaim;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseClaimList;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseItem;
import ca.ualberta.cs.shinyexpensetracker.models.User;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseItem.Category;
import ca.ualberta.cs.shinyexpensetracker.models.ExpenseItem.Currency;
import ca.ualberta.cs.shinyexpensetracker.models.Status;
import ca.ualberta.cs.shinyexpensetracker.models.Tag;
import ca.ualberta.cs.shinyexpensetracker.persistence.ElasticSearchExpenseClaimListPersister;
import ca.ualberta.cs.shinyexpensetracker.persistence.FilePersistenceStrategy;
import ca.ualberta.cs.shinyexpensetracker.persistence.GsonExpenseClaimListPersister;
import ca.ualberta.cs.shinyexpensetracker.persistence.IExpenseClaimListPersister;

/**
 * Acts as an interface between an ExpenseClaim view and the ExpenseClaimList
 * model.
 * 
 * Use ExpenseClaimController.getInstance() to get the singleton object.
 */
public class ExpenseClaimController {
	private final IExpenseClaimListPersister persister;
	private ExpenseClaimList claimList;

	/**
	 * Default constructor.
	 * 
	 * Only use if you have a very good reason. Otherwise, just use
	 * Application.getExpenseClaimController().
	 * 
	 * @param context
	 *            The application's current context.
	 * @throws IOException
	 */
	public ExpenseClaimController(Context context) throws IOException {
		this(new ElasticSearchExpenseClaimListPersister(context, new GsonExpenseClaimListPersister(
				new FilePersistenceStrategy(context, "expenseClaims")), new ConnectivityChecker()));
	}

	/**
	 * Constructor. Use for testing only.
	 * 
	 * @param context
	 *            The application's current context.
	 * @throws IOException
	 */
	public ExpenseClaimController(IExpenseClaimListPersister persister) throws IOException {
		this.persister = persister;
		claimList = persister.loadExpenseClaims();
	}

	/**
	 * Creates a new expense claim.
	 * 
	 * @param name
	 *            The new claim's name.
	 * @param startDate
	 *            Its start date.
	 * @param endDate
	 *            Its end date.
	 * @return The new claim.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public ExpenseClaim addExpenseClaim(String name, Date startDate, Date endDate) throws IOException,
			ValidationException {
		ExpenseClaim newClaim = new ExpenseClaim(name, startDate, endDate);

		claimList.addClaim(newClaim);
		persister.saveExpenseClaims(claimList);
		return newClaim;
	}

	/**
	 * Fetches a claim from the model at the specified index.
	 * 
	 * @param index
	 *            The index.
	 * @return The claim at the given index.
	 */
	public ExpenseClaim getExpenseClaimAtPosition(int index) {
		return claimList.getClaimAtPosition(index);
	}

	/**
	 * Fetches a claim from the model based on ID.
	 * 
	 * @param id
	 *            The ID of the claim to find.
	 * @return The claim if found, and null otherwise.
	 */
	public ExpenseClaim getExpenseClaimByID(UUID id) {
		return claimList.getClaimByID(id);
	}

	/**
	 * Deletes the claim with the specified ID from the model.
	 * 
	 * @param id
	 *            The ID of the claim to delete.
	 * @throws IOException
	 */
	public void deleteExpenseClaim(UUID id) throws IOException {
		claimList.deleteClaim(id);
		persister.saveExpenseClaims(claimList);
	}

	/**
	 * Updates the expense claim with the specified ID.
	 * 
	 * @param id
	 *            The claim's ID.
	 * @param name
	 *            The new name.
	 * @param startDate
	 *            The new start date.
	 * @param endDate
	 *            The new end date.
	 * @return The updated expense claim.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public ExpenseClaim updateExpenseClaim(UUID id, String name, Date startDate, Date endDate) throws IOException,
			ValidationException {
		ExpenseClaim claim = claimList.getClaimByID(id);

		claim.setName(name);
		claim.setStartDate(startDate);
		claim.setEndDate(endDate);

		persister.saveExpenseClaims(claimList);
		return claim;
	}

	/**
	 * Updates the expense claim's status with the specified ID.
	 * 
	 * @param id
	 * @param status
	 *            The new status.
	 * @return The updated expense claim.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public ExpenseClaim updateExpenseClaimStatus(UUID id, Status status) throws IOException,
			ValidationException {
		ExpenseClaim claim = claimList.getClaimByID(id);
		claim.setStatus(status);
		persister.saveExpenseClaims(claimList);
		return claim;
	}

	/**
	 * Adds a new Destination to the claim with the specified ID.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param name
	 *            The destination's name.
	 * @param reasonForTravel
	 *            The destination's reason for travel.
	 * @return The new Destination.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public Destination addDestinationToClaim(UUID claimID, String name, String reasonForTravel, Coordinate geolocation)
			throws IOException, ValidationException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);
		Destination destination = new Destination(name, reasonForTravel, geolocation);
		claim.addDestination(destination);
		persister.saveExpenseClaims(claimList);
		return destination;
	}

	/**
	 * Updates a destination that's attached to a claim.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param destinationID
	 *            The destination's ID.
	 * @param name
	 *            The new name.
	 * @param reasonForTravel
	 *            the new reason for travel.
	 * @return The updated Destination.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public Destination updateDestinationOnClaim(UUID claimID, UUID destinationID, String name, String reasonForTravel, Coordinate geolocation)
			throws IOException, ValidationException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);
		Destination destination = claim.getDestinationByID(destinationID);

		destination.setName(name);
		destination.setReasonForTravel(reasonForTravel);
		destination.setGeolocation(geolocation);

		persister.saveExpenseClaims(claimList);
		return destination;
	}

	/**
	 * Adds a new ExpenseItem to the claim with the specified ID.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param name
	 *            The item's name.
	 * @param date
	 *            The item's date.
	 * @param category
	 *            The item's category.
	 * @param amountSpent
	 *            The item's amount spent.
	 * @param currency
	 *            The item's currency type.
	 * @param description
	 *            The item's description.
	 * @param photo
	 *            The item's receipt photo.
	 * @return The new ExpenseItem.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public ExpenseItem addExpenseItemToClaim(UUID claimID, String name, Date date, Category category, BigDecimal amountSpent, Currency currency, String description, Bitmap photo, Coordinate geolocation)
			throws IOException, ValidationException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);
		ExpenseItem item = new ExpenseItem(name, date, category, amountSpent, currency, description, photo, geolocation);

		claim.addExpenseItem(item);

		persister.saveExpenseClaims(claimList);
		return item;
	}

	/**
	 * Updates an ExpenseItem attached to the claim with the specified ID.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param expenseItemID
	 *            The expense item's ID.
	 * @param name
	 *            The new name.
	 * @param date
	 *            The new date.
	 * @param category
	 *            The new category.
	 * @param amountSpent
	 *            The new amount spent.
	 * @param currency
	 *            The new currency type.
	 * @param description
	 *            The new description.
	 * @param photo
	 *            The new receipt photo.
	 * @return The updated ExpenseItem.
	 * @throws IOException
	 * @throws ValidationException
	 */
	public ExpenseItem updateExpenseItemOnClaim(UUID claimID, UUID expenseItemID, String name, Date date, Category category, BigDecimal amountSpent, Currency currency, String description, Bitmap photo, Coordinate geolocation)
			throws IOException, ValidationException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);
		ExpenseItem item = claim.getExpenseItemByID(expenseItemID);

		item.setName(name);
		item.setDate(date);
		item.setCategory(category);
		item.setAmountSpent(amountSpent);
		item.setCurrency(currency);
		item.setDescription(description);
		item.setReceiptPhoto(photo);
		item.setGeolocation(geolocation);

		persister.saveExpenseClaims(claimList);
		return item;
	}

	/**
	 * Adds one or more tags to the claim specified by the ID.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param tags
	 *            The list of tags to add.
	 * @return The updated ExpenseClaim.
	 * @throws IOException
	 */
	public ExpenseClaim addTagsToClaim(UUID claimID, List<Tag> tags) throws IOException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);

		for (Tag tag : tags) {
			claim.addTag(tag);
		}

		persister.saveExpenseClaims(claimList);
		return claim;
	}

	/**
	 * Removes one or more tags from the claim specified by the ID.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param tags
	 *            The list of tags to remove.
	 * @return The updated ExpenseClaim.
	 * @throws IOException
	 */
	public ExpenseClaim removeTagsFromClaim(UUID claimID, List<Tag> tags) throws IOException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);

		for (Tag tag : tags) {
			claim.removeTag(tag);
		}

		persister.saveExpenseClaims(claimList);
		return claim;
	}

	public void removePhoto(UUID claimId, UUID itemId) throws IOException {
		ExpenseItem item = claimList.getClaim(claimId).getExpenseItemByID(itemId);
		item.setReceiptPhoto(null);
		persister.saveExpenseClaims(claimList);
	}

	/**
	 * Adds a new comment to a claim.
	 * 
	 * @param claimID
	 *            The claim's ID.
	 * @param comment
	 *            The new comment to add.
	 * @return The updated ExpenseClaim.
	 * @throws IOException
	 */
	public ExpenseClaim addCommentToClaim(UUID claimID, String comment) throws IOException {
		ExpenseClaim claim = claimList.getClaimByID(claimID);

		claim.addComment(comment);

		persister.saveExpenseClaims(claimList);

		return claim;
	}

	/**
	 * @return the number of claims in the claim list
	 */
	public int getCount() {
		return claimList.getCount();
	}

	/**
	 * Returns the model
	 * 
	 * @return
	 */
	public ExpenseClaimList getExpenseClaimList() {
		return claimList;
	}

	/**
	 * Returns the index of an expenseClaim
	 * 
	 * @param claim
	 * @return
	 */
	public int getIndexOf(ExpenseClaim claim) {
		return claimList.getClaims().indexOf(claim);
	}

	/**
	 * Returns and ArrayList of the expenseItems associated to a given claim
	 * 
	 * @param claim
	 * @return
	 */
	public ArrayList<ExpenseItem> getExpenseItems(ExpenseClaim claim) {
		return claim.getExpenses();
	}

	/**
	 * Removes the tag given to all claim's taglists. Used when a tag is deleted
	 * from the tag controller
	 * 
	 * @param tag The tag to remove
	 */
	public void removeTag(Tag tag) {

		for (ExpenseClaim claim : claimList.getClaims()) {
			claim.getTagList().deleteTag(tag);
		}
	}
	
	public void setColor(UUID claimID) throws IOException {
		// get home location
		ExpenseClaim claim = claimList.getClaimByID(claimID);
		User user = Application.getUser();
		Coordinate home = user.getHomeGeolocation();
		int color = 0x00000000;
		
		if (home != null && claim.getDestinationCount() != 0) {
			// get coordinate of the first destination of this claim
			Destination firstDestination = claim.getDestinationAtPosition(0);
			Coordinate firstDestinationCoordinate = firstDestination.getGeolocation();
			
			// get distance in km from first destination to home
			double distance = firstDestinationCoordinate.distanceTo(home);
			
			// 256 shades of red
			// new shade for every 80 or so kilometers of distance
			int level = (int) (Math.floor(256*distance/Coordinate.LONGEST_DISTANCE_BETWEEN_POINTS));
			
			color = 0xffffffff - level - (level<<8);
		}
		
		claim.setColor(color);
		
		persister.saveExpenseClaims(claimList);
	}
	
	public void setAllColors() {
		for (int i = 0; i < claimList.getCount(); i++) {
			UUID id = claimList.getClaimAtPosition(i).getID();
			try {
				setColor(id);
			} 
			catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
		
	}
	
	public int getColor(UUID claimID) {
		return claimList.getClaimByID(claimID).getColor();
	}
}
