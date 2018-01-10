/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * <p/>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.react.modules.fresco;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import expolib_v1.com.facebook.imagepipeline.backends.okhttp3.OkHttpNetworkFetcher;
import android.net.Uri;
import android.os.SystemClock;

import expolib_v1.com.facebook.imagepipeline.backends.okhttp3.OkHttpNetworkFetcher;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import expolib_v1.okhttp3.CacheControl;
import expolib_v1.okhttp3.Headers;
import expolib_v1.okhttp3.OkHttpClient;
import expolib_v1.okhttp3.Request;

class ReactOkHttpNetworkFetcher extends OkHttpNetworkFetcher {

  private static final String TAG = "ReactOkHttpNetworkFetcher";

  private final OkHttpClient mOkHttpClient;
  private final Executor mCancellationExecutor;

  /**
   * @param okHttpClient client to use
   */
  public ReactOkHttpNetworkFetcher(OkHttpClient okHttpClient) {
    super(okHttpClient);
    mOkHttpClient = okHttpClient;
    mCancellationExecutor = okHttpClient.dispatcher().executorService();
  }

  private Map<String, String> getHeaders(ReadableMap readableMap) {
    if (readableMap == null) {
        return null;
    }
    ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
    Map<String, String> map = new HashMap<>();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      String value = readableMap.getString(key);
      map.put(key, value);
    }
    return map;
  }

  @Override
  public void fetch(final OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState, final NetworkFetcher.Callback callback) {
    fetchState.submitTime = SystemClock.elapsedRealtime();
    final Uri uri = fetchState.getUri();
    Map<String, String> requestHeaders = null;
    if (fetchState.getContext().getImageRequest() instanceof ReactNetworkImageRequest) {
      ReactNetworkImageRequest networkImageRequest = (ReactNetworkImageRequest)
        fetchState.getContext().getImageRequest();
      requestHeaders = getHeaders(networkImageRequest.getHeaders());
    }
    if (requestHeaders == null) {
      requestHeaders = Collections.emptyMap();
    }
    final Request request = new Request.Builder()
      .cacheControl(new CacheControl.Builder().noStore().build())
      .url(uri.toString())
      .headers(Headers.of(requestHeaders))
      .get()
      .build();

    fetchWithRequest(fetchState, callback, request);
  }
}
