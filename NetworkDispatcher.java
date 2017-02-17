/*     */ package com.android.volley;
/*     */ 
/*     */ import android.annotation.TargetApi;
/*     */ import android.net.TrafficStats;
/*     */ import android.os.Build.VERSION;
/*     */ import android.os.Process;
/*     */ import android.os.SystemClock;
/*     */ import java.util.concurrent.BlockingQueue;
/*     */ 
/*     */ public class NetworkDispatcher extends Thread
/*     */ {
/*     */   private final BlockingQueue<Request<?>> mQueue;
/*     */   private final Network mNetwork;
/*     */   private final Cache mCache;
/*     */   private final ResponseDelivery mDelivery;
/*  45 */   private volatile boolean mQuit = false;
/*     */ 
/*     */   public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery)
/*     */   {
/*  59 */     this.mQueue = queue;
/*  60 */     this.mNetwork = network;
/*  61 */     this.mCache = cache;
/*  62 */     this.mDelivery = delivery;
/*     */   }
/*     */ 
/*     */   public void quit()
/*     */   {
/*  70 */     this.mQuit = true;
/*  71 */     interrupt();
/*     */   }
/*     */ 
/*     */   @TargetApi(14)
/*     */   private void addTrafficStatsTag(Request<?> request)
/*     */   {
/*  77 */     if (Build.VERSION.SDK_INT >= 14)
/*  78 */       TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  84 */     Process.setThreadPriority(10);
/*     */     while (true) {
/*  86 */       long startTimeMs = SystemClock.elapsedRealtime();
/*     */       Request request;
/*     */       try
/*     */       {
/*  90 */         request = (Request)this.mQueue.take();
/*     */       }
/*     */       catch (InterruptedException e) {
/*     */       }
/*     */ 
/*  93 */       if (this.mQuit);
/*  94 */       return;
/*     */       Request request;
/*     */       try
/*     */       {
/* 100 */         request.addMarker("network-queue-take");
/*     */ 
/* 104 */         if (request.isCanceled()) {
/* 105 */           request.finish("network-discard-cancelled");
/*     */         }
/*     */ 
/* 109 */         addTrafficStatsTag(request);
/*     */ 
/* 112 */         NetworkResponse networkResponse = this.mNetwork.performRequest(request);
/* 113 */         request.addMarker("network-http-complete");
/*     */ 
/* 117 */         if ((networkResponse.notModified) && (request.hasHadResponseDelivered())) {
/* 118 */           request.finish("not-modified");
/*     */         }
/*     */ 
/* 123 */         Response response = request.parseNetworkResponse(networkResponse);
/* 124 */         request.addMarker("network-parse-complete");
/*     */ 
/* 128 */         if ((request.shouldCache()) && (response.cacheEntry != null)) {
/* 129 */           this.mCache.put(request.getCacheKey(), response.cacheEntry);
/* 130 */           request.addMarker("network-cache-written");
/*     */         }
/*     */ 
/* 134 */         request.markDelivered();
/* 135 */         this.mDelivery.postResponse(request, response);
/*     */       } catch (VolleyError volleyError) {
/* 137 */         volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
/* 138 */         parseAndDeliverNetworkError(request, volleyError);
/*     */       } catch (Exception e) {
/* 140 */         VolleyLog.e(e, "Unhandled exception %s", new Object[] { e.toString() });
/* 141 */         VolleyError volleyError = new VolleyError(e);
/* 142 */         volleyError.setNetworkTimeMs(SystemClock.elapsedRealtime() - startTimeMs);
/* 143 */         this.mDelivery.postError(request, volleyError);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void parseAndDeliverNetworkError(Request<?> request, VolleyError error) {
/* 149 */     error = request.parseNetworkError(error);
/* 150 */     this.mDelivery.postError(request, error);
/*     */   }
/*     */ }

/* Location:           E:\volley.jar
 * Qualified Name:     com.android.volley.NetworkDispatcher
 * JD-Core Version:    0.5.4
 */