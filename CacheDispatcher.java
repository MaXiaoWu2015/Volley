/*     */ package com.android.volley;
/*     */ 
/*     */ import android.os.Process;
/*     */ import java.util.concurrent.BlockingQueue;
/*     */ 
/*     */ public class CacheDispatcher extends Thread
/*     */ {
/*  34 */   private static final boolean DEBUG = VolleyLog.DEBUG;
/*     */   private final BlockingQueue<Request<?>> mCacheQueue;
/*     */   private final BlockingQueue<Request<?>> mNetworkQueue;
/*     */   private final Cache mCache;
/*     */   private final ResponseDelivery mDelivery;
/*  49 */   private volatile boolean mQuit = false;
/*     */ 
/*     */   public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue, Cache cache, ResponseDelivery delivery)
/*     */   {
/*  63 */     this.mCacheQueue = cacheQueue;
/*  64 */     this.mNetworkQueue = networkQueue;
/*  65 */     this.mCache = cache;
/*  66 */     this.mDelivery = delivery;
/*     */   }
/*     */ 
/*     */   public void quit()
/*     */   {
/*  74 */     this.mQuit = true;
/*  75 */     interrupt();
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  80 */     if (DEBUG) VolleyLog.v("start new dispatcher", new Object[0]);
/*  81 */     Process.setThreadPriority(10);
/*     */ 
/*  84 */     this.mCache.initialize();
/*     */     do try {
/*     */         Request request;
/*     */         Cache.Entry entry;
/*     */         Response response;
/*     */         while (true) {
/*  90 */           request = (Request)this.mCacheQueue.take();
/*  91 */           request.addMarker("cache-queue-take");
/*     */ 
/*  94 */           if (request.isCanceled()) {
/*  95 */             request.finish("cache-discard-canceled");
/*     */           }
/*     */ 
/* 100 */           entry = this.mCache.get(request.getCacheKey());
/* 101 */           if (entry == null) {
/* 102 */             request.addMarker("cache-miss");
/*     */ 
/* 104 */             this.mNetworkQueue.put(request);
/*     */           }
/*     */ 
/* 109 */           if (entry.isExpired()) {
/* 110 */             request.addMarker("cache-hit-expired");
/* 111 */             request.setCacheEntry(entry);
/* 112 */             this.mNetworkQueue.put(request);
/*     */           }
/*     */ 
/* 117 */           request.addMarker("cache-hit");
/* 118 */           response = request.parseNetworkResponse(
/* 119 */             new NetworkResponse(entry.data, entry.responseHeaders));
/* 120 */           request.addMarker("cache-hit-parsed");
/*     */ 
/* 122 */           if (entry.refreshNeeded())
/*     */             break;
/* 124 */           this.mDelivery.postResponse(request, response);
/*     */         }
/*     */ 
/* 129 */         request.addMarker("cache-hit-refresh-needed");
/* 130 */         request.setCacheEntry(entry);
/*     */ 
/* 133 */         response.intermediate = true;
/*     */ 
/* 137 */         this.mDelivery.postResponse(request, response, new Runnable(request)
/*     */         {
/*     */           public void run() {
/*     */             try {
/* 141 */               CacheDispatcher.this.mNetworkQueue.put(this.val$request);
/*     */             }
/*     */             catch (InterruptedException localInterruptedException)
/*     */             {
/*     */             }
/*     */           }
/*     */         });
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/*     */       } while (!this.mQuit);
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.CacheDispatcher
 * JD-Core Version:    0.5.4
 */