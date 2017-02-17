/*     */ package com.android.volley.toolbox;
/*     */ 
/*     */ import android.graphics.Bitmap;
/*     */ import android.graphics.Bitmap.Config;
/*     */ import android.os.Handler;
/*     */ import android.os.Looper;
/*     */ import android.widget.ImageView;
/*     */ import android.widget.ImageView.ScaleType;
/*     */ import com.android.volley.Request;
/*     */ import com.android.volley.RequestQueue;
/*     */ import com.android.volley.Response.ErrorListener;
/*     */ import com.android.volley.Response.Listener;
/*     */ import com.android.volley.VolleyError;
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ 
/*     */ public class ImageLoader
/*     */ {
/*     */   private final RequestQueue mRequestQueue;
/*  47 */   private int mBatchResponseDelayMs = 100;
/*     */   private final ImageCache mCache;
/*  57 */   private final HashMap<String, BatchedImageRequest> mInFlightRequests = new HashMap();
/*     */ 
/*  61 */   private final HashMap<String, BatchedImageRequest> mBatchedResponses = new HashMap();
/*     */ 
/*  64 */   private final Handler mHandler = new Handler(Looper.getMainLooper());
/*     */   private Runnable mRunnable;
/*     */ 
/*     */   public ImageLoader(RequestQueue queue, ImageCache imageCache)
/*     */   {
/*  85 */     this.mRequestQueue = queue;
/*  86 */     this.mCache = imageCache;
/*     */   }
/*     */ 
/*     */   public static ImageListener getImageListener(ImageView view, int defaultImageResId, int errorImageResId)
/*     */   {
/*  99 */     return new ImageListener(errorImageResId, view, defaultImageResId)
/*     */     {
/*     */       public void onErrorResponse(VolleyError error) {
/* 102 */         if (this.val$errorImageResId != 0)
/* 103 */           this.val$view.setImageResource(this.val$errorImageResId);
/*     */       }
/*     */ 
/*     */       public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate)
/*     */       {
/* 109 */         if (response.getBitmap() != null)
/* 110 */           this.val$view.setImageBitmap(response.getBitmap());
/* 111 */         else if (this.val$defaultImageResId != 0)
/* 112 */           this.val$view.setImageResource(this.val$defaultImageResId);
/*     */       }
/*     */     };
/*     */   }
/*     */ 
/*     */   public boolean isCached(String requestUrl, int maxWidth, int maxHeight)
/*     */   {
/* 153 */     return isCached(requestUrl, maxWidth, maxHeight, ImageView.ScaleType.CENTER_INSIDE);
/*     */   }
/*     */ 
/*     */   public boolean isCached(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType)
/*     */   {
/* 166 */     throwIfNotOnMainThread();
/*     */ 
/* 168 */     String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
/* 169 */     return this.mCache.getBitmap(cacheKey) != null;
/*     */   }
/*     */ 
/*     */   public ImageContainer get(String requestUrl, ImageListener listener)
/*     */   {
/* 182 */     return get(requestUrl, listener, 0, 0);
/*     */   }
/*     */ 
/*     */   public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight)
/*     */   {
/* 191 */     return get(requestUrl, imageListener, maxWidth, maxHeight, ImageView.ScaleType.CENTER_INSIDE);
/*     */   }
/*     */ 
/*     */   public ImageContainer get(String requestUrl, ImageListener imageListener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType)
/*     */   {
/* 211 */     throwIfNotOnMainThread();
/*     */ 
/* 213 */     String cacheKey = getCacheKey(requestUrl, maxWidth, maxHeight, scaleType);
/*     */ 
/* 216 */     Bitmap cachedBitmap = this.mCache.getBitmap(cacheKey);
/* 217 */     if (cachedBitmap != null)
/*     */     {
/* 219 */       ImageContainer container = new ImageContainer(cachedBitmap, requestUrl, null, null);
/* 220 */       imageListener.onResponse(container, true);
/* 221 */       return container;
/*     */     }
/*     */ 
/* 225 */     ImageContainer imageContainer = 
/* 226 */       new ImageContainer(null, requestUrl, cacheKey, imageListener);
/*     */ 
/* 229 */     imageListener.onResponse(imageContainer, true);
/*     */ 
/* 232 */     BatchedImageRequest request = (BatchedImageRequest)this.mInFlightRequests.get(cacheKey);
/* 233 */     if (request != null)
/*     */     {
/* 235 */       request.addContainer(imageContainer);
/* 236 */       return imageContainer;
/*     */     }
/*     */ 
/* 241 */     Request newRequest = makeImageRequest(requestUrl, maxWidth, maxHeight, scaleType, 
/* 242 */       cacheKey);
/*     */ 
/* 244 */     this.mRequestQueue.add(newRequest);
/* 245 */     this.mInFlightRequests.put(cacheKey, 
/* 246 */       new BatchedImageRequest(newRequest, imageContainer));
/* 247 */     return imageContainer;
/*     */   }
/*     */ 
/*     */   protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, String cacheKey)
/*     */   {
/* 252 */     return new ImageRequest(requestUrl, new Response.Listener(cacheKey)
/*     */     {
/*     */       public void onResponse(Bitmap response) {
/* 255 */         ImageLoader.this.onGetImageSuccess(this.val$cacheKey, response);
/*     */       }
/*     */     }
/*     */     , maxWidth, maxHeight, scaleType, Bitmap.Config.RGB_565, new Response.ErrorListener(cacheKey)
/*     */     {
/*     */       public void onErrorResponse(VolleyError error) {
/* 260 */         ImageLoader.this.onGetImageError(this.val$cacheKey, error);
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public void setBatchedResponseDelay(int newBatchedResponseDelayMs)
/*     */   {
/* 271 */     this.mBatchResponseDelayMs = newBatchedResponseDelayMs;
/*     */   }
/*     */ 
/*     */   protected void onGetImageSuccess(String cacheKey, Bitmap response)
/*     */   {
/* 281 */     this.mCache.putBitmap(cacheKey, response);
/*     */ 
/* 284 */     BatchedImageRequest request = (BatchedImageRequest)this.mInFlightRequests.remove(cacheKey);
/*     */ 
/* 286 */     if (request == null)
/*     */       return;
/* 288 */     request.mResponseBitmap = response;
/*     */ 
/* 291 */     batchResponse(cacheKey, request);
/*     */   }
/*     */ 
/*     */   protected void onGetImageError(String cacheKey, VolleyError error)
/*     */   {
/* 302 */     BatchedImageRequest request = (BatchedImageRequest)this.mInFlightRequests.remove(cacheKey);
/*     */ 
/* 304 */     if (request == null)
/*     */       return;
/* 306 */     request.setError(error);
/*     */ 
/* 309 */     batchResponse(cacheKey, request);
/*     */   }
/*     */ 
/*     */   private void batchResponse(String cacheKey, BatchedImageRequest request)
/*     */   {
/* 457 */     this.mBatchedResponses.put(cacheKey, request);
/*     */ 
/* 460 */     if (this.mRunnable == null) {
/* 461 */       this.mRunnable = new Runnable()
/*     */       {
/*     */         public void run() {
/* 464 */           for (ImageLoader.BatchedImageRequest bir : ImageLoader.this.mBatchedResponses.values()) {
/* 465 */             for (ImageLoader.ImageContainer container : ImageLoader.BatchedImageRequest.access$0(bir))
/*     */             {
/* 469 */               if (ImageLoader.ImageContainer.access$0(container) == null) {
/*     */                 continue;
/*     */               }
/* 472 */               if (bir.getError() == null) {
/* 473 */                 ImageLoader.ImageContainer.access$1(container, ImageLoader.BatchedImageRequest.access$2(bir));
/* 474 */                 ImageLoader.ImageContainer.access$0(container).onResponse(container, false);
/*     */               } else {
/* 476 */                 ImageLoader.ImageContainer.access$0(container).onErrorResponse(bir.getError());
/*     */               }
/*     */             }
/*     */           }
/* 480 */           ImageLoader.this.mBatchedResponses.clear();
/* 481 */           ImageLoader.this.mRunnable = null;
/*     */         }
/*     */       };
/* 486 */       this.mHandler.postDelayed(this.mRunnable, this.mBatchResponseDelayMs);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void throwIfNotOnMainThread() {
/* 491 */     if (Looper.myLooper() != Looper.getMainLooper())
/* 492 */       throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
/*     */   }
/*     */ 
/*     */   private static String getCacheKey(String url, int maxWidth, int maxHeight, ImageView.ScaleType scaleType)
/*     */   {
/* 503 */     return url.length() + 12 + "#W" + maxWidth + 
/* 504 */       "#H" + maxHeight + "#S" + scaleType.ordinal() + url;
/*     */   }
/*     */ 
/*     */   private class BatchedImageRequest
/*     */   {
/*     */     private final Request<?> mRequest;
/*     */     private Bitmap mResponseBitmap;
/*     */     private VolleyError mError;
/* 401 */     private final LinkedList<ImageLoader.ImageContainer> mContainers = new LinkedList();
/*     */ 
/*     */     public BatchedImageRequest(ImageLoader.ImageContainer request)
/*     */     {
/* 409 */       this.mRequest = request;
/* 410 */       this.mContainers.add(container);
/*     */     }
/*     */ 
/*     */     public void setError(VolleyError error)
/*     */     {
/* 417 */       this.mError = error;
/*     */     }
/*     */ 
/*     */     public VolleyError getError()
/*     */     {
/* 424 */       return this.mError;
/*     */     }
/*     */ 
/*     */     public void addContainer(ImageLoader.ImageContainer container)
/*     */     {
/* 432 */       this.mContainers.add(container);
/*     */     }
/*     */ 
/*     */     public boolean removeContainerAndCancelIfNecessary(ImageLoader.ImageContainer container)
/*     */     {
/* 442 */       this.mContainers.remove(container);
/* 443 */       if (this.mContainers.size() == 0) {
/* 444 */         this.mRequest.cancel();
/* 445 */         return true;
/*     */       }
/* 447 */       return false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract interface ImageCache
/*     */   {
/*     */     public abstract Bitmap getBitmap(String paramString);
/*     */ 
/*     */     public abstract void putBitmap(String paramString, Bitmap paramBitmap);
/*     */   }
/*     */ 
/*     */   public class ImageContainer
/*     */   {
/*     */     private Bitmap mBitmap;
/*     */     private final ImageLoader.ImageListener mListener;
/*     */     private final String mCacheKey;
/*     */     private final String mRequestUrl;
/*     */ 
/*     */     public ImageContainer(Bitmap bitmap, String requestUrl, String cacheKey, ImageLoader.ImageListener listener)
/*     */     {
/* 339 */       this.mBitmap = bitmap;
/* 340 */       this.mRequestUrl = requestUrl;
/* 341 */       this.mCacheKey = cacheKey;
/* 342 */       this.mListener = listener;
/*     */     }
/*     */ 
/*     */     public void cancelRequest()
/*     */     {
/* 349 */       if (this.mListener == null) {
/* 350 */         return;
/*     */       }
/*     */ 
/* 353 */       ImageLoader.BatchedImageRequest request = (ImageLoader.BatchedImageRequest)ImageLoader.this.mInFlightRequests.get(this.mCacheKey);
/* 354 */       if (request != null) {
/* 355 */         boolean canceled = request.removeContainerAndCancelIfNecessary(this);
/* 356 */         if (canceled)
/* 357 */           ImageLoader.this.mInFlightRequests.remove(this.mCacheKey);
/*     */       }
/*     */       else
/*     */       {
/* 361 */         request = (ImageLoader.BatchedImageRequest)ImageLoader.this.mBatchedResponses.get(this.mCacheKey);
/* 362 */         if (request != null) {
/* 363 */           request.removeContainerAndCancelIfNecessary(this);
/* 364 */           if (request.mContainers.size() == 0)
/* 365 */             ImageLoader.this.mBatchedResponses.remove(this.mCacheKey);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*     */     public Bitmap getBitmap()
/*     */     {
/* 375 */       return this.mBitmap;
/*     */     }
/*     */ 
/*     */     public String getRequestUrl()
/*     */     {
/* 382 */       return this.mRequestUrl;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract interface ImageListener extends Response.ErrorListener
/*     */   {
/*     */     public abstract void onResponse(ImageLoader.ImageContainer paramImageContainer, boolean paramBoolean);
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.toolbox.ImageLoader
 * JD-Core Version:    0.5.4
 */