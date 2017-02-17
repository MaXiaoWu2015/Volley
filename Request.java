/*     */ package com.android.volley;
/*     */ 
/*     */ import android.net.Uri;
/*     */ import android.os.Handler;
/*     */ import android.os.Looper;
/*     */ import android.os.SystemClock;
/*     */ import android.text.TextUtils;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.URLEncoder;
/*     */ import java.util.Collections;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ 
/*     */ public abstract class Request<T>
/*     */   implements Comparable<Request<T>>
/*     */ {
/*     */   private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
/*  61 */   private final VolleyLog.MarkerLog mEventLog = (VolleyLog.MarkerLog.ENABLED) ? new VolleyLog.MarkerLog() : null;
/*     */   private final int mMethod;
/*     */   private final String mUrl;
/*     */   private final int mDefaultTrafficStatsTag;
/*     */   private final Response.ErrorListener mErrorListener;
/*     */   private Integer mSequence;
/*     */   private RequestQueue mRequestQueue;
/*  85 */   private boolean mShouldCache = true;
/*     */ 
/*  88 */   private boolean mCanceled = false;
/*     */ 
/*  91 */   private boolean mResponseDelivered = false;
/*     */ 
/*  94 */   private long mRequestBirthTime = 0L;
/*     */   private static final long SLOW_REQUEST_THRESHOLD_MS = 3000L;
/*     */   private RetryPolicy mRetryPolicy;
/* 107 */   private Cache.Entry mCacheEntry = null;
/*     */   private Object mTag;
/*     */ 
/*     */   @Deprecated
/*     */   public Request(String url, Response.ErrorListener listener)
/*     */   {
/* 122 */     this(-1, url, listener);
/*     */   }
/*     */ 
/*     */   public Request(int method, String url, Response.ErrorListener listener)
/*     */   {
/* 132 */     this.mMethod = method;
/* 133 */     this.mUrl = url;
/* 134 */     this.mErrorListener = listener;
/* 135 */     setRetryPolicy(new DefaultRetryPolicy());
/*     */ 
/* 137 */     this.mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
/*     */   }
/*     */ 
/*     */   public int getMethod()
/*     */   {
/* 144 */     return this.mMethod;
/*     */   }
/*     */ 
/*     */   public Request<?> setTag(Object tag)
/*     */   {
/* 154 */     this.mTag = tag;
/* 155 */     return this;
/*     */   }
/*     */ 
/*     */   public Object getTag()
/*     */   {
/* 163 */     return this.mTag;
/*     */   }
/*     */ 
/*     */   public Response.ErrorListener getErrorListener()
/*     */   {
/* 170 */     return this.mErrorListener;
/*     */   }
/*     */ 
/*     */   public int getTrafficStatsTag()
/*     */   {
/* 177 */     return this.mDefaultTrafficStatsTag;
/*     */   }
/*     */ 
/*     */   private static int findDefaultTrafficStatsTag(String url)
/*     */   {
/* 184 */     if (!TextUtils.isEmpty(url)) {
/* 185 */       Uri uri = Uri.parse(url);
/* 186 */       if (uri != null) {
/* 187 */         String host = uri.getHost();
/* 188 */         if (host != null) {
/* 189 */           return host.hashCode();
/*     */         }
/*     */       }
/*     */     }
/* 193 */     return 0;
/*     */   }
/*     */ 
/*     */   public Request<?> setRetryPolicy(RetryPolicy retryPolicy)
/*     */   {
/* 202 */     this.mRetryPolicy = retryPolicy;
/* 203 */     return this;
/*     */   }
/*     */ 
/*     */   public void addMarker(String tag)
/*     */   {
/* 210 */     if (VolleyLog.MarkerLog.ENABLED)
/* 211 */       this.mEventLog.add(tag, Thread.currentThread().getId());
/* 212 */     else if (this.mRequestBirthTime == 0L)
/* 213 */       this.mRequestBirthTime = SystemClock.elapsedRealtime();
/*     */   }
/*     */ 
/*     */   void finish(String tag)
/*     */   {
/* 223 */     if (this.mRequestQueue != null) {
/* 224 */       this.mRequestQueue.finish(this);
/*     */     }
/* 226 */     if (VolleyLog.MarkerLog.ENABLED) {
/* 227 */       long threadId = Thread.currentThread().getId();
/* 228 */       if (Looper.myLooper() != Looper.getMainLooper())
/*     */       {
/* 231 */         Handler mainThread = new Handler(Looper.getMainLooper());
/* 232 */         mainThread.post(new Runnable(tag, threadId)
/*     */         {
/*     */           public void run() {
/* 235 */             Request.this.mEventLog.add(this.val$tag, this.val$threadId);
/* 236 */             Request.this.mEventLog.finish(super.toString());
/*     */           }
/*     */         });
/* 239 */         return;
/*     */       }
/*     */ 
/* 242 */       this.mEventLog.add(tag, threadId);
/* 243 */       this.mEventLog.finish(toString());
/*     */     } else {
/* 245 */       long requestTime = SystemClock.elapsedRealtime() - this.mRequestBirthTime;
/* 246 */       if (requestTime >= 3000L)
/* 247 */         VolleyLog.d("%d ms: %s", new Object[] { Long.valueOf(requestTime), toString() });
/*     */     }
/*     */   }
/*     */ 
/*     */   public Request<?> setRequestQueue(RequestQueue requestQueue)
/*     */   {
/* 259 */     this.mRequestQueue = requestQueue;
/* 260 */     return this;
/*     */   }
/*     */ 
/*     */   public final Request<?> setSequence(int sequence)
/*     */   {
/* 269 */     this.mSequence = Integer.valueOf(sequence);
/* 270 */     return this;
/*     */   }
/*     */ 
/*     */   public final int getSequence()
/*     */   {
/* 277 */     if (this.mSequence == null) {
/* 278 */       throw new IllegalStateException("getSequence called before setSequence");
/*     */     }
/* 280 */     return this.mSequence.intValue();
/*     */   }
/*     */ 
/*     */   public String getUrl()
/*     */   {
/* 287 */     return this.mUrl;
/*     */   }
/*     */ 
/*     */   public String getCacheKey()
/*     */   {
/* 294 */     return getUrl();
/*     */   }
/*     */ 
/*     */   public Request<?> setCacheEntry(Cache.Entry entry)
/*     */   {
/* 304 */     this.mCacheEntry = entry;
/* 305 */     return this;
/*     */   }
/*     */ 
/*     */   public Cache.Entry getCacheEntry()
/*     */   {
/* 312 */     return this.mCacheEntry;
/*     */   }
/*     */ 
/*     */   public void cancel()
/*     */   {
/* 319 */     this.mCanceled = true;
/*     */   }
/*     */ 
/*     */   public boolean isCanceled()
/*     */   {
/* 326 */     return this.mCanceled;
/*     */   }
/*     */ 
/*     */   public Map<String, String> getHeaders()
/*     */     throws AuthFailureError
/*     */   {
/* 336 */     return Collections.emptyMap();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected Map<String, String> getPostParams()
/*     */     throws AuthFailureError
/*     */   {
/* 352 */     return getParams();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected String getPostParamsEncoding()
/*     */   {
/* 371 */     return getParamsEncoding();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getPostBodyContentType()
/*     */   {
/* 379 */     return getBodyContentType();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public byte[] getPostBody()
/*     */     throws AuthFailureError
/*     */   {
/* 395 */     Map postParams = getPostParams();
/* 396 */     if ((postParams != null) && (postParams.size() > 0)) {
/* 397 */       return encodeParameters(postParams, getPostParamsEncoding());
/*     */     }
/* 399 */     return null;
/*     */   }
/*     */ 
/*     */   protected Map<String, String> getParams()
/*     */     throws AuthFailureError
/*     */   {
/* 411 */     return null;
/*     */   }
/*     */ 
/*     */   protected String getParamsEncoding()
/*     */   {
/* 427 */     return "UTF-8";
/*     */   }
/*     */ 
/*     */   public String getBodyContentType()
/*     */   {
/* 434 */     return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
/*     */   }
/*     */ 
/*     */   public byte[] getBody()
/*     */     throws AuthFailureError
/*     */   {
/* 447 */     Map params = getParams();
/* 448 */     if ((params != null) && (params.size() > 0)) {
/* 449 */       return encodeParameters(params, getParamsEncoding());
/*     */     }
/* 451 */     return null;
/*     */   }
/*     */ 
/*     */   private byte[] encodeParameters(Map<String, String> params, String paramsEncoding)
/*     */   {
/* 458 */     StringBuilder encodedParams = new StringBuilder();
/*     */     try {
/* 460 */       for (Map.Entry entry : params.entrySet()) {
/* 461 */         encodedParams.append(URLEncoder.encode((String)entry.getKey(), paramsEncoding));
/* 462 */         encodedParams.append('=');
/* 463 */         encodedParams.append(URLEncoder.encode((String)entry.getValue(), paramsEncoding));
/* 464 */         encodedParams.append('&');
/*     */       }
/* 466 */       return encodedParams.toString().getBytes(paramsEncoding);
/*     */     } catch (UnsupportedEncodingException uee) {
/* 468 */       throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
/*     */     }
/*     */   }
/*     */ 
/*     */   public final Request<?> setShouldCache(boolean shouldCache)
/*     */   {
/* 478 */     this.mShouldCache = shouldCache;
/* 479 */     return this;
/*     */   }
/*     */ 
/*     */   public final boolean shouldCache()
/*     */   {
/* 486 */     return this.mShouldCache;
/*     */   }
/*     */ 
/*     */   public Priority getPriority()
/*     */   {
/* 504 */     return Priority.NORMAL;
/*     */   }
/*     */ 
/*     */   public final int getTimeoutMs()
/*     */   {
/* 513 */     return this.mRetryPolicy.getCurrentTimeout();
/*     */   }
/*     */ 
/*     */   public RetryPolicy getRetryPolicy()
/*     */   {
/* 520 */     return this.mRetryPolicy;
/*     */   }
/*     */ 
/*     */   public void markDelivered()
/*     */   {
/* 528 */     this.mResponseDelivered = true;
/*     */   }
/*     */ 
/*     */   public boolean hasHadResponseDelivered()
/*     */   {
/* 535 */     return this.mResponseDelivered;
/*     */   }
/*     */ 
/*     */   protected abstract Response<T> parseNetworkResponse(NetworkResponse paramNetworkResponse);
/*     */ 
/*     */   protected VolleyError parseNetworkError(VolleyError volleyError)
/*     */   {
/* 557 */     return volleyError;
/*     */   }
/*     */ 
/*     */   protected abstract void deliverResponse(T paramT);
/*     */ 
/*     */   public void deliverError(VolleyError error)
/*     */   {
/* 576 */     if (this.mErrorListener != null)
/* 577 */       this.mErrorListener.onErrorResponse(error);
/*     */   }
/*     */ 
/*     */   public int compareTo(Request<T> other)
/*     */   {
/* 587 */     Priority left = getPriority();
/* 588 */     Priority right = other.getPriority();
/*     */ 
/* 592 */     return (left == right) ? 
/* 593 */       this.mSequence.intValue() - other.mSequence.intValue() : 
/* 594 */       right.ordinal() - left.ordinal();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 599 */     String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
/* 600 */     return ((this.mCanceled) ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag + " " + 
/* 601 */       getPriority() + " " + this.mSequence;
/*     */   }
/*     */ 
/*     */   public static abstract interface Method
/*     */   {
/*     */     public static final int DEPRECATED_GET_OR_POST = -1;
/*     */     public static final int GET = 0;
/*     */     public static final int POST = 1;
/*     */     public static final int PUT = 2;
/*     */     public static final int DELETE = 3;
/*     */     public static final int HEAD = 4;
/*     */     public static final int OPTIONS = 5;
/*     */     public static final int TRACE = 6;
/*     */     public static final int PATCH = 7;
/*     */   }
/*     */ 
/*     */   public static enum Priority
/*     */   {
/* 494 */     LOW, 
/* 495 */     NORMAL, 
/* 496 */     HIGH, 
/* 497 */     IMMEDIATE;
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.Request
 * JD-Core Version:    0.5.4
 */