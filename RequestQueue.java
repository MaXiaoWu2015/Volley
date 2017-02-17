/*     */ package com.android.volley;
/*     */ 
/*     */ import android.os.Handler;
/*     */ import android.os.Looper;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Queue;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.PriorityBlockingQueue;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ 
/*     */ public class RequestQueue
/*     */ {
/*  49 */   private AtomicInteger mSequenceGenerator = new AtomicInteger();
/*     */ 
/*  62 */   private final Map<String, Queue<Request<?>>> mWaitingRequests = new HashMap();
/*     */ 
/*  69 */   private final Set<Request<?>> mCurrentRequests = new HashSet();
/*     */ 
/*  73 */   private final PriorityBlockingQueue<Request<?>> mCacheQueue = new PriorityBlockingQueue();
/*     */ 
/*  77 */   private final PriorityBlockingQueue<Request<?>> mNetworkQueue = new PriorityBlockingQueue();
/*     */   private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
/*     */   private final Cache mCache;
/*     */   private final Network mNetwork;
/*     */   private final ResponseDelivery mDelivery;
/*     */   private NetworkDispatcher[] mDispatchers;
/*     */   private CacheDispatcher mCacheDispatcher;
/*  98 */   private List<RequestFinishedListener> mFinishedListeners = new ArrayList();
/*     */ 
/*     */   public RequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery)
/*     */   {
/* 110 */     this.mCache = cache;
/* 111 */     this.mNetwork = network;
/* 112 */     this.mDispatchers = new NetworkDispatcher[threadPoolSize];
/* 113 */     this.mDelivery = delivery;
/*     */   }
/*     */ 
/*     */   public RequestQueue(Cache cache, Network network, int threadPoolSize)
/*     */   {
/* 125 */     this(cache, network, threadPoolSize, 
/* 125 */       new ExecutorDelivery(new Handler(Looper.getMainLooper())));
/*     */   }
/*     */ 
/*     */   public RequestQueue(Cache cache, Network network)
/*     */   {
/* 135 */     this(cache, network, 4);
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/* 142 */     stop();
/*     */ 
/* 144 */     this.mCacheDispatcher = new CacheDispatcher(this.mCacheQueue, this.mNetworkQueue, this.mCache, this.mDelivery);
/* 145 */     this.mCacheDispatcher.start();
/*     */ 
/* 148 */     for (int i = 0; i < this.mDispatchers.length; ++i) {
/* 149 */       NetworkDispatcher networkDispatcher = new NetworkDispatcher(this.mNetworkQueue, this.mNetwork, 
/* 150 */         this.mCache, this.mDelivery);
/* 151 */       this.mDispatchers[i] = networkDispatcher;
/* 152 */       networkDispatcher.start();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 160 */     if (this.mCacheDispatcher != null) {
/* 161 */       this.mCacheDispatcher.quit();
/*     */     }
/* 163 */     for (int i = 0; i < this.mDispatchers.length; ++i)
/* 164 */       if (this.mDispatchers[i] != null)
/* 165 */         this.mDispatchers[i].quit();
/*     */   }
/*     */ 
/*     */   public int getSequenceNumber()
/*     */   {
/* 174 */     return this.mSequenceGenerator.incrementAndGet();
/*     */   }
/*     */ 
/*     */   public Cache getCache()
/*     */   {
/* 181 */     return this.mCache;
/*     */   }
/*     */ 
/*     */   public void cancelAll(RequestFilter filter)
/*     */   {
/* 197 */     synchronized (this.mCurrentRequests) {
/* 198 */       for (Request request : this.mCurrentRequests)
/* 199 */         if (filter.apply(request))
/* 200 */           request.cancel();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cancelAll(Object tag)
/*     */   {
/* 211 */     if (tag == null) {
/* 212 */       throw new IllegalArgumentException("Cannot cancelAll with a null tag");
/*     */     }
/* 214 */     cancelAll(new RequestFilter(tag)
/*     */     {
/*     */       public boolean apply(Request<?> request) {
/* 217 */         return request.getTag() == this.val$tag;
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public <T> Request<T> add(Request<T> request)
/*     */   {
/* 229 */     request.setRequestQueue(this);
/* 230 */     synchronized (this.mCurrentRequests) {
/* 231 */       this.mCurrentRequests.add(request);
/*     */     }
/*     */ 
/* 235 */     request.setSequence(getSequenceNumber());
/* 236 */     request.addMarker("add-to-queue");
/*     */ 
/* 239 */     if (!request.shouldCache()) {
/* 240 */       this.mNetworkQueue.add(request);
/* 241 */       return request;
/*     */     }
/*     */ 
/* 245 */     synchronized (this.mWaitingRequests) {
/* 246 */       String cacheKey = request.getCacheKey();
/* 247 */       if (this.mWaitingRequests.containsKey(cacheKey))
/*     */       {
/* 249 */         Queue stagedRequests = (Queue)this.mWaitingRequests.get(cacheKey);
/* 250 */         if (stagedRequests == null) {
/* 251 */           stagedRequests = new LinkedList();
/*     */         }
/* 253 */         stagedRequests.add(request);
/* 254 */         this.mWaitingRequests.put(cacheKey, stagedRequests);
/* 255 */         if (VolleyLog.DEBUG) {
/* 256 */           VolleyLog.v("Request for cacheKey=%s is in flight, putting on hold.", new Object[] { cacheKey });
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 261 */         this.mWaitingRequests.put(cacheKey, null);
/* 262 */         this.mCacheQueue.add(request);
/*     */       }
/* 264 */       return request;
/*     */     }
/*     */   }
/*     */ 
/*     */   <T> void finish(Request<T> request)
/*     */   {
/* 277 */     synchronized (this.mCurrentRequests) {
/* 278 */       this.mCurrentRequests.remove(request);
/*     */     }
/* 280 */     synchronized (this.mFinishedListeners) {
/* 281 */       for (RequestFinishedListener listener : this.mFinishedListeners) {
/* 282 */         listener.onRequestFinished(request);
/*     */       }
/*     */     }
/*     */ 
/* 286 */     if (request.shouldCache())
/* 287 */       synchronized (this.mWaitingRequests) {
/* 288 */         String cacheKey = request.getCacheKey();
/* 289 */         Queue waitingRequests = (Queue)this.mWaitingRequests.remove(cacheKey);
/* 290 */         if (waitingRequests != null) {
/* 291 */           if (VolleyLog.DEBUG) {
/* 292 */             VolleyLog.v("Releasing %d waiting requests for cacheKey=%s.", new Object[] { 
/* 293 */               Integer.valueOf(waitingRequests.size()), cacheKey });
/*     */           }
/*     */ 
/* 297 */           this.mCacheQueue.addAll(waitingRequests);
/*     */         }
/*     */       }
/*     */   }
/*     */ 
/*     */   public <T> void addRequestFinishedListener(RequestFinishedListener<T> listener)
/*     */   {
/* 304 */     synchronized (this.mFinishedListeners) {
/* 305 */       this.mFinishedListeners.add(listener);
/*     */     }
/*     */   }
/*     */ 
/*     */   public <T> void removeRequestFinishedListener(RequestFinishedListener<T> listener)
/*     */   {
/* 313 */     synchronized (this.mFinishedListeners) {
/* 314 */       this.mFinishedListeners.remove(listener);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract interface RequestFilter
/*     */   {
/*     */     public abstract boolean apply(Request<?> paramRequest);
/*     */   }
/*     */ 
/*     */   public static abstract interface RequestFinishedListener<T>
/*     */   {
/*     */     public abstract void onRequestFinished(Request<T> paramRequest);
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.RequestQueue
 * JD-Core Version:    0.5.4
 */