package com.example.expensetrackerapp.Managers;

import android.util.Log;

import com.example.expensetrackerapp.Exceptions.CategoryAlreadyExistsException;
import com.example.expensetrackerapp.Exceptions.CategoryNotFoundException;
import com.example.expensetrackerapp.Exceptions.ExpenseWithCategoryExistsException;
import com.example.expensetrackerapp.Models.Category;
import com.example.expensetrackerapp.Models.Expense;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import java.util.*;

public final class FirestoreManager {

    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String EXPENSES_SUBCOLLECTION = "expenses";
    private static final String CATEGORIES_SUBCOLLECTION = "categories";

    // Field names
    private static final String AMOUNT_FIELD = "amount";
    private static final String CATEGORY_ID_FIELD = "categoryId";
    private static final String TRANSACTION_DATE_FIELD = "transactionDate";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String ID_FIELD = "id";
    private static final String NAME_FIELD = "name";
    private static final String ICON_INDEX = "imgIndexInsideArraysXml";

    // Private constructor to prevent instantiation
    private FirestoreManager() {}

    // Firestore Callback Interfaces
    public interface FirestoreListCallback<T> {
        void onComplete(List<T> items);
        void onFailure(Exception e);
    }

    public interface FirestoreIdCallback {
        void onComplete(String id);
        void onFailure(Exception e);
    }

    // ===================== Expenses =====================

    /**
     * Adds an expense to the Firestore database under the specified user's email and category ID.
     * Updates the expense object and the Firestore document with the generated document ID.
     *
     * @param userEmail the email of the user to whom the expense belongs
     * @param expense the expense object to be added
     * @param callback the callback to handle success or failure of the operation
     */
    public static void addExpense(String userEmail, Expense expense, FirestoreIdCallback callback) {
        String categoryId = expense.getCategory().getId();
        Log.d("FirestoreManager", "Adding expense for user: " + userEmail + " in category: " + categoryId);

        // Create a map to hold the expense data to be stored in Firestore
        Map<String, Object> data = new HashMap<>();
        data.put(AMOUNT_FIELD,expense.getAmount());
        data.put(TRANSACTION_DATE_FIELD,expense.getTransactionDate());
        data.put(DESCRIPTION_FIELD,expense.getDescription());
        data.put(CATEGORY_ID_FIELD, categoryId);

        // add the data to the collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION)
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreManager", "Expense added successfully with document ID: " + documentReference.getId());
                    // Set the document ID in the expense object
                    String id = documentReference.getId();
                    expense.setId(id);

                    // Update the document with the ID
                    documentReference.update(ID_FIELD, id)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) callback.onComplete(id);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreManager", "Failed to update expense document with ID: " + e.getMessage(), e);
                                if (callback != null) callback.onFailure(e);
                            });
                })

                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Removes a specified expense record from the Firestore database.
     *
     * @param userEmail The email of the user under which the expense is stored.
     * @param expenseId The ID of the expense record to remove.
     * @param callback A FirestoreIdCallback to handle success or failure.
     *                 - On success, the expenseId will be passed via onComplete.
     *                 - On failure, the error will be passed via onFailure.
     * Functionality:
     * - Deletes a document with the specified expenseId from the "expenses" subcollection under the user's document.
     * - Reports the result of the deletion operation via the provided callback.
     */
    public static void removeExpense(String userEmail, String expenseId, FirestoreIdCallback callback) {
        Log.d("FirestoreManager", "Removing expense with ID: " + expenseId + " for user: " + userEmail);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION)
                .document(expenseId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreManager", "Expense removed successfully with ID: " + expenseId);


                    if (callback != null) callback.onComplete(expenseId);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Modifies an existing expense record in the Firestore database.
     *
     * @param userEmail        The email address of the user to whom the expense belongs.
     * @param expenseId        The ID of the expense record to modify.
     * @param modifiedExpense  The Expense object containing the updated details.
     *                         Only the category name is stored, not the entire category object.
     * @param callback         A FirestoreIdCallback to handle the success or failure of the operation.
     *                         On success, the ID of the updated document is returned via onComplete.
     *                         On failure, the exception is passed via onFailure.
     * Functionality:
     * - Updates the data of an existing document with the specified expenseId in the "expenses" subcollection.
     * - Uses a map with the fields of Expense but with only the name of the category (category id) instead of the whole object
     * - Notifies the provided callback about the result of the operation.
     */
    public static void editExpense(String userEmail, String expenseId, Expense modifiedExpense, FirestoreIdCallback callback) {
        Log.d("editExpense", "Editing expense with ID: " + expenseId + " for user: " + userEmail);
        Map<String, Object> data = new HashMap<>();
        data.put(AMOUNT_FIELD,modifiedExpense.getAmount());
        data.put(CATEGORY_ID_FIELD,modifiedExpense.getCategory().getId());
        data.put(TRANSACTION_DATE_FIELD,modifiedExpense.getTransactionDate());
        data.put(DESCRIPTION_FIELD,modifiedExpense.getDescription());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION)
                .document(expenseId).update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("editExpense", "Expense edited successfully with ID: " + expenseId);
                    if (callback != null) callback.onComplete(expenseId);
                })
                .addOnFailureListener(e -> {
                    Log.e("editExpense", "Failed to edit expense: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e);
                });

    }


    /**
     * Fetches all expenses for a specific user, covering all categories and all transaction dates.
     * Combines the use of `getCategories`, `getTransactionDateRange`, and `getExpenses` methods.
     *
     * @param userEmail the email of the user to fetch all expenses for
     * @param callback the callback to handle the list of all expenses or an error
     */
    public static void getExpenses(String userEmail, FirestoreListCallback<Expense> callback) {
        // Step 1: Get all categories for the user
        getCategories(userEmail, new FirestoreListCallback<>() {
            @Override
            public void onComplete(List<Category> categories) {
                Log.d("GetAllExpenses", "Fetched " + categories.size() + " categories.");

                // Extract category IDs from the categories
                List<String> categoryIds = new ArrayList<>();
                for (Category category : categories) {
                    categoryIds.add(category.getId());
                }

                // Step 2: Get the transaction date range (oldest and newest dates)
                getTransactionDateRange(userEmail, new FirestoreListCallback<>() {
                    @Override
                    public void onComplete(List<Date> dateRange) {
                        if (dateRange.isEmpty() || dateRange.size() < 2) {
                            Log.w("GetAllExpenses", "No valid date range found.");
                            if (callback != null) callback.onComplete(new ArrayList<>()); // Return an empty list
                            return;
                        }

                        Date startingDate = dateRange.get(0); // Oldest transaction date
                        Date endingDate = dateRange.get(1); // Newest transaction date

                        Log.d("GetAllExpenses", "Transaction date range: From " + startingDate + " to " + endingDate);

                        // Step 3: Get all expenses for the user within the date range and categories
                        getExpenses(userEmail, categoryIds, startingDate, endingDate, new FirestoreListCallback<>() {
                            @Override
                            public void onComplete(List<Expense> expenses) {
                                Log.d("GetAllExpenses", "Fetched " + expenses.size() + " expenses.");
                                if (callback != null) callback.onComplete(expenses); // Return the complete list of expenses
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("GetAllExpenses", "Failed to fetch expenses: " + e.getMessage(), e);
                                if (callback != null) callback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("GetAllExpenses", "Failed to fetch transaction date range: " + e.getMessage(), e);
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("GetAllExpenses", "Failed to fetch categories: " + e.getMessage(), e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Fetches all expenses for a specific user filtered by a list of category IDs.
     * Combines the use of `getTransactionDateRange` and `getExpenses` for efficient processing.
     *
     * @param userEmail the email of the user to fetch expenses for
     * @param categoryIds the list of category IDs to filter expenses
     * @param callback the callback to handle the list of expenses or an error
     */
    public static void getExpenses(String userEmail, List<String> categoryIds, FirestoreListCallback<Expense> callback) {
        // Step 1: Get the transaction date range (oldest and newest dates)
        getTransactionDateRange(userEmail, new FirestoreListCallback<>() {
            @Override
            public void onComplete(List<Date> dateRange) {
                if (dateRange.isEmpty() || dateRange.size() < 2) {
                    Log.w("GetExpenses", "No valid date range found.");
                    if (callback != null) callback.onComplete(new ArrayList<>()); // Return an empty list
                    return;
                }

                Date startingDate = dateRange.get(0); // Oldest transaction date
                Date endingDate = dateRange.get(1); // Newest transaction date

                Log.d("GetExpenses", "Transaction date range: From " + startingDate + " to " + endingDate);

                // Step 2: Fetch all expenses filtered by the provided category IDs within the date range
                getExpenses(userEmail, categoryIds, startingDate, endingDate, new FirestoreListCallback<>() {
                    @Override
                    public void onComplete(List<Expense> expenses) {
                        Log.d("GetExpenses", "Fetched " + expenses.size() + " expenses.");
                        if (callback != null) callback.onComplete(expenses); // Return the complete list of expenses
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("GetExpenses", "Failed to fetch expenses: " + e.getMessage(), e);
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("GetExpenses", "Failed to fetch transaction date range: " + e.getMessage(), e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }


    /**
     * Fetches all expenses for a specific user within a specified date range, covering all categories.
     * Combines the use of `getCategories`, `getExpenses`, and uses the provided date range.
     *
     * @param userEmail the email of the user to fetch expenses for
     * @param startingDate the starting date for filtering expenses
     * @param endingDate the ending date for filtering expenses
     * @param callback the callback to handle the list of expenses or an error
     */
    public static void getExpenses(String userEmail, Date startingDate, Date endingDate, FirestoreListCallback<Expense> callback) {
        // Step 1: Get all categories for the user
        getCategories(userEmail, new FirestoreListCallback<>() {
            @Override
            public void onComplete(List<Category> categories) {
                Log.d("GetExpenses", "Fetched " + categories.size() + " categories.");

                // Extract category IDs from the categories
                List<String> categoryIds = new ArrayList<>();
                for (Category category : categories) {
                    categoryIds.add(category.getId());
                }

                // Step 2: Get all expenses for the user within the date range and categories
                getExpenses(userEmail, categoryIds, startingDate, endingDate, new FirestoreListCallback<>() {
                    @Override
                    public void onComplete(List<Expense> expenses) {
                        Log.d("GetExpenses", "Fetched " + expenses.size() + " expenses.");
                        if (callback != null) callback.onComplete(expenses); // Return the complete list of expenses
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("GetExpenses", "Failed to fetch expenses: " + e.getMessage(), e);
                        if (callback != null) callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("GetExpenses", "Failed to fetch categories: " + e.getMessage(), e);
                if (callback != null) callback.onFailure(e);
            }
        });
    }

    /**
     * Fetches a list of expenses for a specific user, filtered by a list of category IDs and a date range.
     * Ensures the returned list is ordered by transaction date in descending order.
     * The returned expenses contain the ids of firestore.
     *
     * @param userEmail the email of the user to whom the expenses belong
     * @param categoryIds a list of category IDs to filter the expenses
     * @param startingDate the starting date for filtering expenses
     * @param endingDate the ending date for filtering expenses
     * @param callback the callback to handle the list of expenses or an error
     */
    public static void getExpenses(String userEmail, List<String> categoryIds, Date startingDate, Date endingDate, FirestoreListCallback<Expense> callback) {
        // Log the beginning of the method
        Log.d("GetExpenses", "Fetching expenses for user: " + userEmail + ", categories: " + categoryIds + ", from: " + startingDate + ", to: " + endingDate);

        // Query Firestore to get all expense documents for the specified categories and date range
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION)
                .whereIn(CATEGORY_ID_FIELD, categoryIds) // Filter by category IDs
                .whereGreaterThanOrEqualTo("transactionDate", startingDate) // Filter by starting date
                .whereLessThanOrEqualTo("transactionDate", endingDate) // Filter by ending date
                .orderBy("transactionDate", Query.Direction.DESCENDING) // Order by date descending
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("GetExpenses", "Fetched " + querySnapshot.size() + " expense documents.");
                    List<DocumentSnapshot> expenseDocuments = querySnapshot.getDocuments();

                    // Use the getCategories method to fetch corresponding categories
                    getCategories(userEmail, expenseDocuments, new FirestoreListCallback<
                            >() {
                        @Override
                        public void onComplete(List<Category> categories) {
                            Log.d("GetExpenses", "Fetched " + categories.size() + " categories.");

                            // Create a list of expenses based on the fetched categories
                            List<Expense> expenses = new ArrayList<>();

                            for (int i = 0; i < expenseDocuments.size(); i++) {
                                DocumentSnapshot expenseDoc = expenseDocuments.get(i);
                                Category category = categories.get(i);

                                Map<String, Object> data = expenseDoc.getData();
                                if (data == null) {
                                    Log.w("GetExpenses", "Expense document at index " + i + " has null data.");
                                    continue;
                                }

                                //Get data
                                Double amount = (Double) data.get(AMOUNT_FIELD);
                                Timestamp timestampTransaction = (Timestamp) data.get(TRANSACTION_DATE_FIELD);
                                if (amount == null || timestampTransaction == null) {
                                    Log.d("GetExpenses", "AMOUNT_FIELD OR TRANSACTION_DATE_FIELD is null");
                                    return;
                                }

                                // Construct the Expense object
                                Expense expense = new Expense();
                                expense.setId((String) data.get("id"));
                                expense.setAmount(amount);
                                expense.setTransactionDate(timestampTransaction.toDate());
                                expense.setDescription((String) data.get(DESCRIPTION_FIELD));
                                expense.setCategory(category);

                                expenses.add(expense);
                                Log.d("GetExpenses", "Created expense: " + expense.getId());
                            }

                            // Pass the list of expenses to the callback
                            if (callback != null) {
                                Log.d("GetExpenses", "Returning " + expenses.size() + " expenses to the callback.");
                                callback.onComplete(expenses);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Handle failure in fetching categories
                            Log.e("GetExpenses", "Failed to fetch categories: " + e.getMessage(), e);
                            if (callback != null) callback.onFailure(e);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle failure in fetching expense documents
                    Log.e("GetExpenses", "Failed to fetch expense documents: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Retrieves the date of the first and last expense transactions for a specific user.
     * The returned list contains two dates: the first (oldest) transaction date and the last (newest) transaction date.
     *
     * @param userEmail the email of the user to fetch the transaction dates for
     * @param callback the callback to handle the list containing the first and last dates or an error
     */
    private static void getTransactionDateRange(String userEmail, FirestoreListCallback<Date> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION)
                .orderBy("transactionDate", Query.Direction.ASCENDING) // Order by date ascending for the oldest transaction
                .limit(1)
                .get()
                .addOnSuccessListener(firstQuerySnapshot -> {
                    if (firstQuerySnapshot.isEmpty()) {
                        Log.w("GetTransactionDateRange", "No expenses found for user: " + userEmail);
                        if (callback != null) callback.onComplete(new ArrayList<>()); // Return an empty list
                        return;
                    }

                    // Extract the oldest transaction date
                    Timestamp oldestTimestamp = firstQuerySnapshot.getDocuments().get(0).getTimestamp("transactionDate");
                    if (oldestTimestamp == null) {
                        Log.e("GetTransactionDateRange", "Oldest transaction date is null.");
                        if (callback != null) callback.onFailure(new NullPointerException("Oldest transaction date is null."));
                        return;
                    }

                    db.collection(USERS_COLLECTION)
                            .document(userEmail)
                            .collection(EXPENSES_SUBCOLLECTION)
                            .orderBy("transactionDate", Query.Direction.DESCENDING) // Order by date descending for the newest transaction
                            .limit(1)
                            .get()
                            .addOnSuccessListener(lastQuerySnapshot -> {
                                if (lastQuerySnapshot.isEmpty()) {
                                    Log.w("GetTransactionDateRange", "No expenses found for user: " + userEmail);
                                    if (callback != null) callback.onComplete(new ArrayList<>()); // Return an empty list
                                    return;
                                }

                                // Extract the newest transaction date
                                Timestamp newestTimestamp = lastQuerySnapshot.getDocuments().get(0).getTimestamp("transactionDate");
                                if (newestTimestamp == null) {
                                    Log.e("GetTransactionDateRange", "Newest transaction date is null.");
                                    if (callback != null) callback.onFailure(new NullPointerException("Newest transaction date is null."));
                                    return;
                                }

                                // Return the oldest and newest dates in a list
                                List<Date> dateRange = new ArrayList<>();
                                dateRange.add(oldestTimestamp.toDate());
                                dateRange.add(newestTimestamp.toDate());

                                Log.d("GetTransactionDateRange", "Oldest: " + dateRange.get(0) + ", Newest: " + dateRange.get(1));
                                if (callback != null) callback.onComplete(dateRange);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("GetTransactionDateRange", "Failed to fetch newest transaction date: " + e.getMessage(), e);
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("GetTransactionDateRange", "Failed to fetch oldest transaction date: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    public static void deleteAllExpenses(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference expensesCollectionRef = db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION);

        expensesCollectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                batch.delete(document.getReference());
            }

            batch.commit().addOnSuccessListener(aVoid ->
                    System.out.println("All expenses deleted successfully")
            ).addOnFailureListener(e ->
                    System.err.println("Error deleting all expenses: " + e.getMessage())
            );
        }).addOnFailureListener(e ->
                System.err.println("Error fetching expenses: " + e.getMessage())
        );
    }


    // ===================== Categories =====================

    /**
     * Adds multiple categories to the "categories" subcollection under the specified user's document in Firestore.
     * Steps:
     * 1. For each category in the list:
     *    - Checks if a category with the same name already exists in the user's "categories" subcollection.
     *    - If no duplicate exists, adds the category with an auto-generated Firestore document ID.
     *    - Updates the category object and the Firestore document with the assigned ID.
     * 2. Tracks all operations using Tasks and only calls onComplete when all categories have been processed.
     * 3. Provides a list of successfully added category IDs through the callback.
     *
     * @param userEmail The email of the user under whose document the categories will be added.
     * @param categories The list of Category objects to be added, each must have a name field set.
     * @param callback The FirestoreListCallback to handle success or failure events. On success, it provides the list of successfully added category IDs.
     */
    public static void addCategories(String userEmail, List<Category> categories, FirestoreListCallback<String> callback) {
        Log.d("addCategories", "Adding " + categories.size() + " categories for user: " + userEmail);

        // Reference to the categories subcollection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoriesRef = db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(CATEGORIES_SUBCOLLECTION);

        // List to store tasks for all category additions
        List<Task<String>> allTasks = new ArrayList<>();

        // Process each category
        for (Category category : categories) {
            // Create a task source that will be completed when this category is fully processed
            TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
            allTasks.add(taskCompletionSource.getTask());

            Log.d("addCategories", "Processing category: " + category.getName());

            // Query to check if a category with the same name already exists
            categoriesRef.whereEqualTo("name", category.getName())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // This category name already exists - can't add it
                            Log.w("addCategories", "Category already exists: " + category.getName());
                            taskCompletionSource.setException(new CategoryAlreadyExistsException(category.getName()));
                        } else {
                            // Category doesn't exist - add with auto-generated ID
                            categoriesRef.add(category)
                                    .addOnSuccessListener(documentReference -> {
                                        // Set the Firestore-assigned ID to the category object
                                        String generatedId = documentReference.getId();
                                        category.setId(generatedId);
                                        Log.d("addCategories", "Added category: " + category.getName() + " with ID: " + generatedId);

                                        // Update the document with the assigned ID
                                        documentReference.update("id", generatedId)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("addCategories", "Updated category document with ID: " + generatedId);
                                                    taskCompletionSource.setResult(generatedId); // Successfully completed with ID
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("addCategories", "Failed to update category with ID: " + e.getMessage(), e);
                                                    taskCompletionSource.setException(e); // Mark as failed
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("addCategories", "Failed to add category: " + e.getMessage(), e);
                                        taskCompletionSource.setException(e); // Mark as failed
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("addCategories", "Failed to query category name: " + e.getMessage(), e);
                        taskCompletionSource.setException(e); // Mark as failed
                    });
        }

        // When all tasks are complete, gather the results and call the callback
        Tasks.whenAllComplete(allTasks)
                .addOnCompleteListener(task -> {
                    List<String> successfulIds = new ArrayList<>();
                    Exception firstException = null;

                    // Process the results of all tasks
                    for (Task<String> categoryTask : allTasks) {
                        if (categoryTask.isSuccessful() && categoryTask.getResult() != null) {
                            successfulIds.add(categoryTask.getResult());
                        } else if (categoryTask.getException() != null && firstException == null) {
                            // Keep track of the first exception encountered
                            firstException = categoryTask.getException();
                        }
                    }

                    // Call appropriate callback method
                    if (callback != null) {
                        if (!successfulIds.isEmpty()) {
                            Log.d("addCategories", "Successfully added " + successfulIds.size() + " categories with IDs: " + successfulIds);
                            callback.onComplete(successfulIds);
                        } else if (firstException != null) {
                            Log.e("addCategories", "Failed to add any categories", firstException);
                            callback.onFailure(firstException);
                        } else {
                            Log.w("addCategories", "No categories were added (all may have been duplicates)");
                            callback.onComplete(successfulIds); // Empty list
                        }
                    }
                });
    }


    /**
     * Removes a specified category from the Firestore database.
     * This method performs the following steps:
     * 1. Queries the "expenses" subcollection to check if there are any expenses associated with the given categoryId.
     *    - If there are expenses, it cannot remove the category and triggers the exception via the callback:
     *      ExpenseWithCategoryExistsException
     * 2. If no expenses are found with the categoryId:
     *    - Deletes the category document in the "categories" subcollection with the specified categoryId.
     *
     * @param userEmail The email of the user under whose document the category will be removed.
     * @param categoryId The ID of the category document to remove.
     * @param callback The FirestoreIdCallback to handle success or failure events.
     *                 - On success, it provides the categoryId of the removed document.
     *                 - On failure, it provides the occurred exception.
     */
    public static void removeCategory(String userEmail, String categoryId, FirestoreIdCallback callback) {
        // Query Firestore to check if there are any expenses with the given category ID
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(EXPENSES_SUBCOLLECTION)
                .whereEqualTo(CATEGORY_ID_FIELD, categoryId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {Log.d("removeCategory", "Checking if any expenses exists with categoryId: " + categoryId);
                    if (!querySnapshot.isEmpty()) {
                        // There are expenses with this category ID, so we cannot delete the category
                        Log.w("removeCategory", "Cannot delete category: " + categoryId + " with existing expenses");
                        if (callback != null) callback.onFailure(new ExpenseWithCategoryExistsException("Cannot delete category with existing expenses."));
                        return;
                    } Log.d("removeCategory", "No expenses found for categoryId: " + categoryId);

                    // No expenses found with this category ID, proceed to delete the category
                    db.collection(USERS_COLLECTION)
                            .document(userEmail)
                            .collection(CATEGORIES_SUBCOLLECTION)
                            .document(categoryId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("removeCategory", "Category removed successfully with ID: " + categoryId);
                                if (callback != null) callback.onComplete(categoryId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("removeCategory", "Failed to delete category: " + e.getMessage(), e);
                                if (callback != null) callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("removeCategory", "Failed to check for expenses with category: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e);
                });

    }

    /**
     * Modifies an existing category's details in the Firestore database.
     * This method performs the following steps:
     * 1. Checks if a category with the same name (as the modified category) already exists in the user's "categories" subcollection.
     *    - If a category with the same name exists (excluding the current category), the operation is aborted, and an exception is triggered through the callback.
     * 2. If no category with the same name is found:
     *    - Overwrites the document in the "categories" subcollection with the provided categoryId, using the details from the modifiedCategory object.
     *
     * @param userEmail The email of the user under whose document the category will be modified.
     * @param categoryId The ID of the category document to modify.
     * @param modifiedCategory The Category object containing the updated details.
     * @param callback The FirestoreIdCallback to handle success or failure events.
     *                 - On success, it provides the categoryId of the modified document.
     *                 - On failure, it provides the occurred exception.
     */
    public static void editCategory(String userEmail, String categoryId, Category modifiedCategory, FirestoreIdCallback callback) {
        // Check modifiedCategory.name doesn't exist
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoryCollectionRef = db.collection(USERS_COLLECTION).document(userEmail)
                .collection(CATEGORIES_SUBCOLLECTION);
        categoryCollectionRef.whereEqualTo(NAME_FIELD, modifiedCategory.getName()).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // modifiedCategory.name already exist
                        Log.w("editCategory", "Category already exists: " + modifiedCategory.getName());
                        if (callback != null)
                            callback.onFailure(new CategoryAlreadyExistsException(modifiedCategory.getName()));
                    }
                    else {
                        // modifiedCategory.name free to use - overwrite categoryId document
                        categoryCollectionRef.document(categoryId).set(modifiedCategory)
                                .addOnSuccessListener(unused -> {
                                    Log.d("editCategory", "Category edited successfully with ID: " + categoryId);
                                    // Successfully updated categoryId document
                                    if (callback != null)
                                        callback.onComplete(categoryId);
                                })
                                .addOnFailureListener(e -> {
                                    // Failed to overwrite categoryId document
                                    Log.e("editCategory", "Failed to edit category: " + e.getMessage(), e);
                                    if (callback != null)
                                        callback.onFailure(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed to query category name
                    Log.e("editCategory", "Failed to query category name: " + e.getMessage(), e);
                    if (callback != null)
                        callback.onFailure(e);
                });
    }

    /**
     * If category document is found by categoryId, updates the icon index field and notifies
     * callback the result.
     * @param userEmail the user's mail to access his category
     * @param categoryId the category identifier
     * @param iconIndex the item index inside arrays.xml representing the drawable icon name
     * @param callback for result operation
     */
    public static void editCategoryIconIndex(String userEmail, String categoryId, int iconIndex, FirestoreIdCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference categoryDocumentRef =
                db.collection(USERS_COLLECTION)
                .document(userEmail).
                collection(CATEGORIES_SUBCOLLECTION)
                .document(categoryId);
        categoryDocumentRef.update(ICON_INDEX, iconIndex)
                .addOnSuccessListener(aVoid -> {
                    // Success modifying iconIndex
                    Log.d("editCategoryIconIndex", "edited category successfully");
                    if (callback != null) {
                        callback.onComplete(categoryId);
                    }
                })
                .addOnFailureListener(e -> {
                    // Failed modifying iconIndex
                    Log.e("editCategoryIconIndex", "failed to edit category", e);
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Retrieves all categories associated with a given user from Firestore.
     *
     * @param userEmail The email of the user whose categories are to be retrieved.
     * @param callback  The FirestoreListCallback to handle the results.
     *                  - onComplete: Called with the list of Category objects if successful.
     *                  - onFailure: Called with an exception if the operation fails.
     */
    public static void getCategories(String userEmail, FirestoreListCallback<Category> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userEmail)
                .collection(CATEGORIES_SUBCOLLECTION)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Category> categories = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Category category = document.toObject(Category.class);
                        categories.add(category);
                    }
                    if (callback != null) callback.onComplete(categories);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Fetches a list of categories from Firestore based on the provided list of document snapshots.
     * Ensures the returned list is in the same order as the input documents and matches its size.
     *
     * @param userEmail the email of the user to whom the categories belong
     * @param documents the list of document snapshots for which categories need to be fetched
     * @param callback the callback to handle the list of categories or an error
     */
    private static void getCategories(String userEmail, List<DocumentSnapshot> documents, FirestoreListCallback<Category> callback) {
        Log.d("getCategories", "Fetching " + documents.size() + " categories for user: " + userEmail);
        // Create a fixed-size array to store categories in the correct order
        Category[] categoriesArray = new Category[documents.size()];
        List<Task<Category>> tasks = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {
            final int index = i; // Capture the index for order preservation
            DocumentSnapshot document = documents.get(i);

            // Get category id
            String categoryId = document.getString(CATEGORY_ID_FIELD);
            if (categoryId == null) {
                Log.e("getCategories", "category id is null", new NullPointerException());
                return;
            }

            // Create a task to fetch the category for the current document
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Task<Category> categoryTask = db.collection(USERS_COLLECTION)
                    .document(userEmail)
                    .collection(CATEGORIES_SUBCOLLECTION)
                    .document(categoryId) // Assuming expense's category ID corresponds to category document ID
                    .get()
                    .continueWith(task -> {
                        Log.d("getCategories", "Fetching category for document: " + document.getId());
                        if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                            Log.e("Error", "Null Category!");
                            throw new CategoryNotFoundException("Category not found for document: " + document.getId());
                        }

                        Log.d("getCategories", "Fetched category for document: " + document.getId());
                        // Convert the document snapshot to a Category object
                        return task.getResult().toObject(Category.class);
                    });

            // Store the fetched category in the correct index upon task completion
            categoryTask.addOnSuccessListener(category -> categoriesArray[index] = category);
            tasks.add(categoryTask);
        }

        // Wait for all tasks to complete
        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(completedTasks -> {
                    Log.d("getCategories", "All categories fetched successfully");
                    List<Category> categories = Arrays.asList(categoriesArray); // Convert array to list
                    if (callback != null) {
                        callback.onComplete(categories); // Pass the completed list to the callback
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("getCategories", "Failed to fetch categories: " + e.getMessage(), e);
                    if (callback != null) callback.onFailure(e); // Handle failure
                });
    }

    /**
     * add default categories - won't be added if already exist
     * @param userEmail the user to which to add the categories
     */
    public static void addDefaultCategories(String userEmail, FirestoreListCallback<String> callback) {
        Category utilities = new Category("Utilities", 0);
        Category transportation = new Category("Transportation", 1);
        Category food = new Category("Food", 2);
        Category insurance = new Category("Insurance", 3);
        Category communications = new Category("Communications", 4);

        List<Category> categories = Arrays.asList(utilities, transportation, food, insurance, communications);

        addCategories(userEmail, categories, new FirestoreListCallback<>() {
            @Override
            public void onComplete(List<String> successfullyAddedIds) {
                if (callback != null) {
                    callback.onComplete(successfullyAddedIds);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });

    }
}
