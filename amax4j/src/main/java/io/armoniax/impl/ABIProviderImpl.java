/*
 * Copyright (c) 2017-2019 block.one all rights reserved.
 */

package io.armoniax.impl;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.armoniax.error.ErrorConstants;
import io.armoniax.error.abiProvider.GetAbiError;
import io.armoniax.provider.IABIProvider;
import io.armoniax.provider.IRPCProvider;
import io.armoniax.provider.ISerializationProvider;
import io.armoniax.models.AMAXName;
import io.armoniax.models.rpc.request.RawAbiRequest;
import io.armoniax.models.rpc.response.RawAbiResponse;
import io.armoniax.tools.ByteFormatter;

import org.jetbrains.annotations.NotNull;

/**
 * Default ABI Provider implementation, providing in memory caching of previously fetched
 * ABI's as well as fetching of ABI's using the provided RPC provider.
 */

public class ABIProviderImpl implements IABIProvider {

    private @NotNull IRPCProvider rpcProvider;
    private @NotNull ISerializationProvider serializationProvider;
    private @NotNull Map<String, String> abiCache = new ConcurrentHashMap<>();

    /**
     * Initialize a new ABI Provider, passing the necessary RPC provider to fetch ABI's
     * if they are not found in the cache.
     *
     * @param rpcProvider RPC provider implementation to use to fetch ABIs if they are not
     * in the cache.
     * @param serializationProvider Serialization provider implementation to use to deserialize
     * the ABIs for return and storage in the cache.
     */
    public ABIProviderImpl(@NotNull IRPCProvider rpcProvider,
            @NotNull ISerializationProvider serializationProvider) {
        this.rpcProvider = rpcProvider;
        this.serializationProvider = serializationProvider;
    }

    /**
     * Return a map of ABIs given the chain id and account names desired.  The in-memory cache
     * will be checked first and any missing ABIs will be fetched via the RPC provider and placed
     * in the cache.  This call is synchronous.  Developers should wrap it
     * in an asynchronous mechanism to avoid blocking if necessary.
     *
     * @param chainId the chain id
     * @param accounts the accounts - duplicate names will be removed
     * @return Map of ABIs keyed by the account
     * @throws GetAbiError If there is an error retrieving or deserializing any of the ABIs
     */
    @Override
    public @NotNull Map<String, String> getAbis(@NotNull String chainId,
            @NotNull List<AMAXName> accounts) throws GetAbiError {

        Map<String, String> returnAbis = new HashMap<>();
        List<AMAXName> uniqAccounts = new ArrayList<>(new HashSet<>(accounts));

        for (AMAXName account : uniqAccounts) {
            String abiJsonString = getAbi(chainId, account);
            returnAbis.put(account.getAccountName(), abiJsonString);
        }

        return returnAbis;
    }

    /**
     * Return an ABI given the chain id and account name desired.  The in-memory cache
     * will be checked first and if missing the ABI will be fetched via the RPC provider
     * and placed in the cache.  This call is synchronous.  Developers should wrap it
     * in an asynchronous mechanism to avoid blocking if necessary.
     * @param chainId the chain id
     * @param account the account
     * @return abiJsonString - the deserialized JSON string for the requested ABI
     * @throws GetAbiError If there is an error retrieving or deserializing the ABI.
     */
    @Override
    public @NotNull String getAbi(@NotNull String chainId, @NotNull AMAXName account)
            throws GetAbiError {

        String abiJsonString;
        String cacheKey = chainId + account.getAccountName();

        abiJsonString = this.abiCache.get(cacheKey);
        if (!Strings.isNullOrEmpty(abiJsonString)) {
            return abiJsonString;
        }

        RawAbiRequest rawAbiRequest = new RawAbiRequest(account.getAccountName());
        try {
            RawAbiResponse rawAbiResponse = this.rpcProvider.getRawAbi(rawAbiRequest);
            if (rawAbiResponse == null) {
                throw new GetAbiError(ErrorConstants.NO_RESPONSE_RETRIEVING_ABI);
            }

            String abi = rawAbiResponse.getAbi();
            if (Strings.isNullOrEmpty(abi)) {
                throw new GetAbiError(ErrorConstants.MISSING_ABI_FROM_RESPONSE);
            }

            ByteFormatter abiByteFormatter = ByteFormatter.createFromBase64(abi);

            String calculatedHash = abiByteFormatter.sha256().toHex().toLowerCase();
            String verificationHash = rawAbiResponse.getAbiHash().toLowerCase();
            if (!calculatedHash.equals(verificationHash)) {
                throw new GetAbiError(ErrorConstants.CALCULATED_HASH_NOT_EQUAL_RETURNED);
            }

            if (!account.getAccountName().equals(rawAbiResponse.getAccountName())) {
                throw new GetAbiError(ErrorConstants.REQUESTED_ACCCOUNT_NOT_EQUAL_RETURNED);
            }

            abiJsonString = this.serializationProvider.deserializeAbi(abiByteFormatter.toHex());
            if (abiJsonString.isEmpty()) {
                throw new GetAbiError(ErrorConstants.NO_ABI_FOUND);
            }

            this.abiCache.put(cacheKey, abiJsonString);

        } catch (Exception ex) {
            throw new GetAbiError(ErrorConstants.ERROR_RETRIEVING_ABI, ex);
        }

        return abiJsonString;
    }
}
