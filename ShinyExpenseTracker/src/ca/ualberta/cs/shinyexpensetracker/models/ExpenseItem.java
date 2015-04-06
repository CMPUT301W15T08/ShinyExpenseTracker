/** Covers Issue 5
 * Things to implement: saving of an expenseItem to an expenseClaim
 * @author Sarah Morris
 * @version 1.0
 * @since 2015-03-09
 *
 * ExpenseItem hold the name, date, category, amount spent, currency, description
 * and a photo of a receipt for an expense that is added to a claim.
 * 
 * To use:
 * 	ExpenseItem e = new ExpenseItem(String name, Date date, Category category, BigDecimal amountSpent, 
 * 							Currency currency, String description, Bitmap photo);
 */

package ca.ualberta.cs.shinyexpensetracker.models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.graphics.Bitmap;
import ca.ualberta.cs.shinyexpensetracker.utilities.Base64BitmapConverter;

/**
 * An ExpenseItem represents the details of an expense that's a part of an
 * expense claim.
 */
public class ExpenseItem extends Model<ExpenseItem> {
	public enum Category {
		AIR_FARE("air fare"), GROUND_TRANSPORT("ground transport"), VEHICLE_RENTAL("vehicle rental"), PRIVATE_AUTOMOBILE(
				"private automobile"), FUEL("fuel"), PARKING("parking"), REGISTRATION("registration"), ACCOMODATION(
				"accomodation"), MEAL("meal"), SUPPLIES("supplies");

		private final String text;

		private Category(final String text) {
			this.text = text;
		}

		public String toString() {
			return this.text;
		}

		public static Category fromString(String text) {
			if (text != null) {
				for (Category c : Category.values()) {
					if (text.equalsIgnoreCase(c.text)) {
						return c;
					}
				}
			}
			return null;
		}
	}

	public enum Currency {
		CAD, USD, GBP, EUR, CHF, JPY, CNY
	}

	private UUID id;
	private String name;
	private Date date;
	private Category category;
	private BigDecimal amountSpent;
	private Currency currency;
	private String description;
	private boolean incompletenessMarker;

	// encoded in Base64
	private String encodedReceiptPhoto;
	// temporarily saved to prevent redundant work, but can't be GSON'd
	private transient Bitmap receiptPhotoBitmap;

	public static final boolean COMPLETE = false;
	public static final boolean INCOMPLETE = true;

	/**
	 * Generalized constructor.
	 * 
	 * @param name
	 * @param date
	 * @param category
	 * @param amountSpent
	 * @param currency
	 * @param description
	 * @param photo
	 * @param completenessFlag
	 */
	public ExpenseItem(String name, Date date, Category category, BigDecimal amountSpent, Currency currency,
			String description, Bitmap photo, boolean completenessFlag) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.date = date;
		this.category = category;
		this.amountSpent = amountSpent;
		this.currency = currency;
		this.description = description;
		this.setReceiptPhoto(photo);
		this.incompletenessMarker = completenessFlag;
	}

	public ExpenseItem(String name, Date date, Category category, BigDecimal amountSpent, Currency currency,
			String description, Bitmap photo) {
		// Call to more specific constructor
		this(name, date, category, amountSpent, currency, description, photo, false);
	}

	public ExpenseItem(String name, Date date, Category category, BigDecimal amountSpent, Currency currency,
			String description) {
		// Call to more specific constructor
		this(name, date, category, amountSpent, currency, description, null, false);
	}

	public ExpenseItem(String name, Date date, Category category, BigDecimal amount, Currency currency) {
		// Call to more specific constructor
		this(name, date, category, amount, currency, "", null, false);
	}

	public UUID getID() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setAmountSpent(BigDecimal amountSpent) {
		this.amountSpent = amountSpent;
	}

	public BigDecimal getAmountSpent() {
		return this.amountSpent;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Currency getCurrency() {
		return this.currency;
	}

	public String getEncodedReceiptPhoto() {
		return this.encodedReceiptPhoto;
	}

	public Bitmap getReceiptPhoto() {
		if (this.receiptPhotoBitmap == null) {
			if (this.encodedReceiptPhoto == null) {
				return null;
			} else {
				this.receiptPhotoBitmap = Base64BitmapConverter.convertFromBase64(this.encodedReceiptPhoto);
			}
		}

		return this.receiptPhotoBitmap;
	}

	public void setReceiptPhoto(Bitmap photo) {
		if (photo == null) {
			this.receiptPhotoBitmap = null;
			this.encodedReceiptPhoto = null;
		} else {
			this.receiptPhotoBitmap = photo;
			this.encodedReceiptPhoto = Base64BitmapConverter.convertToBase64(photo);
        }

		notifyViews();
	}

	// XXX: #69 <- This should return the formatted JodaMoney string
	public String getValueString() {
		return new StringBuilder().append(getAmountSpent()).append(" ").append(getCurrency().toString()).toString();
	}

	/**
	 * Returns true if the ExpenseItem has an attached receipt photo.
	 * 
	 * @return true if an attached receipt photo exists, false otherwise.
	 */
	public boolean hasAnAttachedReceipt() {
		return this.encodedReceiptPhoto != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ExpenseItem rhs = (ExpenseItem) obj;
		return new EqualsBuilder().append(getName(), rhs.getName()).append(getDate(), rhs.getDate())
				.append(getCategory(), rhs.getCategory()).append(getAmountSpent(), rhs.getAmountSpent())
				.append(getCurrency(), rhs.getCurrency()).append(getDescription(), rhs.getDescription())
				.append(getEncodedReceiptPhoto(), rhs.getEncodedReceiptPhoto()).isEquals();
	}

	// Source:
	// http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/builder/ToStringBuilder.html
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Sets the incompleteness markers. You should use ExpenseItem.COMPLETE or
	 * ExpenseItem.INCOMPLETE for clarity.
	 * 
	 * @param isIncomplete
	 *            boolean value representing the completeness of the claim. This
	 *            is marked manually by the user through the UI.
	 */
	public void setIncompletenessMarker(boolean isIncomplete) {
		incompletenessMarker = isIncomplete;
		notifyViews();
	}

	/**
	 * @return true if the expense is marked incomplete, false otherwise.
	 */
	public boolean getIsMarkedIncomplete() {
		return incompletenessMarker;
	}

	/**
	 * Toggles the incompleteness marker
	 */
	public void toggleIncompletenessMarker() {
		if (getIsMarkedIncomplete()) {
			setIncompletenessMarker(COMPLETE);
		} else {
			setIncompletenessMarker(INCOMPLETE);
		}
	}

}
