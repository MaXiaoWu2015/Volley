/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import android.os.SystemClock;
/*     */ import com.android.volley.AuthFailureError;
/*     */ import com.android.volley.Cache.Entry;
/*     */ import com.android.volley.Network;
/*     */ import com.android.volley.NetworkError;
/*     */ import com.android.volley.NetworkResponse;
/*     */ import com.android.volley.NoConnectionError;
/*     */ import com.android.volley.Request;
/*     */ import com.android.volley.RetryPolicy;
/*     */ import com.android.volley.ServerError;
/*     */ import com.android.volley.TimeoutError;
/*     */ import com.android.volley.VolleyError;
/*     */ import com.android.volley.VolleyLog;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.SocketTimeoutException;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.TreeMap;
/*     */ import org.apache.http.Header;
/*     */ import org.apache.http.HttpEntity;
/*     */ import org.apache.http.HttpResponse;
/*     */ import org.apache.http.StatusLine;
/*     */ import org.apache.http.conn.ConnectTimeoutException;
/*     */ import org.apache.http.impl.cookie.DateUtils;
/*     */ 
/*     */ public class BasicNetwork
/*     */   implements Network
/*     */ {
/*  57 */   protected static final boolean DEBUG = VolleyLog.DEBUG;
/*     */ 
/*  59 */   private static int SLOW_REQUEST_THRESHOLD_MS = 3000;
/*     */ 
/*  61 */   private static int DEFAULT_POOL_SIZE = 4096;
/*     */   protected final HttpStack mHttpStack;
/*     */   protected final ByteArrayPool mPool;
/*     */ 
/*     */   public BasicNetwork(HttpStack httpStack)
/*     */   {
/*  73 */     this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
/*     */   }
/*     */ 
/*     */   public BasicNetwork(HttpStack httpStack, ByteArrayPool pool)
/*     */   {
/*  81 */     this.mHttpStack = httpStack;
/*  82 */     this.mPool = pool;
/*     */   }
/*     */ 
/*     */   public NetworkResponse performRequest(Request<?> request) throws VolleyError {
/*  87 */     long requestStart = SystemClock.elapsedRealtime();
/*     */     NetworkResponse networkResponse;
/*     */     while (true) {
/*  89 */       HttpResponse httpResponse = null;
/*  90 */       byte[] responseContents = null;
/*  91 */       Map responseHeaders = Collections.emptyMap();
/*     */       int statusCode;
/*     */       try {
/*  94 */         Map headers = new HashMap();
/*  95 */         addCacheHeaders(headers, request.getCacheEntry());
/*  96 */         httpResponse = this.mHttpStack.performRequest(request, headers);
/*  97 */         StatusLine statusLine = httpResponse.getStatusLine();
/*  98 */         int statusCode = statusLine.getStatusCode();
/*     */ 
/* 100 */         responseHeaders = convertHeaders(httpResponse.getAllHeaders());
/*     */ 
/* 102 */         if (statusCode == 304)
/*     */         {
/* 104 */           Cache.Entry entry = request.getCacheEntry();
/* 105 */           if (entry == null) {
/* 106 */             return new NetworkResponse(304, null, 
/* 107 */               responseHeaders, true, 
/* 108 */               SystemClock.elapsedRealtime() - requestStart);
/*     */           }
/*     */ 
/* 115 */           entry.responseHeaders.putAll(responseHeaders);
/* 116 */           return new NetworkResponse(304, entry.data, 
/* 117 */             entry.responseHeaders, true, 
/* 118 */             SystemClock.elapsedRealtime() - requestStart);
/*     */         }
/*     */ 
/* 122 */         if (httpResponse.getEntity() != null) {
/* 123 */           responseContents = entityToBytes(httpResponse.getEntity());
/*     */         }
/*     */         else
/*     */         {
/* 127 */           responseContents = new byte[0];
/*     */         }
/*     */ 
/* 131 */         long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
/* 132 */         logSlowRequests(requestLifetime, request, responseContents, statusLine);
/*     */ 
/* 134 */         if ((statusCode < 200) || (statusCode > 299)) {
/* 135 */           throw new IOException();
/*     */         }
/* 137 */         return new NetworkResponse(statusCode, responseContents, responseHeaders, false, 
/* 138 */           SystemClock.elapsedRealtime() - requestStart);
/*     */       } catch (SocketTimeoutException e) {
/* 140 */         attemptRetryOnException("socket", request, new TimeoutError());
/*     */       } catch (ConnectTimeoutException e) {
/* 142 */         attemptRetryOnException("connection", request, new TimeoutError());
/*     */       } catch (MalformedURLException e) {
/* 144 */         throw new RuntimeException("Bad URL " + request.getUrl(), e);
/*     */       } catch (IOException e) {
/* 146 */         statusCode = 0;
/* 147 */         networkResponse = null;
/* 148 */         if (httpResponse != null)
/* 149 */           statusCode = httpResponse.getStatusLine().getStatusCode();
/*     */         else
/* 151 */           throw new NoConnectionError(e);
/*     */       }
/* 153 */       VolleyLog.e("Unexpected response code %d for %s", new Object[] { Integer.valueOf(statusCode), request.getUrl() });
/* 154 */       if (responseContents == null) break label450;
/* 155 */       networkResponse = new NetworkResponse(statusCode, responseContents, 
/* 156 */         responseHeaders, false, SystemClock.elapsedRealtime() - requestStart);
/* 157 */       if ((statusCode != 401) && 
/* 158 */         (statusCode != 403)) break;
/* 159 */       attemptRetryOnException("auth", 
/* 160 */         request, new AuthFailureError(networkResponse));
/*     */     }
/*     */ 
/* 163 */     throw new ServerError(networkResponse);
/*     */ 
/* 166 */     label450: throw new NetworkError(networkResponse);
/*     */   }
/*     */ 
/*     */   private void logSlowRequests(long requestLifetime, Request<?> request, byte[] responseContents, StatusLine statusLine)
/*     */   {
/* 177 */     if ((DEBUG) || (requestLifetime > SLOW_REQUEST_THRESHOLD_MS))
/* 178 */       VolleyLog.d("HTTP response for request=<%s> [lifetime=%d], [size=%s], [rc=%d], [retryCount=%s]", new Object[] { 
/* 179 */         request, Long.valueOf(requestLifetime), 
/* 180 */         (responseContents != null) ? Integer.valueOf(responseContents.length) : "null", 
/* 181 */         Integer.valueOf(statusLine.getStatusCode()), Integer.valueOf(request.getRetryPolicy().getCurrentRetryCount()) });
/*     */   }
/*     */ 
/*     */   private static void attemptRetryOnException(String logPrefix, Request<?> request, VolleyError exception)
/*     */     throws VolleyError
/*     */   {
/* 192 */     RetryPolicy retryPolicy = request.getRetryPolicy();
/* 193 */     int oldTimeout = request.getTimeoutMs();
/*     */     try
/*     */     {
/* 196 */       retryPolicy.retry(exception);
/*     */     } catch (VolleyError e) {
/* 198 */       request.addMarker(
/* 199 */         String.format("%s-timeout-giveup [timeout=%s]", new Object[] { logPrefix, Integer.valueOf(oldTimeout) }));
/* 200 */       throw e;
/*     */     }
/* 202 */     request.addMarker(String.format("%s-retry [timeout=%s]", new Object[] { logPrefix, Integer.valueOf(oldTimeout) }));
/*     */   }
/*     */ 
/*     */   private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry)
/*     */   {
/* 207 */     if (entry == null) {
/* 208 */       return;
/*     */     }
/*     */ 
/* 211 */     if (entry.etag != null) {
/* 212 */       headers.put("If-None-Match", entry.etag);
/*     */     }
/*     */ 
/* 215 */     if (entry.lastModified > 0L) {
/* 216 */       Date refTime = new Date(entry.lastModified);
/* 217 */       headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void logError(String what, String url, long start) {
/* 222 */     long now = SystemClock.elapsedRealtime();
/* 223 */     VolleyLog.v("HTTP ERROR(%s) %d ms to fetch %s", new Object[] { what, Long.valueOf(now - start), url });
/*     */   }
/*     */ 
/*     */   private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError
/*     */   {
/* 228 */     PoolingByteArrayOutputStream bytes = 
/* 229 */       new PoolingByteArrayOutputStream(this.mPool, (int)entity.getContentLength());
/* 230 */     byte[] buffer = null;
/*     */     try {
/* 232 */       InputStream in = entity.getContent();
/* 233 */       if (in == null) {
/* 234 */         throw new ServerError();
/*     */       }
/* 236 */       buffer = this.mPool.getBuf(1024);
/*     */       int count;
/* 238 */       while ((count = in.read(buffer)) != -1)
/*     */       {
/*     */         int count;
/* 239 */         bytes.write(buffer, 0, count);
/*     */       }
/* 241 */       return bytes.toByteArray();
/*     */     }
/*     */     finally {
/*     */       try {
/* 245 */         entity.consumeContent();
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 249 */         VolleyLog.v("Error occured when calling consumingContent", new Object[0]);
/*     */       }
/* 251 */       this.mPool.returnBuf(buffer);
/* 252 */       bytes.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static Map<String, String> convertHeaders(Header[] headers)
/*     */   {
/* 260 */     Map result = new TreeMap(String.CASE_INSENSITIVE_ORDER);
/* 261 */     for (int i = 0; i < headers.length; ++i) {
/* 262 */       result.put(headers[i].getName(), headers[i].getValue());
/*     */     }
/* 264 */     return result;
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.BasicNetwork
 * JD-Core Version:    0.5.4
 */