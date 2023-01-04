package io.armoniax.models.rpc.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.armoniax.models.rpc.request.TransactionRequest;
import lombok.Data;

/**
 * The response of the sendTransaction() RPC call
 * {@link io.armoniax.provider.IRPCProvider#sendTransaction(TransactionRequest)}
 */
@Data
public class TransactionResponse {

    /**
     * The transaction id of the successful transaction.
     */
    @SerializedName("transaction_id")
    private String transactionId;

    /**
     * The successful processed details of the transaction.
     */
    @SerializedName("processed")
    private Map processed;

    private transient static final String ACTION_TRACES_NAME = "action_traces";
    private transient static final String RETURN_VALUE_DATA_NAME = "return_value_data";


    /**
     * Return action values, if any.  The returned values are placed in their respective actions.
     * The array must contain null for the actions that do not return action values.
     * There may be more action values than input actions due to inline actions or notifications
     * but input (request) actions are always returned first and in the same order as they were
     * submitted.
     *
     * @return ArrayList of Objects containing the return values from the response.
     */
    public ArrayList<Object> getActionValues() {
        ArrayList<Object> returnValues = new ArrayList<Object>();
        if (processed == null) { return returnValues; }
        if (!processed.containsKey(ACTION_TRACES_NAME)) { return returnValues; }
        for (Map trace : (List<Map>) processed.get(ACTION_TRACES_NAME)) {
            returnValues.add(trace.getOrDefault(RETURN_VALUE_DATA_NAME, null));
        }
        return returnValues;
    }

    /**
     * Get the action value at the specified index, if it exists and return it as the passed in type.
     * @param index The index of the action value returns to retrieve.
     * @param clazz The class type to cast the action value to, if found.
     * @param <T> Typed return.
     * @return The action value as the desired type or null if not found or is the wrong type.
     * @throws ClassCastException if the value cannot be cast to the requested type.
     * @throws IndexOutOfBoundsException if an incorrect index is requested.
     */
    public <T> T getActionValueAtIndex(int index, Class<T> clazz) throws IndexOutOfBoundsException, ClassCastException {
        ArrayList<Object> actionValues = getActionValues();
        if (actionValues == null) { return null; }
        Object actionValuesObj = actionValues.get(index);
        return clazz.cast(actionValuesObj);
    }
}